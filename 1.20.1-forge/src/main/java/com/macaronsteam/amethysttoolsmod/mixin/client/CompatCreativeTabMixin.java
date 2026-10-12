package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatPresentation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs after loader tab events, before Minecraft indexes the search contents. */
@Mixin(value = CreativeModeTab.class, priority = 900)
public abstract class CompatCreativeTabMixin {
  @Shadow private Collection<ItemStack> displayItems;
  @Shadow private Set<ItemStack> displayItemsSearchTab;

  @Inject(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;rebuildSearchTree()V"))
  private void amethysttoolsmod$compatVariants(CreativeModeTab.ItemDisplayParameters parameters,
      CallbackInfo callback) {
    amethysttoolsmod$expand(displayItems);
    amethysttoolsmod$expand(displayItemsSearchTab);
  }

  private static void amethysttoolsmod$expand(Collection<ItemStack> entries) {
    List<ItemStack> expanded = CompatPresentation.withVariants(entries);
    entries.clear();
    entries.addAll(expanded);
  }
}
