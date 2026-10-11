package com.macaronsteam.amethysttoolsmod;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import java.io.IOException;
import net.fabricmc.api.ModInitializer;

public final class AmethystToolsMod implements ModInitializer {
  public static final String MODID = "amethysttoolsmod";

  @Override
  public void onInitialize() {
    try {
      AmethystToolsModConfig.load();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to load Amethyst Tools config", exception);
    }

    EntitiesInit.register();
    ItemsInit.register();
    RecipesInit.register();
    CompatModules.registerReloadListener();
    CompatModules.reload();
  }
}
