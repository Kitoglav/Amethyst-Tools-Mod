package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatStats;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Resolves vanilla/modded base overrides before adding the configured overlay. */
final class CompatItemModel implements BakedModel {
  private final BakedModel base;
  private final BakedModel overlay;
  private final CompatModules.Entry entry;
  private final ItemOverrides overrides;

  CompatItemModel(BakedModel base, BakedModel overlay, CompatModules.Entry entry) {
    this.base = base;
    this.overlay = overlay;
    this.entry = entry;
    this.overrides = new ItemOverrides() {
      @Override
      public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level,
          LivingEntity holder, int seed) {
        BakedModel original = base.getOverrides().resolve(base, stack, level, holder, seed);
        if (original == null) {
          original = base;
        }
        if (!CompatStats.applies(stack)) {
          return original;
        }
        BakedModel decoration = overlay.getOverrides().resolve(overlay, stack, level, holder, seed);
        if (decoration == null) {
          decoration = overlay;
        }
        return entry.replaceModel() ? decoration : new CombinedModel(original, decoration);
      }
    };
  }

  @Override
  public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
    return base.getQuads(state, side, random);
  }

  @Override public boolean useAmbientOcclusion() { return base.useAmbientOcclusion(); }
  @Override public boolean isGui3d() { return base.isGui3d(); }
  @Override public boolean usesBlockLight() { return base.usesBlockLight(); }
  @Override public boolean isCustomRenderer() { return base.isCustomRenderer(); }
  @Override public TextureAtlasSprite getParticleIcon() { return base.getParticleIcon(); }
  @Override public ItemTransforms getTransforms() { return base.getTransforms(); }
  @Override public ItemOverrides getOverrides() { return overrides; }

  private static final class CombinedModel implements BakedModel {
    private final BakedModel base;
    private final BakedModel overlay;

    private CombinedModel(BakedModel base, BakedModel overlay) {
      this.base = base;
      this.overlay = overlay;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
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
}
