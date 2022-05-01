/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.entity;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AmethystSpectralArrowEntity extends AbstractArrow {
  private int duration = 300;

  public AmethystSpectralArrowEntity(EntityType<? extends AmethystSpectralArrowEntity> type, Level level) {
    super(type, level);
  }

  public AmethystSpectralArrowEntity(Level level, LivingEntity living) {
    super(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), living, level);
    setBaseDamage(getBaseDamage() + AmethystToolsModConfig.arrowExtraDamage.get());
  }

  public AmethystSpectralArrowEntity(Level level, double x, double y, double z) {
    super(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), x, y, z, level);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level.isClientSide && !this.inGround) {
      this.level.addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }
  }

  @Override
  protected ItemStack getPickupItem() {
    return new ItemStack(ItemsInit.ITEM_AMETHYST_SPECTRAL_ARROW.get());
  }

  @Override
  protected void doPostHurtEffects(LivingEntity living) {
    super.doPostHurtEffects(living);
    MobEffectInstance mobEffect = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
    living.addEffect(mobEffect, this.getEffectSource());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("Duration")) {
      this.duration = tag.getInt("Duration");
    }
  }

  @Override
  public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putInt("Duration", this.duration);
  }
}
