package com.macaronsteam.amethysttoolsmod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.macaronsteam.amethysttoolsmod.compat.CompatStats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class CompatEnchantmentsMixin {
  // NeoForge routes both calculations through the stack-sensitive item hook.
  @WrapOperation(method = {"getEnchantmentCost", "selectEnchantment"}, at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
  private static int amethysttoolsmod$enchantability(ItemStack stack, Operation<Integer> original) {
    return CompatStats.enchantability(stack, original.call(stack));
  }
}
