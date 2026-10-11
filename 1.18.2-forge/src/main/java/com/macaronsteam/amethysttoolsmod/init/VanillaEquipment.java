package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.item.AmethystArmorItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystArmorMaterial;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import java.util.function.Supplier;

/** Registers the three vanilla equipment tiers with amethyst-adjusted stats. */
final class VanillaEquipment {
  // DeferredRegister constructs the items after Forge has loaded the common config.
  private static final Supplier<Tier> IRON_TIER = () -> ItemsInit.buildTier(Tiers.IRON);
  private static final Supplier<Tier> DIAMOND_TIER = () -> ItemsInit.buildTier(Tiers.DIAMOND);
  private static final Supplier<Tier> NETHERITE_TIER = () -> ItemsInit.buildTier(Tiers.NETHERITE);
  private static final ArmorMaterial IRON_ARMOR = new AmethystArmorMaterial(ArmorMaterials.IRON);
  private static final ArmorMaterial DIAMOND_ARMOR = new AmethystArmorMaterial(ArmorMaterials.DIAMOND);
  private static final ArmorMaterial NETHERITE_ARMOR = new AmethystArmorMaterial(ArmorMaterials.NETHERITE);
  private VanillaEquipment() {
  }

  static void register() {
    registerIronEquipment();
    registerDiamondEquipment();
    registerNetheriteEquipment();
  }

  private static void registerIronEquipment() {
    sword("iron_sword_amethyst", IRON_TIER, false);
    shovel("iron_shovel_amethyst", IRON_TIER, false);
    pickaxe("iron_pickaxe_amethyst", IRON_TIER, false);
    axe("iron_axe_amethyst", IRON_TIER, 6.0F, -3.1F, false);
    hoe("iron_hoe_amethyst", IRON_TIER, -2, -1.0F, false);
    armor("iron", IRON_ARMOR, false);
  }

  private static void registerDiamondEquipment() {
    sword("diamond_sword_amethyst", DIAMOND_TIER, false);
    shovel("diamond_shovel_amethyst", DIAMOND_TIER, false);
    pickaxe("diamond_pickaxe_amethyst", DIAMOND_TIER, false);
    axe("diamond_axe_amethyst", DIAMOND_TIER, 5.0F, -3.0F, false);
    hoe("diamond_hoe_amethyst", DIAMOND_TIER, -3, 0.0F, false);
    armor("diamond", DIAMOND_ARMOR, false);
  }

  private static void registerNetheriteEquipment() {
    sword("netherite_sword_amethyst", NETHERITE_TIER, true);
    shovel("netherite_shovel_amethyst", NETHERITE_TIER, true);
    pickaxe("netherite_pickaxe_amethyst", NETHERITE_TIER, true);
    axe("netherite_axe_amethyst", NETHERITE_TIER, 5.0F, -3.0F, true);
    hoe("netherite_hoe_amethyst", NETHERITE_TIER, -4, 0.0F, true);
    armor("netherite", NETHERITE_ARMOR, true);
  }

  private static void sword(String id, Supplier<Tier> tier, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new SwordItem(tier.get(), 3, -2.4F, combatProperties(fireResistant)));
  }

  private static void shovel(String id, Supplier<Tier> tier, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new ShovelItem(tier.get(), 1.5F, -3.0F, toolProperties(fireResistant)));
  }

  private static void pickaxe(String id, Supplier<Tier> tier, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new PickaxeItem(tier.get(), 1, -2.8F, toolProperties(fireResistant)));
  }

  private static void axe(String id, Supplier<Tier> tier, float damage, float speed, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new AxeItem(tier.get(), damage, speed, toolProperties(fireResistant)));
  }

  private static void hoe(String id, Supplier<Tier> tier, int damage, float speed, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new HoeItem(tier.get(), damage, speed, toolProperties(fireResistant)));
  }

  private static void armor(String prefix, ArmorMaterial material, boolean fire) {
    armorPiece(prefix + "_helmet_amethyst", material, EquipmentSlot.HEAD, fire);
    armorPiece(prefix + "_chestplate_amethyst", material, EquipmentSlot.CHEST, fire);
    armorPiece(prefix + "_leggings_amethyst", material, EquipmentSlot.LEGS, fire);
    armorPiece(prefix + "_boots_amethyst", material, EquipmentSlot.FEET, fire);
  }

  private static void armorPiece(String id, ArmorMaterial material, EquipmentSlot slot, boolean fireResistant) {
    ItemsInit.ITEMS.register(id, () -> new AmethystArmorItem(material, slot, combatProperties(fireResistant)));
  }

  private static Item.Properties combatProperties(boolean fireResistant) {
    return properties(CreativeModeTab.TAB_COMBAT, fireResistant);
  }

  private static Item.Properties toolProperties(boolean fireResistant) {
    return properties(CreativeModeTab.TAB_TOOLS, fireResistant);
  }

  private static Item.Properties properties(CreativeModeTab tab, boolean fireResistant) {
    Item.Properties properties = new Item.Properties().tab(tab);
    return fireResistant ? properties.fireResistant() : properties;
  }
}
