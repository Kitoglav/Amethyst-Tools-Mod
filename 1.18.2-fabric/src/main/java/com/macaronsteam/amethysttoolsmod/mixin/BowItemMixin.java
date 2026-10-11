package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Allows Infinity to consume none of the mod's arrows, matching the Forge editions. */
@Mixin(BowItem.class)
public abstract class BowItemMixin {
  @Redirect(method = "releaseUsing", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
      ordinal = 0))
  private boolean amethysttoolsmod$allowInfinity(ItemStack stack, Item vanillaArrow) {
    return stack.is(vanillaArrow)
        || stack.is(ItemsInit.AMETHYST_ARROW.get())
        || stack.is(ItemsInit.AMETHYST_TIPPED_ARROW.get())
        || stack.is(ItemsInit.AMETHYST_SPECTRAL_ARROW.get());
  }
}
