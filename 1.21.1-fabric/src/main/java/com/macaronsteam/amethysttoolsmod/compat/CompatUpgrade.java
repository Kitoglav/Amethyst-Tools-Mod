package com.macaronsteam.amethysttoolsmod.compat;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

/** A configuration-driven smithing recipe that preserves the original item. */
public final class CompatUpgrade extends SmithingTransformRecipe {
  public static final String UPGRADE_TAG = "AmethystToolsUpgrade";
  private static final String BASE_STATS_TAG = "AmethystToolsBaseStats";

  public CompatUpgrade() {
    super(Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
  }

  @Override
  public boolean matches(SmithingRecipeInput input, Level level) {
    ItemStack base = input.base();
    CompatModules.Entry entry = CompatModules.find(base.getItem());
    return entry != null && input.template().is(Items.AMETHYST_SHARD) && !isUpgraded(base)
        && input.addition().is(entry.clusterLv2()
            ? ItemsInit.AMETHYST_CLUSTER_LV2.get() : ItemsInit.AMETHYST_CLUSTER.get());
  }

  @Override
  public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
    return upgradedCopy(input.base());
  }

  /** Also used by JEI, so its displayed result has exactly the same components as smithing. */
  public static ItemStack upgradedCopy(ItemStack base) {
    ItemStack result = base.copyWithCount(1);
    if (isUpgraded(base)) {
      return result;
    }
    CompoundTag baseline = new CompoundTag();
    Integer maxDamage = result.get(DataComponents.MAX_DAMAGE);
    if (maxDamage != null) {
      baseline.putInt("max_damage", maxDamage);
    }
    ItemAttributeModifiers original = result.get(DataComponents.ATTRIBUTE_MODIFIERS);
    if (original != null) {
      baseline.put("attributes", ItemAttributeModifiers.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow());
    }
    if (maxDamage != null && maxDamage > 0) {
      result.set(DataComponents.MAX_DAMAGE, Math.max(1, (int) Math.min(Integer.MAX_VALUE,
          maxDamage * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get())));
    }
    ItemAttributeModifiers modifiers = result.get(DataComponents.ATTRIBUTE_MODIFIERS);
    if (modifiers != null) {
      modifiers = CompatStats.attributes(modifiers);
      result.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
      baseline.put("boosted_attributes", ItemAttributeModifiers.CODEC.encodeStart(NbtOps.INSTANCE, modifiers).getOrThrow());
    }
    Integer boostedDamage = result.get(DataComponents.MAX_DAMAGE);
    if (boostedDamage != null) {
      baseline.putInt("boosted_max_damage", boostedDamage);
    }
    CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
      tag.putBoolean(UPGRADE_TAG, true);
      tag.put(BASE_STATS_TAG, baseline);
    });
    return result;
  }

  @Override
  public boolean isTemplateIngredient(ItemStack stack) {
    return stack.is(Items.AMETHYST_SHARD);
  }

  @Override
  public boolean isBaseIngredient(ItemStack stack) {
    return CompatModules.find(stack.getItem()) != null && !isUpgraded(stack);
  }

  @Override
  public boolean isAdditionIngredient(ItemStack stack) {
    return stack.is(ItemsInit.AMETHYST_CLUSTER.get()) || stack.is(ItemsInit.AMETHYST_CLUSTER_LV2.get());
  }

  @Override
  public boolean isIncomplete() {
    // JEI uses concrete displays instead of this configuration-driven placeholder.
    return true;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return RecipesInit.COMPAT_UPGRADE;
  }

  /**
   * Restores unboosted components before a normal recipe changes the item type.
   * Setting a component to its prototype value also removes its override, so
   * vanilla smithing uses the destination item's durability and attributes.
   */
  public static ItemStack plainCopy(ItemStack stack) {
    ItemStack result = stack.copy();
    if (!isUpgraded(stack)) {
      return result;
    }
    CompoundTag data = stack.get(DataComponents.CUSTOM_DATA).copyTag();
    CompoundTag baseline = data.getCompound(BASE_STATS_TAG);
    Integer maxDamage = stack.get(DataComponents.MAX_DAMAGE);
    if (baseline.contains("max_damage", Tag.TAG_INT)) {
      int original = baseline.getInt("max_damage");
      int applied = baseline.getInt("boosted_max_damage") - original;
      result.set(DataComponents.MAX_DAMAGE, Math.max(1, maxDamage == null ? original : maxDamage - applied));
    } else if (maxDamage != null) {
      // 2.0.0 did not save the baseline. Prefer the prototype to avoid rounding drift.
      Integer prototype = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
      double multiplier = AmethystToolsModConfig.DURABILITY_MULTIPLIER.get();
      int restored = prototype != null && maxDamage == Math.max(1, (int) (prototype * multiplier))
          ? prototype : Math.max(1, (int) Math.ceil(maxDamage / multiplier));
      result.set(DataComponents.MAX_DAMAGE, restored);
    }
    ItemAttributeModifiers attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
    if (attributes != null) {
      ItemAttributeModifiers original = readAttributes(baseline, "attributes");
      ItemAttributeModifiers boosted = readAttributes(baseline, "boosted_attributes");
      result.set(DataComponents.ATTRIBUTE_MODIFIERS, original != null && boosted != null
          ? CompatStats.withoutBonuses(attributes, original, boosted) : CompatStats.withoutBonuses(attributes));
    }
    CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
      tag.remove(UPGRADE_TAG);
      tag.remove(BASE_STATS_TAG);
    });
    return result;
  }

  private static ItemAttributeModifiers readAttributes(CompoundTag data, String key) {
    Tag value = data.get(key);
    return value == null ? null : ItemAttributeModifiers.CODEC.parse(NbtOps.INSTANCE, value).result().orElse(null);
  }

  /** The recipe has already run on a plain input; apply the destination's bonuses once. */
  public static ItemStack transfer(ItemStack base, ItemStack result) {
    if (!isUpgraded(base) || result.isEmpty()) {
      return result;
    }
    if (CompatModules.find(result.getItem()) == null) {
      return plainCopy(result);
    }
    ItemStack transferred = upgradedCopy(result);
    transferred.setCount(result.getCount());
    return transferred;
  }

  public static boolean isUpgraded(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(UPGRADE_TAG);
  }

  public static final class Serializer implements RecipeSerializer<CompatUpgrade> {
    private static final CompatUpgrade RECIPE = new CompatUpgrade();
    @Override public MapCodec<CompatUpgrade> codec() { return MapCodec.unit(RECIPE); }
    @Override public StreamCodec<RegistryFriendlyByteBuf, CompatUpgrade> streamCodec() { return StreamCodec.unit(RECIPE); }
  }
}
