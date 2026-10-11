/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

public final class AmethystToolsModConfig {

  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  public static final ForgeConfigSpec.BooleanValue ENABLE_IRON;
  public static final ForgeConfigSpec.BooleanValue ENABLE_DIAMOND;
  public static final ForgeConfigSpec.BooleanValue ENABLE_NETHERITE;
  public static final ForgeConfigSpec.BooleanValue ENABLE_AMETHYST_ARROWS;
  public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_ARROWS;
  public static final ForgeConfigSpec.BooleanValue ENABLE_AMETHYST_TRIDENT;
  public static final ForgeConfigSpec.DoubleValue DURABILITY_MULTIPLIER;
  public static final ForgeConfigSpec.DoubleValue EXTRA_DIG_SPEED;
  public static final ForgeConfigSpec.DoubleValue EXTRA_ATTACK_DAMAGE;
  public static final ForgeConfigSpec.IntValue EXTRA_ENCHANTABILITY;
  public static final ForgeConfigSpec.IntValue EXTRA_ARMOR;
  public static final ForgeConfigSpec.DoubleValue EXTRA_TOUGHNESS;
  public static final ForgeConfigSpec.DoubleValue EXTRA_KNOCKBACK_RESISTANCE;
  public static final ForgeConfigSpec.DoubleValue ARROW_EXTRA_DAMAGE;
  public static final ForgeConfigSpec.IntValue GLOWING_DURATION;
  public static final ForgeConfigSpec SPEC;
  private static CommentedConfig startupValues;

  static {
    BUILDER.comment("Recipe availability. Registered content is kept identical on client and server.");
    ENABLE_IRON = BUILDER.define("enableIron", true);
    ENABLE_DIAMOND = BUILDER.define("enableDiamond", true);
    ENABLE_NETHERITE = BUILDER.define("enableNetherite", true);
    ENABLE_AMETHYST_ARROWS = BUILDER.define("enableAmethystArrows", true);
    ENABLE_EXTRA_ARROWS = BUILDER.define("enableExtraArrows", true);
    ENABLE_AMETHYST_TRIDENT = BUILDER.define("enableAmethystTrident", true);

    BUILDER.comment("Stats applied when Amethyst Tools items are created.");
    DURABILITY_MULTIPLIER = BUILDER.defineInRange("durabilityMultiplier", 1.2D, 0.1D, Double.MAX_VALUE);
    EXTRA_DIG_SPEED = BUILDER.defineInRange("extraDigSpeed", 1.0D, 0.0D, Double.MAX_VALUE);
    EXTRA_ATTACK_DAMAGE = BUILDER.defineInRange("extraAttackDamage", 2.0D, 0.0D, Double.MAX_VALUE);
    EXTRA_ENCHANTABILITY = BUILDER.defineInRange("extraEnchantability", 5, 0, Integer.MAX_VALUE);
    EXTRA_ARMOR = BUILDER.defineInRange("extraArmor", 1, 0, Integer.MAX_VALUE);
    EXTRA_TOUGHNESS = BUILDER.defineInRange("extraToughness", 1.0D, 0.0D, Double.MAX_VALUE);
    EXTRA_KNOCKBACK_RESISTANCE = BUILDER.defineInRange("extraKR", 0.0D, 0.0D, Double.MAX_VALUE);
    ARROW_EXTRA_DAMAGE = BUILDER.defineInRange("arrowExtraDamage", 3.0D, 0.0D, Double.MAX_VALUE);
    GLOWING_DURATION = BUILDER.defineInRange("glowingDuration", 300, 0, Integer.MAX_VALUE);
    SPEC = BUILDER.build();
  }

  private AmethystToolsModConfig() {}

  /** Item constructors run before Forge attaches its common config. */
  public static void loadStartupValues() {
    Path path = FMLPaths.CONFIGDIR.get().resolve("amethysttoolsmod-common.toml");
    if (!Files.isRegularFile(path)) {
      return;
    }
    try (Reader reader = Files.newBufferedReader(path)) {
      startupValues = new TomlParser().parse(reader);
      SPEC.correct(startupValues);
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot read Amethyst Tools config: " + path, exception);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T read(ForgeConfigSpec.ConfigValue<T> value) {
    if (SPEC.isLoaded()) {
      return value.get();
    }
    if (startupValues != null) {
      T configured = startupValues.get(value.getPath());
      if (configured != null) {
        return configured;
      }
    }
    return value.getDefault();
  }

  public static boolean isRecipeEnabled(String name) {
    return switch (name) {
      case "enableIron" -> ENABLE_IRON.get();
      case "enableDiamond" -> ENABLE_DIAMOND.get();
      case "enableNetherite" -> ENABLE_NETHERITE.get();
      case "enableAmethystArrows" -> ENABLE_AMETHYST_ARROWS.get();
      case "enableExtraArrows" -> ENABLE_EXTRA_ARROWS.get();
      case "enableAmethystTrident" -> ENABLE_AMETHYST_TRIDENT.get();
      default -> throw new IllegalArgumentException("Unknown config recipe condition: " + name);
    };
  }
}
