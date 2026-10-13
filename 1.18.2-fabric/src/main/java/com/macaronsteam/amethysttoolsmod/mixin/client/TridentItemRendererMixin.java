package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.client.TridentModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class TridentItemRendererMixin {
  // Choose the GUI model before vanilla inspects usesBlockLight(), not only during rendering.
  @ModifyVariable(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
      at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private BakedModel amethysttoolsmod$guiLightingModel(BakedModel original, ItemStack stack,
      int x, int y, BakedModel model) {
    return TridentModels.select(stack, true, original);
  }

  @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private BakedModel amethysttoolsmod$inventoryModel(BakedModel original, ItemStack stack,
      ItemTransforms.TransformType context, boolean leftHand, PoseStack poses, MultiBufferSource buffers,
      int light, int overlay, BakedModel model) {
    boolean flat = context == ItemTransforms.TransformType.GUI || context == ItemTransforms.TransformType.GROUND
        || context == ItemTransforms.TransformType.FIXED;
    return TridentModels.select(stack, flat, original);
  }
}
