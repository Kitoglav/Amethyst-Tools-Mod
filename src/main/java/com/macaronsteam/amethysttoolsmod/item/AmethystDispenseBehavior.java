package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.entity.AmethystArrowEntity;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AmethystDispenseBehavior extends AbstractProjectileDispenseBehavior {

  @Override
  protected Projectile getProjectile(Level level, Position position, ItemStack itemstack) {
    Projectile arrow;
    if (itemstack.is(ItemsInit.ITEM_AMETHYST_SPECTRAL_ARROW.get()))
      arrow = new AmethystSpectralArrowEntity(level, position.x(), position.y(), position.z());
    else {
      arrow = new AmethystArrowEntity(level, position.x(), position.y(), position.z());
      ((AmethystArrowEntity) arrow).setEffectsFromItem(itemstack);
    }
    return arrow;
  }


}
