package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.mixin.client.ModelManagerAccessor;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class CompatModels {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Set<net.minecraft.resources.ResourceLocation> MISSING = ConcurrentHashMap.newKeySet();
  private CompatModels() {}

  public static void register() {
    ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, models) -> {
      CompatModules.reload();
      MISSING.clear();
      if (!CompatAssets.installed()) {
        return;
      }
      CompatModules.items().values().stream().map(CompatModules.Entry::overlayModel)
          .distinct().forEach(models);
    });
  }

  public static BakedModel select(ItemStack stack, BakedModel original) {
    CompatModules.Entry entry = CompatModules.find(stack.getItem());
    if (entry == null || !CompatUpgrade.isUpgraded(stack)) {
      return original;
    }
    BakedModel overlay = ((ModelManagerAccessor) Minecraft.getInstance().getModelManager())
        .amethysttoolsmod$bakedModels().get(entry.overlayModel());
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
