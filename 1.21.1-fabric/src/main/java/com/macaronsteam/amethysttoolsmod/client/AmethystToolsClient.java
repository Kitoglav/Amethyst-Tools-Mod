package com.macaronsteam.amethysttoolsmod.client;

import com.macaronsteam.amethysttoolsmod.compat.client.CompatModels;
import com.macaronsteam.amethysttoolsmod.compat.client.CompatAssets;

import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystArrowRenderer;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentRenderer;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;

public final class AmethystToolsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    TridentModels.register();
    CompatModels.register();
    CompatAssets.register();
    EntityRendererRegistry.register(EntitiesInit.ENTITY_AMETHYST_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_arrow.png"));
    EntityRendererRegistry.register(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(),
        context -> new AmethystArrowRenderer<>(context, "textures/models/amethyst_spectral_arrow.png"));

    ColorProviderRegistry.ITEM.register(
        (stack, layer) -> layer == 0 ? -1 : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor(),
        ItemsInit.AMETHYST_TIPPED_ARROW.get());
    BuiltinItemRendererRegistry.INSTANCE.register(
        ItemsInit.AMETHYST_TRIDENT.get(), new AmethystTridentRenderer());
    ItemProperties.register(ItemsInit.AMETHYST_TRIDENT.get(), ResourceLocation.withDefaultNamespace("throwing"),
        (stack, level, user, seed) -> user != null && user.isUsingItem() && user.getUseItem() == stack ? 1.0F : 0.0F);
  }
}
