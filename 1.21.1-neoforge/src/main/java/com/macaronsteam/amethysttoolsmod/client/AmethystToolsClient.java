package com.macaronsteam.amethysttoolsmod.client;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystArrowRenderer;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentRenderer;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = AmethystToolsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AmethystToolsClient {
  private AmethystToolsClient() {
  }

  @SubscribeEvent
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(EntitiesInit.ENTITY_AMETHYST_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_arrow.png"));
    event.registerEntityRenderer(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_spectral_arrow.png"));
  }

  @SubscribeEvent
  public static void registerColors(RegisterColorHandlersEvent.Item event) {
    event.register((stack, layer) -> layer == 0 ? -1
        : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor(),
        ItemsInit.AMETHYST_TIPPED_ARROW.get());
  }

  @SubscribeEvent
  public static void registerExtensions(RegisterClientExtensionsEvent event) {
    event.registerItem(new IClientItemExtensions() {
      @Override
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return AmethystTridentRenderer.INSTANCE;
      }
    }, ItemsInit.AMETHYST_TRIDENT.get());
  }

  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> ItemProperties.register(ItemsInit.AMETHYST_TRIDENT.get(),
        ResourceLocation.withDefaultNamespace("throwing"),
        (stack, level, user, seed) -> user != null && user.isUsingItem() && user.getUseItem() == stack ? 1.0F : 0.0F));
  }
}
