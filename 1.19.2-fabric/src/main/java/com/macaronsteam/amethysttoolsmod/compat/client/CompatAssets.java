package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.mixin.client.PackRepositoryAccessor;
import java.util.HashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

public final class CompatAssets {
  private static volatile boolean installed;
  private CompatAssets() {}

  public static boolean installed() { return installed; }

  public static void register() {
    ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
      PackRepositoryAccessor repository = (PackRepositoryAccessor) client.getResourcePackRepository();
      HashSet<RepositorySource> sources = new HashSet<>(repository.amethysttoolsmod$sources());
      sources.add((consumer, constructor) -> {
        Pack pack = Pack.create("amethysttoolsmod_compat_assets", true, CompatAssetPack::new,
            constructor, Pack.Position.TOP, PackSource.BUILT_IN);
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
