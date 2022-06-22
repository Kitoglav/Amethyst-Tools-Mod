/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class AmethystArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T> {
  public final String texture;

  public AmethystArrowRenderer(Context context, String path) {
    super(context);
    this.texture = path;
  }

  @Override
  public ResourceLocation getTextureLocation(T entity) {
    return new ResourceLocation(AmethystToolsMod.MODID, texture);
  }
}
