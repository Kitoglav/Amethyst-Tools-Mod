/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.recipe;

import com.google.gson.JsonObject;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigValueCondition implements ICondition {
  public static class Serializer implements IConditionSerializer<ConfigValueCondition> {

    public static final Serializer INSTANCE = new Serializer();

    @Override
    public ResourceLocation getID() {
      return NAME;
    }

    @Override
    public ConfigValueCondition read(JsonObject json) {
      return new ConfigValueCondition(GsonHelper.getAsString(json, "configValue"));
    }

    @Override
    public void write(JsonObject json, ConfigValueCondition value) {
      json.addProperty("configValue", value.configKey);
    }

  }

  private static final ResourceLocation NAME = new ResourceLocation(AmethystToolsMod.MODID, "config_value");

  private final String configKey;

  public ConfigValueCondition(String configKey) {
    this.configKey = configKey;
  }

  @Override
  public ResourceLocation getID() {
    return NAME;
  }

  @Override
  @SuppressWarnings("removal")
  public boolean test() {
    return AmethystToolsModConfig.isRecipeEnabled(configKey);
  }
}
