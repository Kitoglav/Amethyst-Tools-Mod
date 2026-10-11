package com.macaronsteam.amethysttoolsmod.mixin.client;

import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelManager.class)
public interface ModelManagerAccessor {
  @Accessor("bakedRegistry")
  Map<ResourceLocation, BakedModel> amethysttoolsmod$bakedModels();
}
