package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.recipe.ConfigValueCondition;
import com.macaronsteam.amethysttoolsmod.recipe.TippedAmethystArrowRecipe;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public final class RecipesInit {
  private static final RecipeSerializer<?> SERIALIZER = Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "crafting_special_amethysttippedarrow"),
      new SimpleCraftingRecipeSerializer<>(TippedAmethystArrowRecipe::new));
  public static final Supplier<RecipeSerializer<?>> RECIPE_AMETHYST_TIPPED_ARROW = () -> SERIALIZER;
  public static final RecipeSerializer<CompatUpgrade> COMPAT_UPGRADE = Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "compat_upgrade"),
      new CompatUpgrade.Serializer());

  private RecipesInit() {
  }

  public static void register() {
    ResourceConditions.register(ConfigValueCondition.TYPE);
  }
}
