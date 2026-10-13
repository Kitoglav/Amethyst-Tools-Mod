package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.client.TridentModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class TridentItemRendererMixin {
  @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private BakedModel amethysttoolsmod$inventoryModel(BakedModel original, ItemStack stack,
      ItemDisplayContext context, boolean leftHand, PoseStack poses, MultiBufferSource buffers,
      int light, int overlay, BakedModel model) {
    boolean flat = context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND
        || context == ItemDisplayContext.FIXED;
    return TridentModels.select(stack, flat, original);
  }
}
