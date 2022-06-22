package com.macaronsteam.amethysttoolsmod.entity;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ThrownAmethystTrident extends ThrownTrident {

  public ThrownAmethystTrident(Level level) {
    super(EntitiesInit.ENTITY_AMETHYST_TRIDENT.get(), level);
  }

  public ThrownAmethystTrident(Level level, double x, double y, double z) {
    this(level);
    this.setPos(x, y, z);
  }

  public ThrownAmethystTrident(Level level, LivingEntity livingentity) {
    this(level, livingentity.getX(), livingentity.getEyeY() - 0.1F, livingentity.getZ());
    this.setOwner(livingentity);
    if (livingentity instanceof Player) {
      this.pickup = AbstractArrow.Pickup.ALLOWED;
    }
  }

  public ThrownAmethystTrident(Level level, LivingEntity livingentity, ItemStack itemstack) {
    this(level, livingentity);
    ObfuscationReflectionHelper.setPrivateValue(ThrownTrident.class, this, itemstack.copy(), "f_37555_");
    this.entityData.set(ObfuscationReflectionHelper.getPrivateValue(ThrownTrident.class, this, "f_37558_"), (byte) EnchantmentHelper.getLoyalty(itemstack));
    this.entityData.set(ObfuscationReflectionHelper.getPrivateValue(ThrownTrident.class, this, "f_37554_"), itemstack.hasFoil());
  }

  @Override
  protected void onHitEntity(EntityHitResult hitresult) {
    ItemStack itemstack = ObfuscationReflectionHelper.getPrivateValue(ThrownTrident.class, this, "f_37555_");
    Entity entity = hitresult.getEntity();
    float f = 8.0F + AmethystToolsModConfig.arrowExtraDamage.get().floatValue();
    if (entity instanceof LivingEntity livingentity) {
      f += EnchantmentHelper.getDamageBonus(itemstack, livingentity.getMobType());
    }

    Entity entity1 = this.getOwner();
    DamageSource damagesource = DamageSource.trident(this, entity1 == null ? this : entity1);
    ObfuscationReflectionHelper.setPrivateValue(ThrownTrident.class, this, true, "f_37556_");
    SoundEvent soundevent = SoundEvents.TRIDENT_HIT;
    if (entity.hurt(damagesource, f)) {
      if (entity.getType() == EntityType.ENDERMAN) {
        return;
      }

      if (entity instanceof LivingEntity) {
        LivingEntity livingentity = (LivingEntity) entity;
        if (entity1 instanceof LivingEntity) {
          EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
          EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
        }

        this.doPostHurtEffects(livingentity);
      }
    }

    this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
    float f1 = 1.0F;
    if (this.level instanceof ServerLevel && this.level.isThundering() && this.isChanneling()) {
      BlockPos blockpos = entity.blockPosition();
      if (this.level.canSeeSky(blockpos)) {
        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(this.level);
        lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
        lightningbolt.setCause(entity1 instanceof ServerPlayer ? (ServerPlayer) entity1 : null);
        this.level.addFreshEntity(lightningbolt);
        soundevent = SoundEvents.TRIDENT_THUNDER;
        f1 = 5.0F;
      }
    }

    this.playSound(soundevent, f1, 1.0F);
  }
}
