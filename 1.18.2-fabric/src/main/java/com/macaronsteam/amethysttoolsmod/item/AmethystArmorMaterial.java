package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;

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
    return baseMaterial.getDefenseForSlot(slot) + EXTRA_ARMOR.get();
  }

  @Override
  public int getDurabilityForSlot(EquipmentSlot slot) {
    return (int) (baseMaterial.getDurabilityForSlot(slot) * DURABILITY_MULTIPLIER.get());
  }

  @Override
  public int getEnchantmentValue() {
    return baseMaterial.getEnchantmentValue() + EXTRA_ENCHANTABILITY.get();
  }

  @Override
  public SoundEvent getEquipSound() {
    return baseMaterial.getEquipSound();
  }

  @Override
  public float getKnockbackResistance() {
    return baseMaterial.getKnockbackResistance() + EXTRA_KNOCKBACK_RESISTANCE.get().floatValue();
  }

  @Override
  public String getName() {
    return AmethystToolsMod.MODID + ":" + baseMaterial.getName() + "_amethyst";
  }

  @Override
  public Ingredient getRepairIngredient() {
    return baseMaterial.getRepairIngredient();
  }

  @Override
  public float getToughness() {
    return baseMaterial.getToughness() + EXTRA_TOUGHNESS.get().floatValue();
  }
}
