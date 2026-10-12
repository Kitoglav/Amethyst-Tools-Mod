package com.macaronsteam.amethysttoolsmod.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

/** Validated, immutable snapshot of integration modules from the user's config directory. */
public final class CompatModules {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static volatile Map<Item, Entry> items = Map.of();

  private CompatModules() {}

  public static void registerReloadListener() {
    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
      @Override public ResourceLocation getFabricId() { return ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "compat_modules"); }
      @Override public void onResourceManagerReload(ResourceManager manager) { CompatModules.reload(); }
    });
  }

  public static Path directory() {
    return FabricLoader.getInstance().getConfigDir().resolve("amethyst_tools");
  }

  public static void reload() {
    reload(directory());
  }

  /** Loads a snapshot from the chosen directory; also used by compatibility tests. */
  static void reload(Path directory) {
    Map<Item, Entry> loaded = new HashMap<>();
    try {
      Files.createDirectories(directory);
      try (Stream<Path> files = Files.list(directory)) {
        files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
            .sorted()
            .forEach(path -> loadFile(path, loaded));
      }
    } catch (IOException exception) {
      LOGGER.error("Amethyst Tools compat: cannot read module directory {}", directory, exception);
      return;
    }
    items = Collections.unmodifiableMap(loaded);
    LOGGER.info("Amethyst Tools compat: loaded {} upgradeable items from {}", loaded.size(), directory);
  }

  private static void loadFile(Path file, Map<Item, Entry> loaded) {
    String moduleName = "<unreadable>";
    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      moduleName = requiredString(root, "module_name");
      if (!optionalBoolean(root, "enabled", true)) {
        LOGGER.info("Amethyst Tools compat: module '{}' in {} is disabled", moduleName, file);
        return;
      }
      if (!requiredModsPresent(root)) {
        LOGGER.info("Amethyst Tools compat: module '{}' in {} skipped (required mods are missing)", moduleName, file);
        return;
      }
      JsonArray entries = root.getAsJsonArray("items");
      if (entries == null) {
        throw new IllegalArgumentException("missing items array");
      }
      Map<Item, Entry> moduleItems = new HashMap<>();
      for (JsonElement element : entries) {
        loadItem(element.getAsJsonObject(), moduleName, file, moduleItems);
      }
      for (Map.Entry<Item, Entry> candidate : moduleItems.entrySet()) {
        Entry previous = loaded.get(candidate.getKey());
        if (previous != null) {
          throw new IllegalArgumentException("duplicate item '" + candidate.getValue().id()
              + "' (already in module '" + previous.moduleName + "' at " + previous.file + ")");
        }
      }
      loaded.putAll(moduleItems);
      LOGGER.info("Amethyst Tools compat: loaded module '{}' from {}", moduleName, file);
    } catch (IOException | RuntimeException exception) {
      LOGGER.error("Amethyst Tools compat: failed to load module '{}' from {}: {}", moduleName, file, exception.getMessage(), exception);
    }
  }

  private static boolean requiredModsPresent(JsonObject root) {
    JsonArray required = root.getAsJsonArray("mods_required");
    if (required == null) {
      return true;
    }
    for (JsonElement element : required) {
      if (!FabricLoader.getInstance().isModLoaded(element.getAsString())) {
        return false;
      }
    }
    return true;
  }

  private static void loadItem(JsonObject json, String moduleName, Path file, Map<Item, Entry> loaded) {
    if (!optionalBoolean(json, "enabled", true)) {
      return;
    }
    ResourceLocation id = ResourceLocation.parse(requiredString(json, "id"));
    if (!BuiltInRegistries.ITEM.containsKey(id)) {
      throw new IllegalArgumentException("unknown item '" + id + "'");
    }
    String modelPath = requiredString(json, "overlay_model");
    if (modelPath.endsWith(".json")) {
      modelPath = modelPath.substring(0, modelPath.length() - 5);
    }
    ResourceLocation model = ResourceLocation.parse(modelPath);
    boolean clusterLv2 = optionalBoolean(json, "cluster_lv2", false);
    boolean replaceModel = optionalBoolean(json, "replace_model", false);
    Item item = BuiltInRegistries.ITEM.get(id);
    Entry previous = loaded.putIfAbsent(item, new Entry(id, clusterLv2, model, replaceModel, moduleName, file));
    if (previous != null) {
      throw new IllegalArgumentException("duplicate item '" + id + "' (already in module '" + previous.moduleName + "' at " + previous.file + ")");
    }
  }

  private static String requiredString(JsonObject json, String key) {
    if (!json.has(key) || !json.get(key).isJsonPrimitive()) {
      throw new IllegalArgumentException("missing or invalid '" + key + "'");
    }
    String value = json.get(key).getAsString().trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("empty '" + key + "'");
    }
    return value;
  }

  private static boolean optionalBoolean(JsonObject json, String key, boolean fallback) {
    if (!json.has(key)) {
      return fallback;
    }
    JsonElement value = json.get(key);
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
      throw new IllegalArgumentException("'" + key + "' must be a boolean");
    }
    return value.getAsBoolean();
  }

  public static Entry find(Item item) {
    return items.get(item);
  }

  public static Map<Item, Entry> items() {
    return items;
  }

  public record Entry(ResourceLocation id, boolean clusterLv2, ResourceLocation overlayModel,
                      boolean replaceModel, String moduleName, Path file) {}
}
