/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.event;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = AmethystToolsMod.MODID, bus = Bus.MOD)
public class ModEvents {
  @SubscribeEvent
  @OnlyIn(Dist.CLIENT)
  public static void onColorHandlerInit(ColorHandlerEvent.Item event) {
    ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.ifPresent(item -> event.getItemColors().register((stack, layer) -> layer == 0 ? -1 : PotionUtils.getColor(stack), item));
  }
}
