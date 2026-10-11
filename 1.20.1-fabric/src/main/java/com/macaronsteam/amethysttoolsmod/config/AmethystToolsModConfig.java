package com.macaronsteam.amethysttoolsmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Startup config shared by equipment stats and resource conditions. */
public final class AmethystToolsModConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static final Value<Boolean> ENABLE_IRON = new Value<>(true);
  public static final Value<Boolean> ENABLE_DIAMOND = new Value<>(true);
  public static final Value<Boolean> ENABLE_NETHERITE = new Value<>(true);
  public static final Value<Boolean> ENABLE_AMETHYST_ARROWS = new Value<>(true);
  public static final Value<Boolean> ENABLE_EXTRA_ARROWS = new Value<>(true);
  public static final Value<Boolean> ENABLE_AMETHYST_TRIDENT = new Value<>(true);
  public static final Value<Double> DURABILITY_MULTIPLIER = new Value<>(1.2D);
  public static final Value<Double> EXTRA_DIG_SPEED = new Value<>(1.0D);
  public static final Value<Double> EXTRA_ATTACK_DAMAGE = new Value<>(2.0D);
  public static final Value<Integer> EXTRA_ENCHANTABILITY = new Value<>(5);
  public static final Value<Integer> EXTRA_ARMOR = new Value<>(1);
  public static final Value<Double> EXTRA_TOUGHNESS = new Value<>(1.0D);
  public static final Value<Double> EXTRA_KNOCKBACK_RESISTANCE = new Value<>(0.0D);
  public static final Value<Double> ARROW_EXTRA_DAMAGE = new Value<>(3.0D);
  public static final Value<Integer> GLOWING_DURATION = new Value<>(300);

  private AmethystToolsModConfig() {
  }

  public static void load() throws IOException {
    Path path = FabricLoader.getInstance().getConfigDir().resolve("amethysttoolsmod.json");
    if (Files.exists(path)) {
      try {
        JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
        read(json);
      } catch (RuntimeException exception) {
        throw new IOException("Invalid Amethyst Tools config: " + path, exception);
      }
    } else {
      Files.createDirectories(path.getParent());
      Files.writeString(path, GSON.toJson(defaults()), StandardCharsets.UTF_8);
    }
  }

  private static void read(JsonObject json) {
    ENABLE_IRON.set(bool(json, "enableIron", true));
    ENABLE_DIAMOND.set(bool(json, "enableDiamond", true));
    ENABLE_NETHERITE.set(bool(json, "enableNetherite", true));
    ENABLE_AMETHYST_ARROWS.set(bool(json, "enableAmethystArrows", true));
    ENABLE_EXTRA_ARROWS.set(bool(json, "enableExtraArrows", true));
    ENABLE_AMETHYST_TRIDENT.set(bool(json, "enableAmethystTrident", true));
    DURABILITY_MULTIPLIER.set(number(json, "durabilityMultiplier", 1.2D, 0.1D));
    EXTRA_DIG_SPEED.set(number(json, "extraDigSpeed", 1.0D, 0.0D));
    EXTRA_ATTACK_DAMAGE.set(number(json, "extraAttackDamage", 2.0D, 0.0D));
    EXTRA_ENCHANTABILITY.set(integer(json, "extraEnchantability", 5));
    EXTRA_ARMOR.set(integer(json, "extraArmor", 1));
    EXTRA_TOUGHNESS.set(number(json, "extraToughness", 1.0D, 0.0D));
    EXTRA_KNOCKBACK_RESISTANCE.set(number(json, "extraKR", 0.0D, 0.0D));
    ARROW_EXTRA_DAMAGE.set(number(json, "arrowExtraDamage", 3.0D, 0.0D));
    GLOWING_DURATION.set(integer(json, "glowingDuration", 300));
  }

  private static boolean bool(JsonObject json, String key, boolean fallback) {
    return json.has(key) ? json.get(key).getAsBoolean() : fallback;
  }

  private static double number(JsonObject json, String key, double fallback, double minimum) {
    double value = json.has(key) ? json.get(key).getAsDouble() : fallback;
    if (!Double.isFinite(value) || value < minimum) {
      throw new IllegalArgumentException(key + " must be at least " + minimum);
    }
    return value;
  }

  private static int integer(JsonObject json, String key, int fallback) {
    int value = json.has(key) ? json.get(key).getAsInt() : fallback;
    if (value < 0) {
      throw new IllegalArgumentException(key + " must be non-negative");
    }
    return value;
  }

  private static JsonObject defaults() {
    JsonObject json = new JsonObject();
    json.addProperty("enableIron", true);
    json.addProperty("enableDiamond", true);
    json.addProperty("enableNetherite", true);
    json.addProperty("enableAmethystArrows", true);
    json.addProperty("enableExtraArrows", true);
    json.addProperty("enableAmethystTrident", true);
    json.addProperty("durabilityMultiplier", 1.2D);
    json.addProperty("extraDigSpeed", 1.0D);
    json.addProperty("extraAttackDamage", 2.0D);
    json.addProperty("extraEnchantability", 5);
    json.addProperty("extraArmor", 1);
    json.addProperty("extraToughness", 1.0D);
    json.addProperty("extraKR", 0.0D);
    json.addProperty("arrowExtraDamage", 3.0D);
    json.addProperty("glowingDuration", 300);
    return json;
  }

  public static boolean isRecipeEnabled(String name) {
    return switch (name) {
      case "enableIron" -> ENABLE_IRON.get();
      case "enableDiamond" -> ENABLE_DIAMOND.get();
      case "enableNetherite" -> ENABLE_NETHERITE.get();
      case "enableAmethystArrows" -> ENABLE_AMETHYST_ARROWS.get();
      case "enableExtraArrows" -> ENABLE_EXTRA_ARROWS.get();
      case "enableAmethystTrident" -> ENABLE_AMETHYST_TRIDENT.get();
      default -> throw new IllegalArgumentException("Unknown recipe flag: " + name);
    };
  }

  public static final class Value<T> {
    private T value;

    private Value(T defaultValue) {
      value = defaultValue;
    }

    public T get() {
      return value;
    }

    private void set(T newValue) {
      value = newValue;
    }
  }
}
