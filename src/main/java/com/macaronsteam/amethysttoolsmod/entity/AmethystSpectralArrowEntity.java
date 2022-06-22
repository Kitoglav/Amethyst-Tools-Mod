/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.entity;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AmethystSpectralArrowEntity extends AbstractArrow {

  public AmethystSpectralArrowEntity(EntityType<? extends AmethystSpectralArrowEntity> entitytype, Level level) {
    super(entitytype, level);
  }

  public AmethystSpectralArrowEntity(Level level, double x, double y, double z) {
    super(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), x, y, z, level);
  }

  public AmethystSpectralArrowEntity(Level level, LivingEntity livingentity) {
    super(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), livingentity, level);
    setBaseDamage(getBaseDamage() + AmethystToolsModConfig.arrowExtraDamage.get());
  }

  @Override
  protected void doPostHurtEffects(LivingEntity livingentity) {
    super.doPostHurtEffects(livingentity);
    MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.GLOWING, AmethystToolsModConfig.glowingDuration.get(), 0);
    livingentity.addEffect(mobeffectinstance, this.getEffectSource());
  }

  @Override
  protected ItemStack getPickupItem() {
    return new ItemStack(ItemsInit.ITEM_AMETHYST_SPECTRAL_ARROW.get());
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level.isClientSide && !this.inGround) {
      this.level.addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }
  }
}
