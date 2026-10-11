package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystTrident;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin implements AmethystTrident {
  @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 8.0F))
  private float amethysttoolsmod$applyProjectileDamage(float baseDamage) {
    return amethysttoolsmod$isAmethyst()
        ? baseDamage + AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get().floatValue()
        : baseDamage;
  }

  @Override
  public boolean amethysttoolsmod$isAmethyst() {
    return ((ThrownTrident) (Object) this).getWeaponItem().is(ItemsInit.AMETHYST_TRIDENT.get());
  }
}
