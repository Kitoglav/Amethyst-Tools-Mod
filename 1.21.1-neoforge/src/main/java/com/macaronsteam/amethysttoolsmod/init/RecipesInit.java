package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.recipe.ConfigValueCondition;
import com.macaronsteam.amethysttoolsmod.recipe.TippedAmethystArrowRecipe;
import java.util.function.Supplier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class RecipesInit {
  private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, AmethystToolsMod.MODID);
  private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
      DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, AmethystToolsMod.MODID);
  public static final Supplier<RecipeSerializer<?>> RECIPE_AMETHYST_TIPPED_ARROW =
      SERIALIZERS.register("crafting_special_amethysttippedarrow", () -> new SimpleCraftingRecipeSerializer<>(TippedAmethystArrowRecipe::new));
  public static final Supplier<RecipeSerializer<CompatUpgrade>> COMPAT_UPGRADE =
      SERIALIZERS.register("compat_upgrade", CompatUpgrade.Serializer::new);
  private static final Supplier<MapCodec<? extends ICondition>> CONFIG_CONDITION =
      CONDITIONS.register("config_value", () -> ConfigValueCondition.CODEC);

  private RecipesInit() {
  }

  public static void register(IEventBus bus) {
    SERIALIZERS.register(bus);
    CONDITIONS.register(bus);
  }
}
