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
  private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);

  public static int getCustomColor(ItemStack itemstack) {
    CompoundTag tag = itemstack.getTag();
    return tag != null && tag.contains("CustomPotionColor", 99) ? tag.getInt("CustomPotionColor") : -1;
  }

  private final Set<MobEffectInstance> effects = new HashSet<>();
  private boolean fixedColor;

  private Potion potion = Potions.EMPTY;

  public AmethystArrowEntity(EntityType<? extends AmethystArrowEntity> entitytype, Level level) {
    super(entitytype, level);
  }

  public AmethystArrowEntity(Level level, double x, double y, double z) {
    super(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), x, y, z, level);
  }

  public AmethystArrowEntity(Level level, ItemStack itemstack, LivingEntity livingentity) {
    this(level, livingentity);
    setEffectsFromItem(itemstack);
  }

  public AmethystArrowEntity(Level level, LivingEntity livingentity) {
    super(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), livingentity, level);
    setBaseDamage(getBaseDamage() + AmethystToolsModConfig.arrowExtraDamage.get());
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundtag) {
    super.addAdditionalSaveData(compoundtag);
    if (this.potion != Potions.EMPTY) {
      compoundtag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
    }
    if (this.fixedColor) {
      compoundtag.putInt("Color", this.getColor());
    }
    if (!this.effects.isEmpty()) {
      ListTag listtag = new ListTag();
      for (MobEffectInstance mobeffectinstance : this.effects) {
        listtag.add(mobeffectinstance.save(new CompoundTag()));
      }
      compoundtag.put("CustomPotionEffects", listtag);
    }

  }

  public void addEffect(MobEffectInstance mobeffectinstance) {
    this.effects.add(mobeffectinstance);
    this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(ID_EFFECT_COLOR, -1);
  }

  @Override
  protected void doPostHurtEffects(LivingEntity livingentity) {
    super.doPostHurtEffects(livingentity);
    Entity entity = this.getEffectSource();
    for (MobEffectInstance mobeffectinstance : this.potion.getEffects()) {
      livingentity.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), Math.max(mobeffectinstance.getDuration() / 8, 1), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()), entity);
    }
    if (!this.effects.isEmpty()) {
      for (MobEffectInstance mobeffectinstance1 : this.effects) {
        livingentity.addEffect(mobeffectinstance1, entity);
      }
    }
  }

  public int getColor() {
    return this.entityData.get(ID_EFFECT_COLOR);
  }

  @Override
  protected ItemStack getPickupItem() {
    if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
      return new ItemStack(ItemsInit.ITEM_AMETHYST_ARROW.get());
    } else {
      ItemStack itemstack = new ItemStack(ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.get());
      PotionUtils.setPotion(itemstack, this.potion);
      PotionUtils.setCustomEffects(itemstack, this.effects);
      if (this.fixedColor) {
        itemstack.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
      }
      return itemstack;
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

  @Override
  public void readAdditionalSaveData(CompoundTag compoundtag) {
    super.readAdditionalSaveData(compoundtag);
    if (compoundtag.contains("Potion", 8)) {
      this.potion = PotionUtils.getPotion(compoundtag);
    }
    for (MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(compoundtag)) {
      this.addEffect(mobeffectinstance);
    }
    if (compoundtag.contains("Color", 99)) {
      this.setFixedColor(compoundtag.getInt("Color"));
    } else {
      this.updateColor();
    }

  }

  public void setEffectsFromItem(ItemStack itemstack) {
    if (itemstack.is(ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.get())) {
      this.potion = PotionUtils.getPotion(itemstack);
      Collection<MobEffectInstance> collection = PotionUtils.getCustomEffects(itemstack);
      if (!collection.isEmpty()) {
        for (MobEffectInstance mobeffectinstance : collection) {
          this.effects.add(new MobEffectInstance(mobeffectinstance));
        }
      }
      int i = getCustomColor(itemstack);
      if (i == -1) {
        this.updateColor();
      } else {
        this.setFixedColor(i);
      }
    } else if (itemstack.is(ItemsInit.ITEM_AMETHYST_ARROW.get())) {
      this.potion = Potions.EMPTY;
      this.effects.clear();
      this.entityData.set(ID_EFFECT_COLOR, -1);
    }
  }

  private void setFixedColor(int color) {
    this.fixedColor = true;
    this.entityData.set(ID_EFFECT_COLOR, color);
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

  private void updateColor() {
    this.fixedColor = false;
    if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
      this.entityData.set(ID_EFFECT_COLOR, -1);
    } else {
      this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

  }
}
