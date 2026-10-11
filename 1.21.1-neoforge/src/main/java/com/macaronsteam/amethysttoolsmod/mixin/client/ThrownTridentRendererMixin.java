package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.entity.AmethystTrident;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTridentRenderer.class)
public abstract class ThrownTridentRendererMixin {
  private static final ResourceLocation AMETHYST_TEXTURE = ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "textures/models/amethyst_trident.png");

  @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/projectile/ThrownTrident;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
  private void amethysttoolsmod$useAmethystTexture(ThrownTrident trident, CallbackInfoReturnable<ResourceLocation> callback) {
    if (((AmethystTrident) trident).amethysttoolsmod$isAmethyst()) {
      callback.setReturnValue(AMETHYST_TEXTURE);
    }
  }
}
