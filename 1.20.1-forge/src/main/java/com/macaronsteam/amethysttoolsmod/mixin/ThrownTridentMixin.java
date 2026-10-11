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
import org.spongepowered.asm.mixin.Shadow;
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

  @Shadow
  private ItemStack tridentItem;

  @Inject(method = "defineSynchedData", at = @At("TAIL"))
  private void amethysttoolsmod$defineAmethystFlag(CallbackInfo callback) {
    ((ThrownTrident) (Object) this).getEntityData().define(AMETHYST, false);
  }

  @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
  private void amethysttoolsmod$rememberThrownItem(Level level, LivingEntity owner, ItemStack stack, CallbackInfo callback) {
    ((ThrownTrident) (Object) this).getEntityData().set(AMETHYST, stack.is(ItemsInit.AMETHYST_TRIDENT.get()));
  }

  @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
  private void amethysttoolsmod$restoreAmethystFlag(CompoundTag tag, CallbackInfo callback) {
    ((ThrownTrident) (Object) this).getEntityData().set(AMETHYST, tridentItem.is(ItemsInit.AMETHYST_TRIDENT.get()));
  }

  @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 8.0F))
  private float amethysttoolsmod$applyProjectileDamage(float baseDamage) {
    return tridentItem.is(ItemsInit.AMETHYST_TRIDENT.get())
        ? baseDamage + AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get().floatValue()
        : baseDamage;
  }

  @Override
  public boolean amethysttoolsmod$isAmethyst() {
    return ((ThrownTrident) (Object) this).getEntityData().get(AMETHYST);
  }
}
