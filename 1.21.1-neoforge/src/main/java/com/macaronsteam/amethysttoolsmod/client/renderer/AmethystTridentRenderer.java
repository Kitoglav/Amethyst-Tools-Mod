package com.macaronsteam.amethysttoolsmod.client.renderer;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class AmethystTridentRenderer extends BlockEntityWithoutLevelRenderer {
  public static final AmethystTridentRenderer INSTANCE = new AmethystTridentRenderer();
  private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
      AmethystToolsMod.MODID, "textures/models/amethyst_trident.png");
  private TridentModel model;

  private AmethystTridentRenderer() {
    super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void onResourceManagerReload(ResourceManager resources) {
    model = new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
  }

  @Override
  public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
      MultiBufferSource buffers, int light, int overlay) {
    if (model == null) {
      onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }
    poseStack.pushPose();
    poseStack.scale(1.0F, -1.0F, -1.0F);
    VertexConsumer vertices = ItemRenderer.getFoilBufferDirect(
        buffers, model.renderType(TEXTURE), false, stack.hasFoil());
    model.renderToBuffer(poseStack, vertices, light, overlay);
    poseStack.popPose();
  }
}
