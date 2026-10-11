package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class CompatItemStackMixin {
  @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$compatMiningSpeed(BlockState state, CallbackInfoReturnable<Float> result) {
    ItemStack stack = (ItemStack) (Object) this;
    if (CompatUpgrade.isUpgraded(stack) && CompatModules.find(stack.getItem()) != null
        && result.getReturnValue() > 1.0F) {
      result.setReturnValue(result.getReturnValue() + AmethystToolsModConfig.EXTRA_DIG_SPEED.get().floatValue());
    }
  }
}
