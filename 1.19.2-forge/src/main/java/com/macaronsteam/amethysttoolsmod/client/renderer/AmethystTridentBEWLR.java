package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class AmethystTridentBEWLR extends BlockEntityWithoutLevelRenderer {

  public static final AmethystTridentBEWLR INSTANCE = new AmethystTridentBEWLR();
  private static final ResourceLocation AMETHYST_TEXTURE =
      new ResourceLocation(AmethystToolsMod.MODID, "textures/models/amethyst_trident.png");
  private TridentModel tridentModel;

  public AmethystTridentBEWLR() {
    super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourcemanager) {
    this.tridentModel = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
  }

  @Override
  public void renderByItem(ItemStack stack, TransformType transform, PoseStack poseStack,
      MultiBufferSource buffers, int light, int overlay) {
    poseStack.pushPose();
    poseStack.scale(1.0F, -1.0F, -1.0F);
    VertexConsumer vertices = ItemRenderer.getFoilBufferDirect(
        buffers, tridentModel.renderType(AMETHYST_TEXTURE), false, stack.hasFoil());
    tridentModel.renderToBuffer(poseStack, vertices, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    poseStack.popPose();
  }
}
