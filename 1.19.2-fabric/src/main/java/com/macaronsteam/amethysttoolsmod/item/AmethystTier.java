package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Vanilla tier values plus the configured amethyst bonuses. */
public record AmethystTier(Tier base) implements Tier {
  @Override
  public int getUses() {
    return (int) (base.getUses() * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get());
  }

  @Override
  public float getSpeed() {
    return base.getSpeed() + AmethystToolsModConfig.EXTRA_DIG_SPEED.get().floatValue();
  }

  @Override
  public float getAttackDamageBonus() {
    return base.getAttackDamageBonus() + AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get().floatValue();
  }

  @Override
  public int getLevel() {
    return base.getLevel();
  }

  @Override
  public int getEnchantmentValue() {
    return base.getEnchantmentValue() + AmethystToolsModConfig.EXTRA_ENCHANTABILITY.get();
  }

  @Override
  public Ingredient getRepairIngredient() {
    return base.getRepairIngredient();
  }
}
