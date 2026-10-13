package com.macaronsteam.amethysttoolsmod.client;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Keeps the built-in trident renderer in hand, but selects a flat model in inventory contexts. */
public final class TridentModels {
  private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("amethysttoolsmod", "item/amethyst_trident_2d");
  private static BakedModel icon;

  private TridentModels() {}

  public static void register() {
    ModelLoadingPlugin.register(context -> {
      icon = null;
      context.addModels(ICON);
      context.modifyModelAfterBake().register((model, bake) -> {
        if (ICON.equals(bake.resourceId())) {
          icon = model;
        }
        return model;
      });
    });
  }

  public static BakedModel select(ItemStack stack, boolean flat, BakedModel original) {
    if (!flat || !stack.is(ItemsInit.AMETHYST_TRIDENT.get())) {
      return original;
    }
    BakedModel model = icon;
    return model == null || model == Minecraft.getInstance().getModelManager().getMissingModel() ? original : model;
  }
}
