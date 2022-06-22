package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class AmethystTridentBEWLR extends BlockEntityWithoutLevelRenderer {

  public static final AmethystTridentBEWLR INSTANCE = new AmethystTridentBEWLR();
  private TridentModel tridentModel;

  public AmethystTridentBEWLR() {
    super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourcemanager) {
    this.tridentModel = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
  }

  @Override
  public void renderByItem(ItemStack itemstack, TransformType transformtype, PoseStack posestack, MultiBufferSource multibuffersource, int i1, int i2) {
    if (itemstack.is(ItemsInit.ITEM_AMETHYST_TRIDENT.get())) {
      posestack.pushPose();
      posestack.scale(1.0F, -1.0F, -1.0F);
      VertexConsumer vertexconsumer1 = ItemRenderer.getFoilBufferDirect(multibuffersource, this.tridentModel.renderType(AmethystTridentRenderer.TEXTURE), false, itemstack.hasFoil());
      this.tridentModel.renderToBuffer(posestack, vertexconsumer1, i1, i2, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
    } else {
      super.renderByItem(itemstack, transformtype, posestack, multibuffersource, i1, i2);
    }
  }
}
