package com.macaronsteam.amethysttoolsmod.compat;

import static org.junit.Assert.*;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import org.junit.BeforeClass;
import org.junit.Test;

public class CompatStatsTest {
  @BeforeClass
  public static void bootstrap() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();

  }


  @Test
  public void newlyAddedBonusesDoNotShareIdsAcrossArmorSlots() {
    Multimap<Attribute, AttributeModifier> original = ImmutableMultimap.of(Attributes.ARMOR,
        new AttributeModifier(UUID.randomUUID(), "Armor", 6, AttributeModifier.Operation.ADDITION));
    AttributeModifier chest = CompatStats.attributes(original, EquipmentSlot.CHEST)
        .get(Attributes.ARMOR_TOUGHNESS).iterator().next();
    AttributeModifier head = CompatStats.attributes(original, EquipmentSlot.HEAD)
        .get(Attributes.ARMOR_TOUGHNESS).iterator().next();
    assertNotEquals(chest.getId(), head.getId());
  }

  @Test
  public void damageBonusReplacesBaseModifierRatherThanAddingAnotherLine() {
    UUID id = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf");
    AttributeModifier base = new AttributeModifier(id, "Weapon modifier", 7, AttributeModifier.Operation.ADDITION);
    Multimap<Attribute, AttributeModifier> original = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE, base);
    Multimap<Attribute, AttributeModifier> result = CompatStats.attributes(original, EquipmentSlot.MAINHAND);
    assertEquals(1, result.size());
    AttributeModifier upgraded = result.get(Attributes.ATTACK_DAMAGE).iterator().next();
    assertEquals(9, upgraded.getAmount(), 0); // Player base damage adds the remaining 1 in the tooltip.
    assertSame(id, upgraded.getId()); // Vanilla's green damage line uses UUID object identity.
    assertEquals(7, base.getAmount(), 0);
  }

  @Test
  public void armorBonusesMergeAndKeepTheirEquipmentSlot() {
    UUID id = UUID.randomUUID();
    Multimap<Attribute, AttributeModifier> original = ImmutableMultimap.<Attribute, AttributeModifier>builder()
        .put(Attributes.ARMOR, new AttributeModifier(id, "Armor modifier", 8, AttributeModifier.Operation.ADDITION))
        .put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, "Armor toughness", 2, AttributeModifier.Operation.ADDITION))
        .put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, "Armor knockback", 0.1, AttributeModifier.Operation.ADDITION))
        .build();
    Multimap<Attribute, AttributeModifier> result = CompatStats.attributes(original, EquipmentSlot.CHEST);
    assertEquals(3, result.size());
    assertEquals(9, result.get(Attributes.ARMOR).iterator().next().getAmount(), 0);
    assertEquals(3, result.get(Attributes.ARMOR_TOUGHNESS).iterator().next().getAmount(), 0);
    assertEquals(0.1 + AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE.get(),
        result.get(Attributes.KNOCKBACK_RESISTANCE).iterator().next().getAmount(), 0.000001);
    result.values().forEach(modifier -> assertSame(id, modifier.getId()));
  }

  @Test
  public void missingArmorBonusesAreAddedWithoutDuplicatingExistingOnes() {
    Multimap<Attribute, AttributeModifier> original = ImmutableMultimap.of(Attributes.ARMOR,
        new AttributeModifier(UUID.randomUUID(), "Armor", 6, AttributeModifier.Operation.ADDITION));
    Multimap<Attribute, AttributeModifier> result = CompatStats.attributes(original, EquipmentSlot.CHEST);
    assertEquals(2, result.size());
    assertEquals(7, result.get(Attributes.ARMOR).iterator().next().getAmount(), 0);
    assertEquals(1, result.get(Attributes.ARMOR_TOUGHNESS).iterator().next().getAmount(), 0);
  }

  @Test
  public void thirdPartyDamageUuidAlsoMerges() {
    UUID id = UUID.randomUUID();
    Multimap<Attribute, AttributeModifier> result = CompatStats.attributes(ImmutableMultimap.of(
        Attributes.ATTACK_DAMAGE, new AttributeModifier(id, "Custom damage", 7,
            AttributeModifier.Operation.ADDITION)), EquipmentSlot.MAINHAND);
    assertEquals(1, result.size());
    assertSame(id, result.values().iterator().next().getId());
    assertEquals(9, result.values().iterator().next().getAmount(), 0);
  }

  @Test
  public void offhandGetsNoWeaponBonus() {
    Multimap<Attribute, AttributeModifier> original = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,
        new AttributeModifier(UUID.randomUUID(), "Damage", 7, AttributeModifier.Operation.ADDITION));
    assertEquals(original, CompatStats.attributes(original, EquipmentSlot.OFFHAND));
  }

}
