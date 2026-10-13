package com.macaronsteam.amethysttoolsmod.client;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.mixin.client.ModelManagerAccessor;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Keeps the built-in trident renderer in hand, but selects a flat model in inventory contexts. */
public final class TridentModels {
  private static final ResourceLocation ICON = new ResourceLocation("amethysttoolsmod", "item/amethyst_trident_2d");

  private TridentModels() {}

  public static void register() {
    ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, models) -> models.accept(ICON));
  }

  public static BakedModel select(ItemStack stack, boolean flat, BakedModel original) {
    if (!flat || !stack.is(ItemsInit.AMETHYST_TRIDENT.get())) {
      return original;
    }
    BakedModel model = ((ModelManagerAccessor) Minecraft.getInstance().getModelManager())
        .amethysttoolsmod$bakedModels().get(ICON);
    return model == null || model == Minecraft.getInstance().getModelManager().getMissingModel() ? original : model;
  }
}
