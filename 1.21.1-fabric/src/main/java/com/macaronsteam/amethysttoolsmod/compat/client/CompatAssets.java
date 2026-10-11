package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.mixin.client.PackRepositoryAccessor;
import java.util.Optional;
import java.util.HashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

/** Installs config/amethyst_tools/assets as a mandatory reloadable client resource pack. */
public final class CompatAssets {
  private static volatile boolean installed;
  private CompatAssets() {}

  public static boolean installed() { return installed; }

  public static void register() {
    ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
      PackRepositoryAccessor repository = (PackRepositoryAccessor) client.getResourcePackRepository();
      HashSet<RepositorySource> sources = new HashSet<>(repository.amethysttoolsmod$sources());
      sources.add(consumer -> {
            PackLocationInfo location = new PackLocationInfo("amethysttoolsmod_compat_assets",
                Component.literal("Amethyst Tools compatibility assets"), PackSource.BUILT_IN, Optional.empty());
            Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
              @Override public PackResources openPrimary(PackLocationInfo ignored) { return new CompatAssetPack(location); }
              @Override public PackResources openFull(PackLocationInfo ignored, Pack.Metadata metadata) { return new CompatAssetPack(location); }
            };
            Pack pack = Pack.readMetaAndCreate(location, resources, PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(true, Pack.Position.TOP, true));
            if (pack != null) {
              consumer.accept(pack);
            }
          });
      repository.amethysttoolsmod$setSources(sources);
      installed = true;
      client.getResourcePackRepository().reload();
      client.reloadResourcePacks();
    });
  }
}
