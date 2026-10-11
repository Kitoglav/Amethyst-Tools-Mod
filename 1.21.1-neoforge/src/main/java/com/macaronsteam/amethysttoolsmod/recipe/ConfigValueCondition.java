package com.macaronsteam.amethysttoolsmod.recipe;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public record ConfigValueCondition(String configValue) implements ICondition {
  public static final MapCodec<ConfigValueCondition> CODEC = Codec.STRING.fieldOf("configValue")
      .xmap(ConfigValueCondition::new, ConfigValueCondition::configValue);

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return AmethystToolsModConfig.isRecipeEnabled(configValue);
  }
}
