package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.entity.AmethystArrowEntity;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpectralArrowItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class AmethystArrowItem extends ArrowItem {
  public static class SpectralItem extends SpectralArrowItem {

    public SpectralItem() {
      super(PROPERTIES);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      return new AmethystSpectralArrowEntity(level, livingentity);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
      return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
    }
  }

  public static class TippedItem extends TippedArrowItem {

    public TippedItem() {
      super(PROPERTIES);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      return new AmethystArrowEntity(level, itemstack, livingentity);
    }

    @Override
    public boolean isInfinite(ItemStack itemstack, ItemStack bow, Player player) {
      return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
    }
  }

  public static final Properties PROPERTIES = new Properties().tab(CreativeModeTab.TAB_COMBAT);

  public AmethystArrowItem() {
    super(PROPERTIES);
  }

  @Override
  public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
    return new AmethystArrowEntity(level, itemstack, livingentity);
  }

  @Override
  public boolean isInfinite(ItemStack itemstack, ItemStack bow, Player player) {
    return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
  }
}
