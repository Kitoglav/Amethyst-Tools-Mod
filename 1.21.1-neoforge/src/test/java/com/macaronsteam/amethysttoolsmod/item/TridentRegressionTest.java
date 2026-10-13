package com.macaronsteam.amethysttoolsmod.item;

import static org.junit.jupiter.api.Assertions.*;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystTrident;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

/** Exercises the registered item and actual transformed entity under the NeoForge test launcher. */
public class TridentRegressionTest {
  @Test
  public void meleeAttributesUseVanillaTooltipIdentities() {
    ItemStack stack = new ItemStack(ItemsInit.AMETHYST_TRIDENT.get());
    var entries = stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers();
    assertEquals(2, entries.size());
    var damage = entries.stream().filter(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE)).findFirst().orElseThrow();
    var speed = entries.stream().filter(entry -> entry.attribute().equals(Attributes.ATTACK_SPEED)).findFirst().orElseThrow();
    assertEquals(Item.BASE_ATTACK_DAMAGE_ID, damage.modifier().id());
    assertEquals(Item.BASE_ATTACK_SPEED_ID, speed.modifier().id());
    assertEquals(8.0 + AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get(), damage.modifier().amount());
    assertEquals(-2.9, speed.modifier().amount());
    assertEquals(AttributeModifier.Operation.ADD_VALUE, damage.modifier().operation());
    assertEquals(EquipmentSlotGroup.MAINHAND, damage.slot());
    assertEquals(EquipmentSlotGroup.MAINHAND, speed.slot());
  }

  @Test
  public void dispensedAmethystStateReachesAClientWithAnOrdinaryPickupStack() {
    ThrownTrident server = new ThrownTrident(null, 1, 2, 3, new ItemStack(ItemsInit.AMETHYST_TRIDENT.get()));
    ThrownTrident client = new ThrownTrident(EntityType.TRIDENT, null);
    assertTrue(((AmethystTrident) server).amethysttoolsmod$isAmethyst());
    assertFalse(((AmethystTrident) client).amethysttoolsmod$isAmethyst());
    assertTrue(client.getWeaponItem().is(Items.TRIDENT));
    var values = server.getEntityData().getNonDefaultValues();
    assertNotNull(values);
    client.getEntityData().assignValues(values);
    assertTrue(((AmethystTrident) client).amethysttoolsmod$isAmethyst());
    assertTrue(client.getWeaponItem().is(Items.TRIDENT));
  }

  @Test
  public void ordinaryTridentsKeepTheirOrdinaryState() {
    ThrownTrident trident = new ThrownTrident(null, 1, 2, 3, new ItemStack(Items.TRIDENT));
    assertFalse(((AmethystTrident) trident).amethysttoolsmod$isAmethyst());
  }
}
