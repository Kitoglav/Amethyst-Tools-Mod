/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class AmethystArrowEntity extends AbstractArrow {
  private static final int EXPOSED_POTION_DECAY_TIME = 600;
  private static final int NO_EFFECT_COLOR = -1;
  private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
  private static final byte EVENT_POTION_PUFF = 0;
  private Potion potion = Potions.EMPTY;
  private final Set<MobEffectInstance> effects = new HashSet<>();
  private boolean fixedColor;

  public AmethystArrowEntity(EntityType<? extends AmethystArrowEntity> type, Level level) {
    super(type, level);
  }

  public AmethystArrowEntity(Level level, LivingEntity living) {
    super(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), living, level);
    setBaseDamage(getBaseDamage() + AmethystToolsModConfig.arrowExtraDamage.get());
  }

  public AmethystArrowEntity(Level level, double x, double y, double z) {
    super(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), x, y, z, level);
  }

  public void setEffectsFromItem(ItemStack stack) {
    if (stack.is(ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.get())) {
      this.potion = PotionUtils.getPotion(stack);
      Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(stack);
      if (!collection.isEmpty()) {
        for (MobEffectInstance mobEffect : collection) {
          this.effects.add(new MobEffectInstance(mobEffect));
        }
      }
      int i = getCustomColor(stack);
      if (i == -1) {
        this.updateColor();
      } else {
        this.setFixedColor(i);
      }
    } else if (stack.is(ItemsInit.ITEM_AMETHYST_ARROW.get())) {
      this.potion = Potions.EMPTY;
      this.effects.clear();
      this.entityData.set(ID_EFFECT_COLOR, -1);
    }
  }

  public static int getCustomColor(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains("CustomPotionColor", 99) ? tag.getInt("CustomPotionColor") : -1;
  }

  private void updateColor() {
    this.fixedColor = false;
    if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
      this.entityData.set(ID_EFFECT_COLOR, -1);
    } else {
      this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

  }

  public void addEffect(MobEffectInstance mobEffect) {
    this.effects.add(mobEffect);
    this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(ID_EFFECT_COLOR, -1);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level.isClientSide) {
      if (this.inGround) {
        if (this.inGroundTime % 5 == 0) {
          this.makeParticle(1);
        }
      } else {
        this.makeParticle(2);
      }
    } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
      this.level.broadcastEntityEvent(this, (byte) 0);
      this.potion = Potions.EMPTY;
      this.effects.clear();
      this.entityData.set(ID_EFFECT_COLOR, -1);
    }
  }

  private void makeParticle(int d) {
    int i = this.getColor();
    if (i != -1 && d > 0) {
      double d0 = (i >> 16 & 255) / 255.0D;
      double d1 = (i >> 8 & 255) / 255.0D;
      double d2 = (i >> 0 & 255) / 255.0D;
      for (int j = 0; j < d; ++j) {
        this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
      }
    }
  }

  public int getColor() {
    return this.entityData.get(ID_EFFECT_COLOR);
  }

  private void setFixedColor(int p_36883_) {
    this.fixedColor = true;
    this.entityData.set(ID_EFFECT_COLOR, p_36883_);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    if (this.potion != Potions.EMPTY) {
      tag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
    }
    if (this.fixedColor) {
      tag.putInt("Color", this.getColor());
    }
    if (!this.effects.isEmpty()) {
      ListTag list = new ListTag();
      for (MobEffectInstance mobEffect : this.effects) {
        list.add(mobEffect.save(new CompoundTag()));
      }
      tag.put("CustomPotionEffects", list);
    }

  }

  @Override
  public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("Potion", 8)) {
      this.potion = PotionUtils.getPotion(tag);
    }
    for (MobEffectInstance mobEffect : PotionUtils.getCustomEffects(tag)) {
      this.addEffect(mobEffect);
    }
    if (tag.contains("Color", 99)) {
      this.setFixedColor(tag.getInt("Color"));
    } else {
      this.updateColor();
    }

  }

  @Override
  protected void doPostHurtEffects(LivingEntity living) {
    super.doPostHurtEffects(living);
    Entity entity = this.getEffectSource();
    for (MobEffectInstance mobEffect : this.potion.getEffects()) {
      living.addEffect(new MobEffectInstance(mobEffect.getEffect(), Math.max(mobEffect.getDuration() / 8, 1), mobEffect.getAmplifier(), mobEffect.isAmbient(), mobEffect.isVisible()), entity);
    }
    if (!this.effects.isEmpty()) {
      for (MobEffectInstance mobEffect1 : this.effects) {
        living.addEffect(mobEffect1, entity);
      }
    }
  }

  @Override
  protected ItemStack getPickupItem() {
    if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
      return new ItemStack(ItemsInit.ITEM_AMETHYST_ARROW.get());
    } else {
      ItemStack stack = new ItemStack(ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.get());
      PotionUtils.setPotion(stack, this.potion);
      PotionUtils.setCustomEffects(stack, this.effects);
      if (this.fixedColor) {
        stack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
      }
      return stack;
    }
  }

  @Override
  public void handleEntityEvent(byte d) {
    if (d == 0) {
      int i = this.getColor();
      if (i != -1) {
        double d0 = (i >> 16 & 255) / 255.0D;
        double d1 = (i >> 8 & 255) / 255.0D;
        double d2 = (i >> 0 & 255) / 255.0D;
        for (int j = 0; j < 20; ++j) {
          this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
        }
      }
    } else {
      super.handleEntityEvent(d);
    }

  }
}
