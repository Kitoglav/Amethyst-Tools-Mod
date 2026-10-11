package com.macaronsteam.amethysttoolsmod.init;

import net.minecraft.world.item.ArmorItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystArmorMaterial;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.List;

/** Registers the three vanilla equipment tiers with amethyst-adjusted stats. */
final class VanillaEquipment {
  private static final List<Item> COMBAT_ITEMS = new ArrayList<>();
  private static final List<Item> TOOL_ITEMS = new ArrayList<>();
  private static final Supplier<Tier> IRON_TIER = () -> ItemsInit.buildTier(Tiers.IRON);
  private static final Supplier<Tier> DIAMOND_TIER = () -> ItemsInit.buildTier(Tiers.DIAMOND);
  private static final Supplier<Tier> NETHERITE_TIER = () -> ItemsInit.buildTier(Tiers.NETHERITE);
  private static final Holder<ArmorMaterial> IRON_ARMOR = AmethystArmorMaterial.register("iron", ArmorMaterials.IRON);
  private static final Holder<ArmorMaterial> DIAMOND_ARMOR = AmethystArmorMaterial.register("diamond", ArmorMaterials.DIAMOND);
  private static final Holder<ArmorMaterial> NETHERITE_ARMOR = AmethystArmorMaterial.register("netherite", ArmorMaterials.NETHERITE);
  private VanillaEquipment() {
  }

  static void register() {
    registerIronEquipment();
    registerDiamondEquipment();
    registerNetheriteEquipment();
  }

  static List<Item> combatItems() {
    return COMBAT_ITEMS;
  }

  static List<Item> toolItems() {
    return TOOL_ITEMS;
  }

  private static void registerIronEquipment() {
    sword("iron_sword_amethyst", IRON_TIER, false);
    shovel("iron_shovel_amethyst", IRON_TIER, false);
    pickaxe("iron_pickaxe_amethyst", IRON_TIER, false);
    axe("iron_axe_amethyst", IRON_TIER, 6.0F, -3.1F, false);
    hoe("iron_hoe_amethyst", IRON_TIER, -2, -1.0F, false);
    armor("iron", IRON_ARMOR, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, false);
  }

  private static void registerDiamondEquipment() {
    sword("diamond_sword_amethyst", DIAMOND_TIER, false);
    shovel("diamond_shovel_amethyst", DIAMOND_TIER, false);
    pickaxe("diamond_pickaxe_amethyst", DIAMOND_TIER, false);
    axe("diamond_axe_amethyst", DIAMOND_TIER, 5.0F, -3.0F, false);
    hoe("diamond_hoe_amethyst", DIAMOND_TIER, -3, 0.0F, false);
    armor("diamond", DIAMOND_ARMOR, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, false);
  }

  private static void registerNetheriteEquipment() {
    sword("netherite_sword_amethyst", NETHERITE_TIER, true);
    shovel("netherite_shovel_amethyst", NETHERITE_TIER, true);
    pickaxe("netherite_pickaxe_amethyst", NETHERITE_TIER, true);
    axe("netherite_axe_amethyst", NETHERITE_TIER, 5.0F, -3.0F, true);
    hoe("netherite_hoe_amethyst", NETHERITE_TIER, -4, 0.0F, true);
    armor("netherite", NETHERITE_ARMOR, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, true);
  }

  private static void sword(String id, Supplier<Tier> tier, boolean fireResistant) {
    registerCombat(id, () -> new SwordItem(tier.get(), toolProperties(tier.get(), fireResistant)
        .attributes(SwordItem.createAttributes(tier.get(), 3, -2.4F))));
  }

  private static void shovel(String id, Supplier<Tier> tier, boolean fireResistant) {
    registerTool(id, () -> new ShovelItem(tier.get(), toolProperties(tier.get(), fireResistant)
        .attributes(DiggerItem.createAttributes(tier.get(), 1.5F, -3.0F))));
  }

  private static void pickaxe(String id, Supplier<Tier> tier, boolean fireResistant) {
    registerTool(id, () -> new PickaxeItem(tier.get(), toolProperties(tier.get(), fireResistant)
        .attributes(DiggerItem.createAttributes(tier.get(), 1.0F, -2.8F))));
  }

  private static void axe(String id, Supplier<Tier> tier, float damage, float speed, boolean fireResistant) {
    registerTool(id, () -> new AxeItem(tier.get(), toolProperties(tier.get(), fireResistant)
        .attributes(DiggerItem.createAttributes(tier.get(), damage, speed))));
  }

  private static void hoe(String id, Supplier<Tier> tier, int damage, float speed, boolean fireResistant) {
    registerTool(id, () -> new HoeItem(tier.get(), toolProperties(tier.get(), fireResistant)
        .attributes(DiggerItem.createAttributes(tier.get(), damage, speed))));
  }

  private static void armor(String prefix, Holder<ArmorMaterial> material, Item helmet, Item chestplate, Item leggings, Item boots, boolean fire) {
    armorPiece(prefix + "_helmet_amethyst", material, ArmorItem.Type.HELMET, helmet, fire);
    armorPiece(prefix + "_chestplate_amethyst", material, ArmorItem.Type.CHESTPLATE, chestplate, fire);
    armorPiece(prefix + "_leggings_amethyst", material, ArmorItem.Type.LEGGINGS, leggings, fire);
    armorPiece(prefix + "_boots_amethyst", material, ArmorItem.Type.BOOTS, boots, fire);
  }

  private static void armorPiece(String id, Holder<ArmorMaterial> material, ArmorItem.Type type, Item base, boolean fireResistant) {
    registerCombat(id, () -> new ArmorItem(material, type, properties(fireResistant)
        .durability((int) (base.getDefaultInstance().getMaxDamage() * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get()))));
  }

  private static <T extends Item> void registerCombat(String id, Supplier<T> item) {
    COMBAT_ITEMS.add(ItemsInit.register(id, item).get());
  }

  private static <T extends Item> void registerTool(String id, Supplier<T> item) {
    TOOL_ITEMS.add(ItemsInit.register(id, item).get());
  }

  private static Item.Properties properties(boolean fireResistant) {
    Item.Properties properties = new Item.Properties();
    return fireResistant ? properties.fireResistant() : properties;
  }

  private static Item.Properties toolProperties(Tier tier, boolean fireResistant) {
    return properties(fireResistant).durability(tier.getUses());
  }
}
