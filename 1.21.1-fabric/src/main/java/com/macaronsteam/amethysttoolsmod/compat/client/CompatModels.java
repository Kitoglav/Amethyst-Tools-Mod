package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/** Adds external overlay models and wraps only configured item models after baking. */
public final class CompatModels {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Set<ResourceLocation> MISSING = ConcurrentHashMap.newKeySet();
  private static final Map<ResourceLocation, BakedModel> OVERLAYS = new ConcurrentHashMap<>();

  private CompatModels() {}

  public static void register() {
    ModelLoadingPlugin.register(context -> {
      CompatModules.reload();
      OVERLAYS.clear();
      MISSING.clear();
      if (!CompatAssets.installed()) {
        return;
      }
      Set<ResourceLocation> ids = CompatModules.items().values().stream()
          .map(CompatModules.Entry::overlayModel).collect(java.util.stream.Collectors.toSet());
      context.addModels(ids);
      context.modifyModelAfterBake().register((model, bake) -> {
        if (model != null && ids.contains(bake.resourceId())) {
          OVERLAYS.put(bake.resourceId(), model);
        }
        return model;
      });
    });
  }

  public static BakedModel select(ItemStack stack, BakedModel original) {
    CompatModules.Entry entry = CompatModules.find(stack.getItem());
    if (entry == null || !CompatUpgrade.isUpgraded(stack)) {
      return original;
    }
    BakedModel overlay = OVERLAYS.get(entry.overlayModel());
    if (overlay == null || overlay == Minecraft.getInstance().getModelManager().getMissingModel()) {
      if (MISSING.add(entry.overlayModel())) {
        LOGGER.error("Amethyst Tools compat: module '{}' in {} has missing overlay model {}",
            entry.moduleName(), entry.file(), entry.overlayModel());
      }
      return original;
    }
    return entry.replaceModel() ? overlay : new CompatItemModel(original, overlay);
  }
}
