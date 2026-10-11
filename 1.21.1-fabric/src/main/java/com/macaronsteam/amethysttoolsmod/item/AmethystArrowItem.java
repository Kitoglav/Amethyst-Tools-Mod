package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpectralArrowItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.Position;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Projectile;

public class AmethystArrowItem extends ArrowItem {
  public static final class SpectralItem extends SpectralArrowItem {

    public SpectralItem() {
      super(arrowProperties());
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity, ItemStack weapon) {
      SpectralArrow arrow = new SpectralArrow(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), level);
      prepareProjectile(arrow, livingentity);
      return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
      return prepareDispensed(new SpectralArrow(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), level), position);
    }

  }

  public static final class TippedItem extends TippedArrowItem {

    public TippedItem() {
      super(arrowProperties());
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity, ItemStack weapon) {
      return createAmethystArrow(level, itemstack, livingentity);
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
      return createAmethystArrow(level, position, stack);
    }

  }

  public AmethystArrowItem() {
    super(arrowProperties());
  }

  @Override
  public AbstractArrow createArrow(Level level, ItemStack itemstack, LivingEntity livingentity, ItemStack weapon) {
    return createAmethystArrow(level, itemstack, livingentity);
  }

  @Override
  public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
    return createAmethystArrow(level, position, stack);
  }


  private static Arrow createAmethystArrow(Level level, ItemStack stack, LivingEntity owner) {
    Arrow arrow = new Arrow(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), level);
    prepareProjectile(arrow, owner);
    stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
        .forEachEffect(arrow::addEffect);
    return arrow;
  }

  private static Arrow createAmethystArrow(Level level, Position position, ItemStack stack) {
    Arrow arrow = prepareDispensed(new Arrow(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), level), position);
    stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
        .forEachEffect(arrow::addEffect);
    return arrow;
  }

  private static <T extends AbstractArrow> T prepareDispensed(T arrow, Position position) {
    arrow.setPos(position.x(), position.y(), position.z());
    arrow.setBaseDamage(arrow.getBaseDamage() + AmethystToolsModConfig.ARROW_EXTRA_DAMAGE.get());
    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
    return arrow;
  }

  private static void prepareProjectile(AbstractArrow arrow, LivingEntity owner) {
    arrow.setOwner(owner);
    arrow.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    arrow.setBaseDamage(arrow.getBaseDamage() + AmethystToolsModConfig.ARROW_EXTRA_DAMAGE.get());
  }

  private static Properties arrowProperties() {
    return new Properties();
  }

}
