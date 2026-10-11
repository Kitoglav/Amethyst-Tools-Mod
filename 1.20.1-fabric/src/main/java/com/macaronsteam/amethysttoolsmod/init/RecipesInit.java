package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.recipe.TippedAmethystArrowRecipe;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.util.GsonHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public final class RecipesInit {
  private static final RecipeSerializer<?> SERIALIZER = Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER,
      new ResourceLocation(AmethystToolsMod.MODID, "crafting_special_amethysttippedarrow"),
      new SimpleCraftingRecipeSerializer<>(TippedAmethystArrowRecipe::new));
  public static final Supplier<RecipeSerializer<?>> RECIPE_AMETHYST_TIPPED_ARROW = () -> SERIALIZER;
  public static final RecipeSerializer<CompatUpgrade> COMPAT_UPGRADE = Registry.register(
      BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(AmethystToolsMod.MODID, "compat_upgrade"),
      new CompatUpgrade.Serializer());

  private RecipesInit() {
  }

  public static void register() {
    ResourceConditions.register(new ResourceLocation(AmethystToolsMod.MODID, "config_value"),
        json -> AmethystToolsModConfig.isRecipeEnabled(GsonHelper.getAsString(json, "configValue")));
  }
}
