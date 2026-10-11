package com.macaronsteam.amethysttoolsmod.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

/** Exposes vanilla's protected tool constructors without overriding behavior. */
public final class EquipmentItems {
  private EquipmentItems() {
  }

  public static final class Pickaxe extends PickaxeItem {
    public Pickaxe(Tier tier, int damage, float speed, Item.Properties properties) {
      super(tier, damage, speed, properties);
    }
  }

  public static final class Axe extends AxeItem {
    public Axe(Tier tier, float damage, float speed, Item.Properties properties) {
      super(tier, damage, speed, properties);
    }
  }

  public static final class Hoe extends HoeItem {
    public Hoe(Tier tier, int damage, float speed, Item.Properties properties) {
      super(tier, damage, speed, properties);
    }
  }
}
