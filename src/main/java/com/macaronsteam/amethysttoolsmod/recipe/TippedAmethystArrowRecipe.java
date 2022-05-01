/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.recipe;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedAmethystArrowRecipe extends CustomRecipe {
  public TippedAmethystArrowRecipe(ResourceLocation location) {
    super(location);
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    if (container.getWidth() == 3 && container.getHeight() == 3) {
      for (int i = 0; i < container.getWidth(); ++i) {
        for (int j = 0; j < container.getHeight(); ++j) {
          ItemStack stack = container.getItem(i + j * container.getWidth());
          if (stack.isEmpty()) {
            return false;
          }
          if (i == 1 && j == 1) {
            if (!stack.is(Items.LINGERING_POTION)) {
              return false;
            }
          } else if (!stack.is(ItemsInit.ITEM_AMETHYST_ARROW.get())) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public ItemStack assemble(CraftingContainer container) {
    ItemStack stack = container.getItem(1 + container.getWidth());
    if (!stack.is(Items.LINGERING_POTION)) {
      return ItemStack.EMPTY;
    } else {
      ItemStack stack1 = new ItemStack(ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.get(), 8);
      PotionUtils.setPotion(stack1, PotionUtils.getPotion(stack));
      PotionUtils.setCustomEffects(stack1, PotionUtils.getCustomEffects(stack));
      return stack1;
    }
  }

  @Override
  public boolean canCraftInDimensions(int d0, int d1) {
    return d0 >= 2 && d1 >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return RecipesInit.RECIPE_AMETHYST_TIPPED_ARROW.get();
  }
}
