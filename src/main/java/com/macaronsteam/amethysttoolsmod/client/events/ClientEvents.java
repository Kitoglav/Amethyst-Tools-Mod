package com.macaronsteam.amethysttoolsmod.client.events;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystArrowRenderer;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentBEWLR;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentRenderer;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD, modid = AmethystToolsMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

  @SubscribeEvent
  public static void addReloadListenerEvent(RegisterClientReloadListenersEvent event) {
    if (ItemsInit.ITEM_AMETHYST_TRIDENT.isPresent())
      event.registerReloadListener(AmethystTridentBEWLR.INSTANCE);
  }

  @OnlyIn(Dist.CLIENT)
  public static void doClientWork() {
    EntitiesInit.ENTITY_AMETHYST_ARROW.ifPresent(entity -> EntityRenderers.register(entity, ctx -> new AmethystArrowRenderer(ctx, "textures/models/amethyst_arrow.png")));
    EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.ifPresent(entity -> EntityRenderers.register(entity, ctx -> new AmethystArrowRenderer(ctx, "textures/models/amethyst_spectral_arrow.png")));
    EntitiesInit.ENTITY_AMETHYST_TRIDENT.ifPresent(entity -> EntityRenderers.register(entity, AmethystTridentRenderer::new));
    ItemsInit.ITEM_AMETHYST_TRIDENT.ifPresent(item -> ItemProperties.register(item, new ResourceLocation("throwing"), (itemstack, level, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack ? 1.0F : 0.0F));
  }
}
