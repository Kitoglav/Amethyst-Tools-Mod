package com.macaronsteam.amethysttoolsmod.mixin;

import com.google.common.collect.Multimap;
import com.macaronsteam.amethysttoolsmod.compat.CompatStats;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.macaronsteam.amethysttoolsmod.compat.CompatPresentation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies configured bonuses only to stacks marked by the compat smithing recipe. */
@Mixin(ItemStack.class)
public abstract class CompatItemStackMixin {
  @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$upgradedName(CallbackInfoReturnable<Component> result) {
    result.setReturnValue(CompatPresentation.name((ItemStack) (Object) this, result.getReturnValue()));
  }

  @Shadow public abstract Item getItem();

  private boolean amethysttoolsmod$isCompatUpgraded() {
    return CompatStats.applies((ItemStack) (Object) this);
  }

  @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$maxDamage(CallbackInfoReturnable<Integer> result) {
    if (amethysttoolsmod$isCompatUpgraded()) {
      result.setReturnValue(CompatStats.maxDamage(result.getReturnValue()));
    }
  }

  @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$miningSpeed(BlockState state, CallbackInfoReturnable<Float> result) {
    if (amethysttoolsmod$isCompatUpgraded() && result.getReturnValue() > 1.0F) {
      result.setReturnValue(result.getReturnValue() + AmethystToolsModConfig.EXTRA_DIG_SPEED.get().floatValue());
    }
  }

  @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$attributes(EquipmentSlot slot,
      CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> result) {
    if (amethysttoolsmod$isCompatUpgraded()) {
      result.setReturnValue(CompatStats.attributes(result.getReturnValue(), slot));
    }
  }

}
