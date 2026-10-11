package com.macaronsteam.amethysttoolsmod.item;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.read;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.DURABILITY_MULTIPLIER;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ARMOR;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ENCHANTABILITY;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_TOUGHNESS;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class AmethystArmorMaterial implements ArmorMaterial {

  private final ArmorMaterial baseMaterial;

  public AmethystArmorMaterial(ArmorMaterial material) {
    this.baseMaterial = material;
  }

  @Override
  public int getDefenseForSlot(EquipmentSlot slot) {
    return baseMaterial.getDefenseForSlot(slot) + read(EXTRA_ARMOR);
  }

  @Override
  public int getDurabilityForSlot(EquipmentSlot slot) {
    return (int) (baseMaterial.getDurabilityForSlot(slot) * read(DURABILITY_MULTIPLIER));
  }

  @Override
  public int getEnchantmentValue() {
    return baseMaterial.getEnchantmentValue() + read(EXTRA_ENCHANTABILITY);
  }

  @Override
  public SoundEvent getEquipSound() {
    return baseMaterial.getEquipSound();
  }

  @Override
  public float getKnockbackResistance() {
    return baseMaterial.getKnockbackResistance() + read(EXTRA_KNOCKBACK_RESISTANCE).floatValue();
  }

  @Override
  public String getName() {
    return baseMaterial.getName() + "_amethyst";
  }

  @Override
  public Ingredient getRepairIngredient() {
    return baseMaterial.getRepairIngredient();
  }

  @Override
  public float getToughness() {
    return baseMaterial.getToughness() + read(EXTRA_TOUGHNESS).floatValue();
  }
}
