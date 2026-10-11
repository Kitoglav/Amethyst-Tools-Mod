package com.macaronsteam.amethysttoolsmod.compat;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
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
    CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.putBoolean(UPGRADE_TAG, true));
    Integer maxDamage = result.get(DataComponents.MAX_DAMAGE);
    if (maxDamage != null && maxDamage > 0) {
      result.set(DataComponents.MAX_DAMAGE, Math.max(1, (int) Math.min(Integer.MAX_VALUE,
          maxDamage * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get())));
    }
    ItemAttributeModifiers modifiers = result.get(DataComponents.ATTRIBUTE_MODIFIERS);
    if (modifiers != null) {
      modifiers = CompatStats.attributes(modifiers);
      result.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }
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
