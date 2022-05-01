/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.recipe;

import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigValueCondition implements ICondition {
  private static final ResourceLocation NAME = new ResourceLocation(AmethystToolsMod.MODID, "config_value");
  public final BooleanValue value;

  public ConfigValueCondition(BooleanValue value) {
    this.value = value;
  }

  @Override
  public ResourceLocation getID() {
    return NAME;
  }

  @Override
  public boolean test() {
    return value.get();
  }

  public static ConfigValueCondition config(BooleanValue value) {
    return new ConfigValueCondition(value);
  }

  public static class Serializer implements IConditionSerializer<ConfigValueCondition> {

    public static final Serializer INSTANCE = new Serializer();

    @Override
    public void write(JsonObject json, ConfigValueCondition value) {
      json.addProperty("configValue", value.value.getPath().stream().collect(Collectors.joining(".")));
    }

    @Override
    public ConfigValueCondition read(JsonObject json) {
      return new ConfigValueCondition(AmethystToolsModConfig.spec.getValues().get(GsonHelper.getAsString(json, "configValue")));
    }

    @Override
    public ResourceLocation getID() {
      return NAME;
    }

  }
}
