/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import java.util.function.Supplier;
import com.google.common.primitives.Booleans;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.recipes.TippedAmethystArrowRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipesInit {
  public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
      DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AmethystToolsMod.MODID);
  public static RegistryObject<RecipeSerializer<?>> RECIPE_AMETHYST_TIPPED_ARROW =
      register("crafting_special_amethysttippedarrow",
          () -> new SimpleRecipeSerializer<>(TippedAmethystArrowRecipe::new),
          AmethystToolsModConfig.enableAmethystArrows.get(),
          AmethystToolsModConfig.enableExtraArrows.get());

  private static RegistryObject<RecipeSerializer<?>> register(String name, Supplier recipe,
      boolean... condition) {
    if (!Booleans.contains(condition, false) && recipe != null)
      return RECIPES.register(name, recipe);
    return null;
  }
}
