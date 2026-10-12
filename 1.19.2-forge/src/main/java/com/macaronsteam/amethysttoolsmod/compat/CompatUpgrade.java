package com.macaronsteam.amethysttoolsmod.compat;

import com.google.gson.JsonObject;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

/** A single smithing recipe whose valid bases come from the active compat modules. */
public final class CompatUpgrade extends UpgradeRecipe {
  public static final String UPGRADE_TAG = "AmethystToolsUpgrade";

  public CompatUpgrade(ResourceLocation id) {
    super(id, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
  }

  @Override
  public boolean matches(Container container, Level level) {
    ItemStack base = container.getItem(0);
    CompatModules.Entry entry = CompatModules.find(base.getItem());
    return entry != null && !base.isEmpty() && !isUpgraded(base)
        && container.getItem(1).is(entry.clusterLv2()
            ? ItemsInit.AMETHYST_CLUSTER_LV2.get() : ItemsInit.AMETHYST_CLUSTER.get());
  }

  @Override
  public ItemStack assemble(Container container) {
    return upgradedCopy(container.getItem(0));
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
    return RecipesInit.COMPAT_UPGRADE.get();
  }

  /** A fresh preview/result, shared by smithing, JEI and creative tabs. */
  public static ItemStack upgradedCopy(ItemStack base) {
    ItemStack result = base.copy();
    result.setCount(1);
    result.getOrCreateTag().putBoolean(UPGRADE_TAG, true);
    return result;
  }

  /** Removes only our marker; the original item's data stays intact. */
  public static ItemStack plainCopy(ItemStack stack) {
    ItemStack result = stack.copy();
    if (result.hasTag()) {
      result.getTag().remove(UPGRADE_TAG);
      if (result.getTag().isEmpty()) {
        result.setTag(null);
      }
    }
    return result;
  }

  /** A normal recipe may carry the upgrade only to another enabled compat item. */
  public static ItemStack transfer(ItemStack base, ItemStack result) {
    if (!isUpgraded(base) || result.isEmpty()) {
      return result;
    }
    ItemStack transferred = CompatModules.find(result.getItem()) != null
        ? upgradedCopy(result) : plainCopy(result);
    transferred.setCount(result.getCount());
    return transferred;
  }

  public static boolean isUpgraded(ItemStack stack) {
    return stack.hasTag() && stack.getTag().getBoolean(UPGRADE_TAG);
  }

  public static final class Serializer implements RecipeSerializer<CompatUpgrade> {
    @Override
    public CompatUpgrade fromJson(ResourceLocation id, JsonObject json) {
      return new CompatUpgrade(id);
    }

    @Override
    public CompatUpgrade fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
      return new CompatUpgrade(id);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CompatUpgrade recipe) {
      // Module definitions are local configuration, not recipe payload.
    }
  }
}
