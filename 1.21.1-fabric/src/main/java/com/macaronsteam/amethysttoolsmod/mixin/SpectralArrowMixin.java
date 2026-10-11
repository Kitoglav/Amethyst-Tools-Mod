package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpectralArrow.class)
public abstract class SpectralArrowMixin {
  @Shadow
  private int duration;

  @Inject(method = "doPostHurtEffects", at = @At("HEAD"))
  private void amethysttoolsmod$applyConfiguredGlow(LivingEntity target, CallbackInfo callback) {
    SpectralArrow arrow = (SpectralArrow) (Object) this;
    if (arrow.getType() == EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get()) {
      duration = AmethystToolsModConfig.GLOWING_DURATION.get();
    }
  }

  @Inject(method = "getDefaultPickupItem", at = @At("HEAD"), cancellable = true)
  private void amethysttoolsmod$restoreAmethystPickup(CallbackInfoReturnable<ItemStack> callback) {
    SpectralArrow arrow = (SpectralArrow) (Object) this;
    if (arrow.getType() == EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get()) {
      callback.setReturnValue(new ItemStack(ItemsInit.AMETHYST_SPECTRAL_ARROW.get()));
    }
  }
}
