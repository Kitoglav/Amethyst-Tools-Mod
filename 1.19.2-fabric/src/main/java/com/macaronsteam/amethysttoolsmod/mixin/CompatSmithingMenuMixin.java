package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.UpgradeRecipe;

import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let existing recipes upgrade the original item, then retain compatible amethyst bonuses. */
@Mixin(SmithingMenu.class)
public abstract class CompatSmithingMenuMixin {
  @Redirect(method = "createResult", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/crafting/UpgradeRecipe;assemble(Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack amethysttoolsmod$transferUpgrade(UpgradeRecipe recipe, Container input) {
    ItemStack base = input.getItem(0);
    if (recipe instanceof CompatUpgrade || !CompatUpgrade.isUpgraded(base)) {
      return recipe.assemble(input);
    }
    SimpleContainer plain = new SimpleContainer(input.getContainerSize());
    for (int slot = 0; slot < input.getContainerSize(); slot++) {
      plain.setItem(slot, slot == 0 ? CompatUpgrade.plainCopy(base) : input.getItem(slot).copy());
    }
    return CompatUpgrade.transfer(base, recipe.assemble(plain));
  }
}
