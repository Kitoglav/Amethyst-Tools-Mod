/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystArrowRenderer;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystSpectralArrowRenderer;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystArrowEntity;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import com.macaronsteam.amethysttoolsmod.init.EntitiesInit;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.macaronsteam.amethysttoolsmod.init.RecipesInit;
import com.macaronsteam.amethysttoolsmod.recipe.ConfigValueCondition;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AmethystToolsMod.MODID)
public class AmethystToolsMod {
  public static final String MODID = "amethysttoolsmod";

  public AmethystToolsMod() {
    AmethystToolsModConfig.setup();
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    EntitiesInit.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    ItemsInit.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    RecipesInit.RECIPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    CraftingHelper.register(ConfigValueCondition.Serializer.INSTANCE);
  }

  private void setupClient(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      EntitiesInit.ENTITY_AMETHYST_ARROW.ifPresent(entity -> EntityRenderers.register(entity, AmethystArrowRenderer::new));
      EntitiesInit.ENTITY_AMETHYST_SPECTRAL_ARROW.ifPresent(entity -> EntityRenderers.register(entity, AmethystSpectralArrowRenderer::new));
    });
  }

  private void setup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      ItemsInit.ITEM_AMETHYST_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
        @Override
        protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
          AmethystArrowEntity arrow = new AmethystArrowEntity(level, pos.x(), pos.y(), pos.z());
          arrow.pickup = Pickup.ALLOWED;
          return arrow;
        }
      }));
      ItemsInit.ITEM_AMETHYST_TIPPED_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
        @Override
        protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
          AmethystArrowEntity arrow = new AmethystArrowEntity(level, pos.x(), pos.y(), pos.z());
          arrow.setEffectsFromItem(stack);
          arrow.pickup = Pickup.ALLOWED;
          return arrow;
        }
      }));
      ItemsInit.ITEM_AMETHYST_SPECTRAL_ARROW.ifPresent(item -> DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
        @Override
        protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
          AmethystSpectralArrowEntity arrow = new AmethystSpectralArrowEntity(level, pos.x(), pos.y(), pos.z());
          arrow.pickup = Pickup.ALLOWED;
          return arrow;
        }
      }));
    });
  }

  public static boolean isItemEnabled(Item input) {
    return (Stream.of("Iron", "Diamond", "Netherite")
        .anyMatch(str -> input.getRegistryName().getPath().contains(str.toLowerCase()) && ((BooleanValue) AmethystToolsModConfig.spec.getValues().get("enable" + str)).get()));
  }

  private static Properties buildProperties(Item input) {
    Properties properties = new Properties().tab(input.getItemCategory());
    if (input.isFireResistant())
      properties.fireResistant();
    return properties;
  }

  private static Tier buildTier(Tier tier) {
    return new Tier() {
      @Override
      public int getUses() {
        return (int) (tier.getUses() * AmethystToolsModConfig.durabilityMultiplier.get());
      }

      @Override
      public float getSpeed() {
        return tier.getSpeed() + AmethystToolsModConfig.extraDigSpeed.get().floatValue();
      }

      @Override
      public float getAttackDamageBonus() {
        return tier.getAttackDamageBonus() + AmethystToolsModConfig.extraAttackDamage.get().floatValue();
      }

      @Override
      public int getLevel() {
        return tier.getLevel();
      }

      @Override
      public int getEnchantmentValue() {
        return tier.getEnchantmentValue() + AmethystToolsModConfig.extraEnchantability.get();
      }

      @Override
      public Ingredient getRepairIngredient() {
        return tier.getRepairIngredient();
      }
    };
  }

  private static ArmorMaterial buildArmorMaterial(ArmorMaterial armorMaterial) {
    return new ArmorMaterial() {

      @Override
      public String getName() {
        return armorMaterial.getName() + "_amethyst";
      }

      @Override
      public float getKnockbackResistance() {
        return armorMaterial.getKnockbackResistance() + AmethystToolsModConfig.extraKR.get().floatValue();
      }

      @Override
      public SoundEvent getEquipSound() {
        return armorMaterial.getEquipSound();
      }

      @Override
      public int getEnchantmentValue() {
        return armorMaterial.getEnchantmentValue() + AmethystToolsModConfig.extraEnchantability.get();
      }

      @Override
      public int getDurabilityForSlot(EquipmentSlot slot) {
        return (int) (armorMaterial.getDurabilityForSlot(slot) * AmethystToolsModConfig.durabilityMultiplier.get());
      }

      @Override
      public int getDefenseForSlot(EquipmentSlot slot) {
        return armorMaterial.getDefenseForSlot(slot) + AmethystToolsModConfig.extraArmor.get();
      }

      @Override
      public float getToughness() {
        return armorMaterial.getToughness() + (armorMaterial.getToughness() > 0.0F ? AmethystToolsModConfig.extraToughness.get().floatValue() : 0);
      }

      @Override
      public Ingredient getRepairIngredient() {
        return armorMaterial.getRepairIngredient();
      }
    };
  }

  public static Item tryToCreate(Item input) {
    if (input instanceof ArmorItem iof)
      return new ArmorItem(AmethystToolsMod.buildArmorMaterial(iof.getMaterial()), iof.getSlot(), AmethystToolsMod.buildProperties(input)) {
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
        return (Item) constructor.newInstance(AmethystToolsMod.buildTier(iof.getTier()), attackDamage,
            (float) input.getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).toArray(AttributeModifier[]::new)[0].getAmount(),
            AmethystToolsMod.buildProperties(input));
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
    return null;
  }
}
