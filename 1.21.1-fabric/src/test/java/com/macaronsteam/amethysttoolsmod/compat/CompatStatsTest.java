package com.macaronsteam.amethysttoolsmod.compat;

import static org.junit.Assert.*;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;

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
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("test", "armor");
    ItemAttributeModifiers chest = new ItemAttributeModifiers(java.util.List.of(entry(Attributes.ARMOR, id, 6)), true);
    ItemAttributeModifiers head = new ItemAttributeModifiers(java.util.List.of(
        new ItemAttributeModifiers.Entry(Attributes.ARMOR,
            new AttributeModifier(id, 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)), true);
    assertNotEquals(CompatStats.attributes(chest).modifiers().get(1).modifier().id(),
        CompatStats.attributes(head).modifiers().get(1).modifier().id());
  }

  @Test
  public void smithingPreviewUsesTheSameStatsWithoutChangingTheOriginalStack() {
    net.minecraft.world.item.ItemStack original = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
    net.minecraft.world.item.ItemStack result = CompatUpgrade.upgradedCopy(original);
    assertFalse(CompatUpgrade.isUpgraded(original));
    assertTrue(CompatUpgrade.isUpgraded(result));
    assertEquals((int) (original.getMaxDamage() * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get()),
        result.getMaxDamage());
    assertEquals(CompatStats.attributes(original.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS)),
        result.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS));
  }

  @Test
  public void damageBonusReplacesBaseModifierRatherThanAddingAnotherLine() {
    ResourceLocation id = ResourceLocation.withDefaultNamespace("base_attack_damage");
    ItemAttributeModifiers original = new ItemAttributeModifiers(java.util.List.of(
        new ItemAttributeModifiers.Entry(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(id, 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)), true);
    ItemAttributeModifiers result = CompatStats.attributes(original);
    assertEquals(1, result.modifiers().size());
    assertEquals(9, result.modifiers().getFirst().modifier().amount(), 0);
    assertEquals(id, result.modifiers().getFirst().modifier().id());
    assertEquals(7, original.modifiers().getFirst().modifier().amount(), 0);
  }

  @Test
  public void armorBonusesMergeAndKeepTheirEquipmentSlot() {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("test", "chestplate");
    ItemAttributeModifiers original = new ItemAttributeModifiers(java.util.List.of(
        entry(Attributes.ARMOR, id, 8), entry(Attributes.ARMOR_TOUGHNESS, id, 2),
        entry(Attributes.KNOCKBACK_RESISTANCE, id, 0.1)), true);
    ItemAttributeModifiers result = CompatStats.attributes(original);
    assertEquals(3, result.modifiers().size());
    assertEquals(9, result.modifiers().get(0).modifier().amount(), 0);
    assertEquals(3, result.modifiers().get(1).modifier().amount(), 0);
    assertEquals(0.1 + AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE.get(),
        result.modifiers().get(2).modifier().amount(), 0.000001);
    result.modifiers().forEach(entry -> assertEquals(EquipmentSlotGroup.CHEST, entry.slot()));
  }

  @Test
  public void missingArmorBonusesAreAddedWithoutDuplicatingExistingOnes() {
    ItemAttributeModifiers original = new ItemAttributeModifiers(java.util.List.of(
        entry(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("test", "chestplate"), 6)), false);
    ItemAttributeModifiers result = CompatStats.attributes(original);
    assertEquals(2, result.modifiers().size());
    assertFalse(result.showInTooltip());
    result.modifiers().forEach(entry -> assertEquals(EquipmentSlotGroup.CHEST, entry.slot()));
  }

  private static ItemAttributeModifiers.Entry entry(Holder<Attribute> attribute, ResourceLocation id, double value) {
    return new ItemAttributeModifiers.Entry(attribute,
        new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
  }

}
