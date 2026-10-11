package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AmethystDispenseBehavior extends AbstractProjectileDispenseBehavior {

  @Override
  protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
    if (itemstack.is(ItemsInit.AMETHYST_SPECTRAL_ARROW.get())) {
      SpectralArrow arrow = new SpectralArrow(EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.get(), level);
      arrow.setPos(position.x(), position.y(), position.z());
      return arrow;
    }
    Arrow arrow = new Arrow(EntitiesInit.ENTITY_AMETHYST_ARROW.get(), level);
    arrow.setPos(position.x(), position.y(), position.z());
    arrow.setEffectsFromItem(itemstack);
    return arrow;
  }


}
