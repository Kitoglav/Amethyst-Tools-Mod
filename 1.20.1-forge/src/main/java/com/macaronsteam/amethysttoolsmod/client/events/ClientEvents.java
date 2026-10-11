package com.macaronsteam.amethysttoolsmod.client.events;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystArrowRenderer;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentBEWLR;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD, modid = AmethystToolsMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {

  private ClientEvents() {
  }

  @SubscribeEvent
  public static void addReloadListenerEvent(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(AmethystTridentBEWLR.INSTANCE);
  }

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(ClientEvents::registerRenderersAndProperties);
  }

  private static void registerRenderersAndProperties() {
    EntityRenderers.register(EntitiesInit.ENTITY_AMETHYST_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_arrow.png"));
    EntityRenderers.register(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_spectral_arrow.png"));
    ItemProperties.register(ItemsInit.AMETHYST_TRIDENT.get(), new ResourceLocation("throwing"),
        (stack, level, user, seed) -> user != null && user.isUsingItem() && user.getUseItem() == stack ? 1.0F : 0.0F);
  }
}
