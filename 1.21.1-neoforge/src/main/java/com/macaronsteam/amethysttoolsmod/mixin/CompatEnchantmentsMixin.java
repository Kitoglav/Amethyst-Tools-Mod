package com.macaronsteam.amethysttoolsmod.mixin;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentHelper.class)
public abstract class CompatEnchantmentsMixin {
  @Redirect(method = "getEnchantmentCost", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
  private static int amethysttoolsmod$cost(Item item, RandomSource random, int slot, int shelves, ItemStack stack) {
    return enchantability(item, stack);
  }

  @Redirect(method = "selectEnchantment", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
  private static int amethysttoolsmod$selection(Item item, RandomSource random, ItemStack stack, int cost,
      Stream<Holder<Enchantment>> available) {
    return enchantability(item, stack);
  }

  private static int enchantability(Item item, ItemStack stack) {
    int original = item.getEnchantmentValue();
    return CompatUpgrade.isUpgraded(stack) && CompatModules.find(stack.getItem()) != null
        ? original + AmethystToolsModConfig.EXTRA_ENCHANTABILITY.get() : original;
  }
}
