/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.entities.AmethystArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class AmethystArrowRenderer extends ArrowRenderer<AmethystArrowEntity> {

  public AmethystArrowRenderer(Context ctx) {
    super(ctx);
  }

  @Override
  public ResourceLocation getTextureLocation(AmethystArrowEntity entity) {
    return new ResourceLocation(AmethystToolsMod.MODID, "textures/models/amethyst_arrow.png");
  }
}
