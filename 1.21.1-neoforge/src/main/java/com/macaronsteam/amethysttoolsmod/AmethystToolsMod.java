package com.macaronsteam.amethysttoolsmod;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import java.io.IOException;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(AmethystToolsMod.MODID)
public final class AmethystToolsMod {
  public static final String MODID = "amethysttoolsmod";

  public AmethystToolsMod(IEventBus modBus) {
    try {
      AmethystToolsModConfig.load();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to load Amethyst Tools config", exception);
    }

    EntitiesInit.register(modBus);
    ItemsInit.register(modBus);
    RecipesInit.register(modBus);
    NeoForge.EVENT_BUS.addListener(CompatModules::onAddReloadListener);
    CompatModules.reload();
  }
}
