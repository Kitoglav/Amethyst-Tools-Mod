package com.macaronsteam.amethysttoolsmod.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class MaterialItem extends Item {

  public MaterialItem() {
    super(new Properties().tab(CreativeModeTab.TAB_MATERIALS));
  }

  public MaterialItem(Rarity rarity) {
    super(new Properties().tab(CreativeModeTab.TAB_MATERIALS).rarity(rarity));
  }


  @Override
  public boolean isFoil(ItemStack itemstack) {
    return getRarity(itemstack).equals(Rarity.UNCOMMON);
  }
}
