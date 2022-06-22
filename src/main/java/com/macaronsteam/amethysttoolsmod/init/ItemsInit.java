/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.durabilityMultiplier;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.enableAmethystArrows;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.enableAmethystTrident;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.enableExtraArrows;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.extraAttackDamage;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.extraDigSpeed;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.extraEnchantability;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.management.RuntimeErrorException;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import com.google.common.primitives.Booleans;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.item.AmethystArmorMaterial;
import com.macaronsteam.amethysttoolsmod.item.AmethystArrowItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystDispenseBehavior;
import com.macaronsteam.amethysttoolsmod.item.AmethystTridentItem;
import com.macaronsteam.amethysttoolsmod.item.MaterialItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemsInit {

  public static RegistryObject<Item> ITEM_AMETHYST_DUST, ITEM_AMETHYST_CLUSTER, ITEM_AMETHYST_CLUSTER_LV2, ITEM_AMETHYST_ARROW, ITEM_AMETHYST_TIPPED_ARROW, ITEM_AMETHYST_SPECTRAL_ARROW, ITEM_AMETHYST_TRIDENT;
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AmethystToolsMod.MODID);

  public static final Int2ObjectMap<ArmorMaterial> MATERIALS_CACHE = new Int2ObjectOpenHashMap<>();

  public static final Int2ObjectMap<Tier> TIERS_CACHE = new Int2ObjectOpenHashMap<>();

  private static ArmorMaterial buildArmorMaterial(ArmorMaterial material) {
    int hash = material.getName().hashCode();
    ArmorMaterial amethystMaterial;
    if (!MATERIALS_CACHE.containsKey(hash)) {
      amethystMaterial = new AmethystArmorMaterial(material);
      MATERIALS_CACHE.put(hash, amethystMaterial);
      return amethystMaterial;
    }
    return MATERIALS_CACHE.get(hash);
  }

  private static Properties buildProperties(Item input) {
    Properties properties = new Properties().tab(input.getItemCategory());
    if (input.isFireResistant())
      properties.fireResistant();
    return properties;
  }

  private static Tier buildTier(Tier tier) {
    int hash = TierSortingRegistry.getName(tier).hashCode();
    Tier amethystTier;
    if (!TIERS_CACHE.containsKey(hash)) {
      amethystTier = new ForgeTier(tier.getLevel(), (int) (tier.getUses() * durabilityMultiplier.get()), tier.getSpeed() + extraDigSpeed.get().floatValue(), tier.getAttackDamageBonus() + extraAttackDamage.get().floatValue(), tier.getEnchantmentValue() + extraEnchantability.get(), tier.getTag(), () -> tier.getRepairIngredient());
      TIERS_CACHE.put(hash, amethystTier);
      return amethystTier;
    }
    return TIERS_CACHE.get(hash);
  }

  private static boolean isItemEnabled(Item input) {
    return (Stream.of("Iron", "Diamond", "Netherite").anyMatch(str -> ForgeRegistries.ITEMS.getKey(input).getPath().contains(str.toLowerCase()) && ((BooleanValue) AmethystToolsModConfig.spec.getValues().get("enable" + str)).get()));
  }

  public static void register() {
    registerAmethystEquipment();
    ITEM_AMETHYST_DUST = register("amethyst_dust", () -> new MaterialItem());
    ITEM_AMETHYST_CLUSTER = register("amethyst_cluster", () -> new MaterialItem());
    ITEM_AMETHYST_CLUSTER_LV2 = register("amethyst_cluster_lv2", () -> new MaterialItem(Rarity.UNCOMMON));
    ITEM_AMETHYST_ARROW = register("amethyst_arrow", () -> new AmethystArrowItem(), enableAmethystArrows.get());
    ITEM_AMETHYST_TIPPED_ARROW = register("amethyst_tipped_arrow", () -> new AmethystArrowItem.TippedItem(), enableAmethystArrows.get(), enableExtraArrows.get());
    ITEM_AMETHYST_SPECTRAL_ARROW = register("amethyst_spectral_arrow", () -> new AmethystArrowItem.SpectralItem(), enableAmethystArrows.get(), enableExtraArrows.get());
    ITEM_AMETHYST_TRIDENT = register("amethyst_trident", () -> new AmethystTridentItem(), enableAmethystTrident.get());
  }

  public static RegistryObject<Item> register(String name, Supplier item, boolean... condition) {
    if (!Booleans.contains(condition, false) && name != null && item != null)
      return ITEMS.register(name, item);
    return ReflectionUtil.instantiate(RegistryObject.class);
  }

  public static void registerAmethystEquipment() {
    String[] types = new String[] {"sword", "pickaxe", "axe", "shovel", "hoe", "boots", "leggings", "chestplate", "helmet"};
    List<Item> list = Stream.of("iron", "diamond", "netherite").flatMap(str -> {
      String[] arr = new String[types.length];
      IntStream.range(0, types.length).forEach(i -> arr[i] = String.join("_", str, types[i]));
      return Stream.of(arr);
    }).map(name -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(name))).collect(Collectors.toList());
    list.stream().filter(ItemsInit::isItemEnabled).forEach(item -> register(ForgeRegistries.ITEMS.getKey(item).getPath() + "_amethyst", () -> ItemsInit.tryToCreate(item)));
  }

  public static void registerBehavior() {
    AbstractProjectileDispenseBehavior behavior = new AmethystDispenseBehavior();
    ITEM_AMETHYST_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, behavior));
    ITEM_AMETHYST_TIPPED_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, behavior));
    ITEM_AMETHYST_SPECTRAL_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, behavior));
  }

  public static Item tryToCreate(Item input) {
    if (input instanceof ArmorItem iof)
      return new ArmorItem(buildArmorMaterial(iof.getMaterial()), iof.getSlot(), buildProperties(input)) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
          return AmethystToolsMod.MODID + ":textures/models/" + this.getMaterial().getName() + "_layer_" + (slot != EquipmentSlot.LEGS ? 1 : 2) + ".png";
        }
      };
    else if (input instanceof TieredItem iof) {
      Object attackDamage = (iof instanceof DiggerItem iof1 ? iof1.getAttackDamage() : ((SwordItem) iof).getDamage()) - iof.getTier().getAttackDamageBonus();
      Constructor constructor = input.getClass().getDeclaredConstructors()[0];
      if (constructor.getParameterTypes()[1] == int.class)
        attackDamage = ((Float) attackDamage).intValue();
      try {
        return (Item) constructor.newInstance(buildTier(iof.getTier()), attackDamage, (float) input.getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).toArray(AttributeModifier[]::new)[0].getAmount(), buildProperties(input));
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
        throw new RuntimeErrorException(new Error(input + " instantination by reflection had failed"));
      }
    }
    return null;
  }
}
