package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let existing recipes upgrade the original item, then retain compatible amethyst bonuses. */
@Mixin(SmithingMenu.class)
public abstract class CompatSmithingMenuMixin {
  @Redirect(method = "createResult", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/crafting/SmithingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack amethysttoolsmod$transferUpgrade(SmithingRecipe recipe, RecipeInput input, HolderLookup.Provider registries) {
    SmithingRecipeInput smithing = (SmithingRecipeInput) input;
    ItemStack base = smithing.base();
    if (recipe instanceof CompatUpgrade || !CompatUpgrade.isUpgraded(base)) {
      return recipe.assemble(smithing, registries);
    }
    SmithingRecipeInput plain = new SmithingRecipeInput(
        smithing.template(), CompatUpgrade.plainCopy(base), smithing.addition());
    return CompatUpgrade.transfer(base, recipe.assemble(plain, registries));
  }
}
