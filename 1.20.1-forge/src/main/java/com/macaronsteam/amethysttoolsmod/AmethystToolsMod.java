/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.DefaultCompatPack;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AmethystToolsMod.MODID)
public class AmethystToolsMod {
  public static final String MODID = "amethysttoolsmod";

  public AmethystToolsMod() {
    DefaultCompatPack.installIfMissing();
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AmethystToolsModConfig.SPEC);
    AmethystToolsModConfig.loadStartupValues();
    MinecraftForge.EVENT_BUS.addListener(CompatModules::onAddReloadListener);
    modBus.addListener(this::onCommonSetup);
    modBus.addListener(ItemsInit::addToCreativeTabs);
    EntitiesInit.ENTITIES.register(modBus);
    ItemsInit.ITEMS.register(modBus);
    ItemsInit.registerEquipment();
    RecipesInit.RECIPES.register(modBus);
    RecipesInit.register();
  }

  private void onCommonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(ItemsInit::registerBehavior);
  }
}
