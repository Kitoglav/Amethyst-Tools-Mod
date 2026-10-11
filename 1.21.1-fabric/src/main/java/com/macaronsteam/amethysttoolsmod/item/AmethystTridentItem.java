package com.macaronsteam.amethysttoolsmod.item;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.DURABILITY_MULTIPLIER;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ENCHANTABILITY;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class AmethystTridentItem extends TridentItem {
  public AmethystTridentItem() {
    super(new Properties()
        .durability((int) (new net.minecraft.world.item.ItemStack(Items.TRIDENT).getMaxDamage() * DURABILITY_MULTIPLIER.get()))
        .attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("amethysttoolsmod", "trident_damage"),
                8.0D + EXTRA_ATTACK_DAMAGE.get(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("amethysttoolsmod", "trident_speed"),
                -2.9D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build()));
  }

  @Override
  public int getEnchantmentValue() {
    return super.getEnchantmentValue() + EXTRA_ENCHANTABILITY.get();
  }
}
