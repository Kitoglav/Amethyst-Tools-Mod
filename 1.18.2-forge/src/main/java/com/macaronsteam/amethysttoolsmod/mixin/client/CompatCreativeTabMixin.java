package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatPresentation;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Handles custom tabs that override fillItemList as well as ordinary tabs. */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CompatCreativeTabMixin {
  @Inject(method = "selectTab", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/CreativeModeTab;fillItemList(Lnet/minecraft/core/NonNullList;)V",
      shift = At.Shift.AFTER))
  private void amethysttoolsmod$compatVariants(CreativeModeTab tab, CallbackInfo callback) {
    amethysttoolsmod$expandEntries();
  }

  @Inject(method = "refreshSearchResults", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;scrollTo(F)V"))
  private void amethysttoolsmod$searchVariants(CallbackInfo callback) {
    amethysttoolsmod$expandEntries();
  }

  @Inject(method = "refreshSearchResults", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/CreativeModeTab;fillItemList(Lnet/minecraft/core/NonNullList;)V",
      shift = At.Shift.AFTER))
  private void amethysttoolsmod$customTabSearchVariants(CallbackInfo callback) {
    amethysttoolsmod$expandEntries();
  }

  private void amethysttoolsmod$expandEntries() {
    NonNullList<ItemStack> entries = ((CreativeModeInventoryScreen) (Object) this).getMenu().items;
    List<ItemStack> expanded = CompatPresentation.withVariants(entries);
    entries.clear();
    entries.addAll(expanded);
  }
}
