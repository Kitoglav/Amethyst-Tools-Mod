/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.recipe;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedAmethystArrowRecipe extends CustomRecipe {
  public TippedAmethystArrowRecipe(ResourceLocation location, CraftingBookCategory category) {
    super(location, category);
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
    ItemStack potion = container.getItem(4);
    if (!potion.is(Items.LINGERING_POTION)) {
      return ItemStack.EMPTY;
    }

    ItemStack arrows = new ItemStack(ItemsInit.AMETHYST_TIPPED_ARROW.get(), 8);
    PotionUtils.setPotion(arrows, PotionUtils.getPotion(potion));
    PotionUtils.setCustomEffects(arrows, PotionUtils.getCustomEffects(potion));
    return arrows;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width >= 3 && height >= 3;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return RecipesInit.RECIPE_AMETHYST_TIPPED_ARROW.get();
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    if (container.getWidth() != 3 || container.getHeight() != 3) {
      return false;
    }

    for (int slot = 0; slot < 9; slot++) {
      ItemStack stack = container.getItem(slot);
      if (slot == 4 ? !stack.is(Items.LINGERING_POTION) : !stack.is(ItemsInit.AMETHYST_ARROW.get())) {
        return false;
      }
    }
    return true;
  }
}
