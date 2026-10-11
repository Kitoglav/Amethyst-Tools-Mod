/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.read;
import com.macaronsteam.amethysttoolsmod.item.AmethystArrowItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystDispenseBehavior;
import com.macaronsteam.amethysttoolsmod.item.AmethystTridentItem;
import com.macaronsteam.amethysttoolsmod.item.MaterialItem;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ItemsInit {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AmethystToolsMod.MODID);

  public static final RegistryObject<Item> AMETHYST_DUST = ITEMS.register("amethyst_dust", MaterialItem::new);
  public static final RegistryObject<Item> AMETHYST_CLUSTER = ITEMS.register("amethyst_cluster", MaterialItem::new);
  public static final RegistryObject<Item> AMETHYST_CLUSTER_LV2 = ITEMS.register("amethyst_cluster_lv2", () -> new MaterialItem(Rarity.UNCOMMON));
  public static final RegistryObject<Item> AMETHYST_ARROW = ITEMS.register("amethyst_arrow", AmethystArrowItem::new);
  public static final RegistryObject<Item> AMETHYST_TIPPED_ARROW = ITEMS.register("amethyst_tipped_arrow", AmethystArrowItem.TippedItem::new);
  public static final RegistryObject<Item> AMETHYST_SPECTRAL_ARROW = ITEMS.register("amethyst_spectral_arrow", AmethystArrowItem.SpectralItem::new);
  public static final RegistryObject<Item> AMETHYST_TRIDENT = ITEMS.register("amethyst_trident", AmethystTridentItem::new);

  private ItemsInit() {}

  static Tier buildTier(Tier tier) {
    return new ForgeTier(
        tier.getLevel(),
        (int) (tier.getUses() * read(AmethystToolsModConfig.DURABILITY_MULTIPLIER)),
        tier.getSpeed() + read(AmethystToolsModConfig.EXTRA_DIG_SPEED).floatValue(),
        tier.getAttackDamageBonus() + read(AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE).floatValue(),
        tier.getEnchantmentValue() + read(AmethystToolsModConfig.EXTRA_ENCHANTABILITY),
        tier.getTag(),
        tier::getRepairIngredient
    );
  }

  public static void registerEquipment() {
    VanillaEquipment.register();
  }

  public static void registerBehavior() {
    AbstractProjectileDispenseBehavior behavior = new AmethystDispenseBehavior();
    DispenserBlock.registerBehavior(AMETHYST_ARROW.get(), behavior);
    DispenserBlock.registerBehavior(AMETHYST_TIPPED_ARROW.get(), behavior);
    DispenserBlock.registerBehavior(AMETHYST_SPECTRAL_ARROW.get(), behavior);
  }
}
