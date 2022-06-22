/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod;

import com.macaronsteam.amethysttoolsmod.client.events.ClientEvents;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AmethystToolsMod.MODID)
public class AmethystToolsMod {
  public static final String MODID = "amethysttoolsmod";

  public AmethystToolsMod() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    AmethystToolsModConfig.setup();
    bus.addListener(this::setup);
    bus.addListener(this::setupClient);
    EntitiesInit.ENTITIES.register(bus);
    EntitiesInit.register();
    ItemsInit.ITEMS.register(bus);
    ItemsInit.register();
    RecipesInit.RECIPES.register(bus);
    RecipesInit.register();
  }

  private void setup(FMLCommonSetupEvent event) {
    event.enqueueWork(ItemsInit::registerBehavior);
  }

  private void setupClient(FMLClientSetupEvent event) {
    event.enqueueWork(ClientEvents::doClientWork);
  }
}
