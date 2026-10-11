package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class AmethystArmorItem extends ArmorItem {
  public AmethystArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties properties) { super(material, slot, properties); }

  @Override
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    return AmethystToolsMod.MODID + ":textures/models/" + getMaterial().getName() + "_layer_" + (slot == EquipmentSlot.LEGS ? 2 : 1) + ".png";
  }
}
