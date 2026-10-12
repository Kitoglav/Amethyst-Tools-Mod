package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.core.RegistryAccess;

import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let existing recipes upgrade the original item, then retain compatible amethyst bonuses. */
@Mixin(SmithingMenu.class)
public abstract class CompatSmithingMenuMixin {
  @Redirect(method = "createResult", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/crafting/SmithingRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack amethysttoolsmod$transferUpgrade(SmithingRecipe recipe, Container input, RegistryAccess registries) {
    ItemStack base = input.getItem(1);
    if (recipe instanceof CompatUpgrade || !CompatUpgrade.isUpgraded(base)) {
      return recipe.assemble(input, registries);
    }
    SimpleContainer plain = new SimpleContainer(input.getContainerSize());
    for (int slot = 0; slot < input.getContainerSize(); slot++) {
      plain.setItem(slot, slot == 1 ? CompatUpgrade.plainCopy(base) : input.getItem(slot).copy());
    }
    return CompatUpgrade.transfer(base, recipe.assemble(plain, registries));
  }
}
