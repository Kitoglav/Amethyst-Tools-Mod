package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Arrow.class)
public abstract class ArrowMixin {
  @Inject(method = "getDefaultPickupItem", at = @At("RETURN"), cancellable = true)
  private void amethysttoolsmod$restoreAmethystPickup(CallbackInfoReturnable<ItemStack> callback) {
    Arrow arrow = (Arrow) (Object) this;
    if (arrow.getType() != EntitiesInit.ENTITY_AMETHYST_ARROW.get()) {
      return;
    }

    ItemStack vanillaPickup = callback.getReturnValue();
    Item amethystItem = arrow.getColor() == -1 ? ItemsInit.AMETHYST_ARROW.get() : ItemsInit.AMETHYST_TIPPED_ARROW.get();
    callback.setReturnValue(copyWithItem(vanillaPickup, amethystItem));
  }

  private static ItemStack copyWithItem(ItemStack source, Item item) {
    ItemStack result = new ItemStack(item, source.getCount());
    result.applyComponents(source.getComponentsPatch());
    return result;
  }
}
