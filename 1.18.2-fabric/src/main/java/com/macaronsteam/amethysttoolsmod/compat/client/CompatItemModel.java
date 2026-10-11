package com.macaronsteam.amethysttoolsmod.compat.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

final class CompatItemModel implements BakedModel {
  private final BakedModel base;
  private final BakedModel overlay;

  CompatItemModel(BakedModel base, BakedModel overlay) {
    this.base = base;
    this.overlay = overlay;
  }

  @Override public List<BakedQuad> getQuads(BlockState state, Direction side, Random random) {
    List<BakedQuad> quads = new ArrayList<>(base.getQuads(state, side, random));
    quads.addAll(overlay.getQuads(state, side, random));
    return quads;
  }
  @Override public boolean useAmbientOcclusion() { return base.useAmbientOcclusion(); }
  @Override public boolean isGui3d() { return base.isGui3d(); }
  @Override public boolean usesBlockLight() { return base.usesBlockLight(); }
  @Override public boolean isCustomRenderer() { return base.isCustomRenderer(); }
  @Override public TextureAtlasSprite getParticleIcon() { return base.getParticleIcon(); }
  @Override public ItemTransforms getTransforms() { return base.getTransforms(); }
  @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}
