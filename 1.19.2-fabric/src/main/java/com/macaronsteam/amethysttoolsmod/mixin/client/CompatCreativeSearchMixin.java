package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatPresentation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Index upgraded names and tags too, so creative search can find these variants. */
@Mixin(Minecraft.class)
public abstract class CompatCreativeSearchMixin {
  @Redirect(method = "createSearchTrees", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/Item;fillItemCategory(Lnet/minecraft/world/item/CreativeModeTab;Lnet/minecraft/core/NonNullList;)V"))
  private void amethysttoolsmod$indexVariants(Item item, CreativeModeTab tab, NonNullList<ItemStack> entries) {
    int first = entries.size();
    item.fillItemCategory(tab, entries);
    List<ItemStack> expanded = CompatPresentation.withVariants(entries.subList(first, entries.size()));
    entries.subList(first, entries.size()).clear();
    entries.addAll(expanded);
  }
}
