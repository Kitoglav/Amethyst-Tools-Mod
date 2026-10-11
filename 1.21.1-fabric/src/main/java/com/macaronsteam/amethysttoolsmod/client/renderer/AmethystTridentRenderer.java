package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class AmethystTridentRenderer implements BuiltinItemRenderer {
  private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
      AmethystToolsMod.MODID, "textures/models/amethyst_trident.png");
  private TridentModel model;

  @Override
  public void render(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
    if (model == null) {
      model = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
    }

    poseStack.pushPose();
    poseStack.scale(1.0F, -1.0F, -1.0F);
    VertexConsumer vertices = ItemRenderer.getFoilBufferDirect(
        buffers, model.renderType(TEXTURE), false, stack.hasFoil());
    model.renderToBuffer(poseStack, vertices, light, overlay);
    poseStack.popPose();
  }
}
