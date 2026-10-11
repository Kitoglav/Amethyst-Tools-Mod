package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = AmethystToolsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CompatClient {
  private static final Logger LOGGER = LogUtils.getLogger();

  private CompatClient() {}

  @SubscribeEvent
  public static void onAddPackFinders(AddPackFindersEvent event) {
    if (event.getPackType() != PackType.CLIENT_RESOURCES) {
      return;
    }
    event.addRepositorySource((consumer, constructor) -> {
      Pack pack = Pack.create("amethysttoolsmod_compat_assets", true, CompatAssetPack::new,
          constructor,
          Pack.Position.TOP, PackSource.BUILT_IN);
      if (pack != null) {
        consumer.accept(pack);
      }
    });
  }

  @SubscribeEvent
  public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
    CompatModules.reload();
    CompatModules.items().values().stream().map(CompatModules.Entry::overlayModel)
        .distinct().forEach(event::register);
  }

  @SubscribeEvent
  public static void onBakeModels(ModelEvent.BakingCompleted event) {
    CompatModules.items().values().forEach(entry -> {
      ModelResourceLocation itemModel = new ModelResourceLocation(entry.id(), "inventory");
      BakedModel base = event.getModels().get(itemModel);
      BakedModel overlay = event.getModels().get(entry.overlayModel());
      if (base == null || overlay == null) {
        LOGGER.error("Amethyst Tools compat: module '{}' in {} has missing base or overlay model for {}",
            entry.moduleName(), entry.file(), entry.id());
        return;
      }
      event.getModels().put(itemModel, new CompatItemModel(base, overlay, entry));
    });
  }
}
