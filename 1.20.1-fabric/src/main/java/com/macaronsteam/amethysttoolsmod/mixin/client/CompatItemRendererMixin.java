package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.compat.client.CompatModels;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class CompatItemRendererMixin {
  @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$compatModel(ItemStack stack, Level level, LivingEntity holder, int seed,
      CallbackInfoReturnable<BakedModel> result) {
    result.setReturnValue(CompatModels.select(stack, result.getReturnValue()));
  }
}
