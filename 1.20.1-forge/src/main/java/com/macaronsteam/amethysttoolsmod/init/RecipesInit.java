/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.recipe.ConfigValueCondition;
import com.macaronsteam.amethysttoolsmod.recipe.TippedAmethystArrowRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipesInit {
  public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AmethystToolsMod.MODID);
  public static final RegistryObject<RecipeSerializer<?>> RECIPE_AMETHYST_TIPPED_ARROW = RECIPES.register("crafting_special_amethysttippedarrow", () -> new SimpleCraftingRecipeSerializer<>(TippedAmethystArrowRecipe::new));
  public static final RegistryObject<RecipeSerializer<?>> COMPAT_UPGRADE = RECIPES.register("compat_upgrade", CompatUpgrade.Serializer::new);

  public static void register() {
    CraftingHelper.register(ConfigValueCondition.Serializer.INSTANCE);
  }
}
