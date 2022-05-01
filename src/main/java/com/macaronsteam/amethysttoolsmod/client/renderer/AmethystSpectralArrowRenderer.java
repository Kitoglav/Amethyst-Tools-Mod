/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class AmethystSpectralArrowRenderer extends ArrowRenderer<AmethystSpectralArrowEntity> {

  public AmethystSpectralArrowRenderer(Context ctx) {
    super(ctx);
  }

  @Override
  public ResourceLocation getTextureLocation(AmethystSpectralArrowEntity entity) {
    return new ResourceLocation(AmethystToolsMod.MODID, "textures/models/amethyst_spectral_arrow.png");
  }
}
