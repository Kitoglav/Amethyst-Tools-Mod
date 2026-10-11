package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.Optional;
import org.slf4j.Logger;

@EventBusSubscriber(modid = AmethystToolsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CompatClient {
  private static final Logger LOGGER = LogUtils.getLogger();

  private CompatClient() {}

  @SubscribeEvent
  public static void onAddPackFinders(AddPackFindersEvent event) {
    if (event.getPackType() != PackType.CLIENT_RESOURCES) {
      return;
    }
    event.addRepositorySource(consumer -> {
      PackLocationInfo location = new PackLocationInfo("amethysttoolsmod_compat_assets",
          Component.literal("Amethyst Tools compatibility assets"), PackSource.BUILT_IN, Optional.empty());
      Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
        @Override public net.minecraft.server.packs.PackResources openPrimary(PackLocationInfo ignored) { return new CompatAssetPack(location); }
        @Override public net.minecraft.server.packs.PackResources openFull(PackLocationInfo ignored, Pack.Metadata metadata) { return new CompatAssetPack(location); }
      };
      Pack pack = Pack.readMetaAndCreate(location, resources, PackType.CLIENT_RESOURCES,
          new PackSelectionConfig(true, Pack.Position.TOP, true));
      if (pack != null) {
        consumer.accept(pack);
      }
    });
  }

  @SubscribeEvent
  public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
    CompatModules.reload();
    CompatModules.items().values().stream().map(CompatModules.Entry::overlayModel)
        .distinct().map(ModelResourceLocation::standalone).forEach(event::register);
  }

  @SubscribeEvent
  public static void onBakeModels(ModelEvent.ModifyBakingResult event) {
    CompatModules.items().values().forEach(entry -> {
      ModelResourceLocation itemModel = new ModelResourceLocation(entry.id(), "inventory");
      BakedModel base = event.getModels().get(itemModel);
      BakedModel overlay = event.getModels().get(ModelResourceLocation.standalone(entry.overlayModel()));
      if (base == null || overlay == null) {
        LOGGER.error("Amethyst Tools compat: module '{}' in {} has missing base or overlay model for {}",
            entry.moduleName(), entry.file(), entry.id());
        return;
      }
      event.getModels().put(itemModel, new CompatItemModel(base, overlay, entry));
    });
  }
}
