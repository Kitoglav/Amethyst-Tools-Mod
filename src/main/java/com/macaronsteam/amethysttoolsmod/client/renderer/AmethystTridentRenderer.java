package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrownTrident;

public class AmethystTridentRenderer extends ThrownTridentRenderer {
  public static final ResourceLocation TEXTURE = new ResourceLocation(AmethystToolsMod.MODID, "textures/models/amethyst_trident.png");

  public AmethystTridentRenderer(Context context) {
    super(context);
  }

  @Override
  public ResourceLocation getTextureLocation(ThrownTrident trident) {
    return TEXTURE;
  }
}
