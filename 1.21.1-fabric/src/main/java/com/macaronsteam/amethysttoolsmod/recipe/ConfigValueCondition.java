package com.macaronsteam.amethysttoolsmod.recipe;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public record ConfigValueCondition(String configValue) implements ResourceCondition {
  public static final ResourceConditionType<ConfigValueCondition> TYPE = ResourceConditionType.create(
      ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "config_value"),
      Codec.STRING.fieldOf("configValue").xmap(ConfigValueCondition::new, ConfigValueCondition::configValue));

  @Override
  public ResourceConditionType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(HolderLookup.Provider registries) {
    return AmethystToolsModConfig.isRecipeEnabled(configValue);
  }
}
