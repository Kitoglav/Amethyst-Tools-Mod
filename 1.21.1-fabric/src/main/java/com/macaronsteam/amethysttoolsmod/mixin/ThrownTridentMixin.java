package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystTrident;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin implements AmethystTrident {
  @Unique
  private static final EntityDataAccessor<Boolean> AMETHYST =
      SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BOOLEAN);

  @Inject(method = "defineSynchedData", at = @At("TAIL"))
  private void amethysttoolsmod$defineAmethystFlag(SynchedEntityData.Builder builder, CallbackInfo callback) {
    builder.define(AMETHYST, false);
  }

  @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
  private void amethysttoolsmod$rememberThrownItem(Level level, LivingEntity owner, ItemStack stack, CallbackInfo callback) {
    amethysttoolsmod$setAmethyst(stack);
  }

  @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
  private void amethysttoolsmod$rememberDispensedItem(Level level, double x, double y, double z, ItemStack stack,
      CallbackInfo callback) {
    amethysttoolsmod$setAmethyst(stack);
  }

  @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
  private void amethysttoolsmod$restoreAmethystFlag(CompoundTag tag, CallbackInfo callback) {
    amethysttoolsmod$setAmethyst(((ThrownTrident) (Object) this).getWeaponItem());
  }

  @Unique
  private void amethysttoolsmod$setAmethyst(ItemStack stack) {
    ((ThrownTrident) (Object) this).getEntityData().set(AMETHYST, stack.is(ItemsInit.AMETHYST_TRIDENT.get()));
  }

  @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 8.0F))
  private float amethysttoolsmod$applyProjectileDamage(float baseDamage) {
    return amethysttoolsmod$isAmethyst()
        ? baseDamage + AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get().floatValue() : baseDamage;
  }

  @Override
  public boolean amethysttoolsmod$isAmethyst() {
    return ((ThrownTrident) (Object) this).getEntityData().get(AMETHYST);
  }
}
