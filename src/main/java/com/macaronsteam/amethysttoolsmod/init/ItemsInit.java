/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import com.google.common.primitives.Booleans;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystArrowEntity;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemsInit {
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AmethystToolsMod.MODID);
  public static RegistryObject<Item> ITEM_AMETHYST_CLUSTER = register("amethyst_cluster", () -> new Item(new Properties().tab(CreativeModeTab.TAB_MATERIALS))),
      ITEM_AMETHYST_DUST = register("amethyst_dust", () -> new Item(new Properties().tab(CreativeModeTab.TAB_MATERIALS))),
      ITEM_AMETHYST_CLUSTER_LV2 = register("amethyst_cluster_lv2", () -> new Item(new Properties().tab(CreativeModeTab.TAB_MATERIALS)) {
        @Override
        public boolean isFoil(ItemStack stack) {
          return true;
        }

        @Override
        public Rarity getRarity(ItemStack stack) {
          return Rarity.UNCOMMON;
        }
      }), ITEM_AMETHYST_ARROW = register("amethyst_arrow", () -> new ArrowItem(new Properties().tab(CreativeModeTab.TAB_COMBAT)) {
        @Override
        public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity living) {
          return new AmethystArrowEntity(level, living);
        }

        @Override
        public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
          int infinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
          return infinity > 0;
        }
      }, AmethystToolsModConfig.enableAmethystArrows.get()),
      ITEM_AMETHYST_TIPPED_ARROW = register("amethyst_tipped_arrow", () -> new TippedArrowItem(new Properties().tab(CreativeModeTab.TAB_COMBAT)) {
        @Override
        public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity living) {
          AmethystArrowEntity arrow = new AmethystArrowEntity(level, living);
          arrow.setEffectsFromItem(stack);
          return arrow;
        }

        @Override
        public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
          int infinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
          return infinity > 0;
        }
      }, AmethystToolsModConfig.enableAmethystArrows.get(), AmethystToolsModConfig.enableExtraArrows.get()),
      ITEM_AMETHYST_SPECTRAL_ARROW = register("amethyst_spectral_arrow", () -> new ArrowItem(new Properties().tab(CreativeModeTab.TAB_COMBAT)) {
        @Override
        public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity living) {
          return new AmethystSpectralArrowEntity(level, living);
        }

        @Override
        public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
          int infinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
          return infinity > 0;
        }
      }, AmethystToolsModConfig.enableAmethystArrows.get(), AmethystToolsModConfig.enableExtraArrows.get());

  public static List<RegistryObject<Item>> ARRAY_ITEMS_EQUIPMENT = registerAmethystEquipment();

  public static RegistryObject<Item> register(String name, Supplier item, boolean... condition) {
    if (!Booleans.contains(condition, false) && name != null && item != null)
      return ITEMS.register(name, item);
    return ReflectionUtil.instantiate(RegistryObject.class);
  }

  public static List<RegistryObject<Item>> registerAmethystEquipment() {
    List<RegistryObject<Item>> toReturn = new ArrayList<>();
    ForgeRegistries.ITEMS.getValues().stream()
        .filter(item -> ForgeHooks.getDefaultCreatorModId(item.getDefaultInstance()).equals("minecraft") && item.canBeDepleted() && AmethystToolsMod.isItemEnabled(item))
        .forEach(item -> toReturn.add(register(item.getRegistryName().getPath() + "_amethyst", () -> AmethystToolsMod.tryToCreate(item))));
    return toReturn;
  }
}
