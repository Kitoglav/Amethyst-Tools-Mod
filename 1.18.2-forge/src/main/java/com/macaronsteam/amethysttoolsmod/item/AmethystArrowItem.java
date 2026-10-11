package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpectralArrowItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class AmethystArrowItem extends ArrowItem {
  public static final class SpectralItem extends SpectralArrowItem {

    public SpectralItem() {
      super(arrowProperties());
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      SpectralArrow arrow = new SpectralArrow(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), level);
      prepareProjectile(arrow, livingentity);
      return arrow;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
      return hasInfinity(bow);
    }
  }

  public static final class TippedItem extends TippedArrowItem {

    public TippedItem() {
      super(arrowProperties());
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
      return createAmethystArrow(level, itemstack, livingentity);
    }

    @Override
    public boolean isInfinite(ItemStack itemstack, ItemStack bow, Player player) {
      return hasInfinity(bow);
    }
  }

  public AmethystArrowItem() {
    super(arrowProperties());
  }

  @Override
  public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity) {
    return createAmethystArrow(level, itemstack, livingentity);
  }

  @Override
  public boolean isInfinite(ItemStack itemstack, ItemStack bow, Player player) {
    return hasInfinity(bow);
  }

  private static Arrow createAmethystArrow(Level level, ItemStack stack, LivingEntity owner) {
    Arrow arrow = new Arrow(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), level);
    prepareProjectile(arrow, owner);
    arrow.setEffectsFromItem(stack);
    return arrow;
  }

  private static void prepareProjectile(AbstractArrow arrow, LivingEntity owner) {
    arrow.setOwner(owner);
    arrow.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    arrow.setBaseDamage(arrow.getBaseDamage() + AmethystToolsModConfig.ARROW_EXTRA_DAMAGE.get());
  }

  private static Properties arrowProperties() {
    return new Properties().tab(CreativeModeTab.TAB_COMBAT);
  }

  private static boolean hasInfinity(ItemStack bow) {
    return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
  }
}
