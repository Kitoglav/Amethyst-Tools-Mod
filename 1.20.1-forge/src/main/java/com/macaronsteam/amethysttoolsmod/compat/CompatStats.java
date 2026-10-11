package com.macaronsteam.amethysttoolsmod.compat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** Stack-specific stat bonuses; no attributes on another copy of the item are changed. */
public final class CompatStats {
  private static final UUID DAMAGE_ID = UUID.fromString("91f5beb9-bbd0-41ac-8fba-392854460101");
  private static final UUID ARMOR_ID = UUID.fromString("91f5beb9-bbd0-41ac-8fba-392854460102");
  private static final UUID TOUGHNESS_ID = UUID.fromString("91f5beb9-bbd0-41ac-8fba-392854460103");
  private static final UUID KNOCKBACK_ID = UUID.fromString("91f5beb9-bbd0-41ac-8fba-392854460104");
  private static final UUID BASE_ATTACK_DAMAGE_ID = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf");

  private CompatStats() {}

  public static boolean applies(ItemStack stack) {
    return CompatUpgrade.isUpgraded(stack) && CompatModules.find(stack.getItem()) != null;
  }

  public static int maxDamage(int original) {
    if (original <= 0) {
      return original;
    }
    return Math.max(1, (int) Math.min(Integer.MAX_VALUE,
        original * AmethystToolsModConfig.DURABILITY_MULTIPLIER.get()));
  }

  public static Multimap<Attribute, AttributeModifier> attributes(
      Multimap<Attribute, AttributeModifier> original, EquipmentSlot slot) {
    double damage = slot == EquipmentSlot.MAINHAND && original.containsKey(Attributes.ATTACK_DAMAGE)
        ? AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get() : 0;
    double armor = original.containsKey(Attributes.ARMOR)
        ? AmethystToolsModConfig.EXTRA_ARMOR.get() : 0;
    double toughness = original.containsKey(Attributes.ARMOR)
        ? AmethystToolsModConfig.EXTRA_TOUGHNESS.get() : 0;
    double knockback = original.containsKey(Attributes.ARMOR)
        ? AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE.get() : 0;

    ImmutableMultimap.Builder<Attribute, AttributeModifier> result = ImmutableMultimap.builder();
    boolean damageMerged = false;
    UUID damageModifier = original.get(Attributes.ATTACK_DAMAGE).stream()
        .filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION)
        .min(java.util.Comparator.comparing(modifier -> !modifier.getId().equals(BASE_ATTACK_DAMAGE_ID)))
        .map(AttributeModifier::getId).orElse(null);
    boolean armorMerged = false;
    boolean toughnessMerged = false;
    boolean knockbackMerged = false;
    for (Map.Entry<Attribute, AttributeModifier> entry : original.entries()) {
      Attribute attribute = entry.getKey();
      AttributeModifier modifier = entry.getValue();
      if (attribute == Attributes.ATTACK_DAMAGE && damage != 0 && !damageMerged
          && modifier.getOperation() == AttributeModifier.Operation.ADDITION
          && modifier.getId().equals(damageModifier)) {
        modifier = increased(modifier, damage);
        damageMerged = true;
      } else if (attribute == Attributes.ARMOR && armor != 0 && !armorMerged
          && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
        modifier = increased(modifier, armor);
        armorMerged = true;
      } else if (attribute == Attributes.ARMOR_TOUGHNESS && toughness != 0 && !toughnessMerged
          && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
        modifier = increased(modifier, toughness);
        toughnessMerged = true;
      } else if (attribute == Attributes.KNOCKBACK_RESISTANCE && knockback != 0 && !knockbackMerged
          && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
        modifier = increased(modifier, knockback);
        knockbackMerged = true;
      }
      result.put(attribute, modifier);
    }
    if (!damageMerged && damage != 0) {
      add(result, Attributes.ATTACK_DAMAGE, DAMAGE_ID, "Amethyst compat damage",
          damage);
    }
    if (original.containsKey(Attributes.ARMOR)) {
      if (!armorMerged) {
        add(result, Attributes.ARMOR, slotId(ARMOR_ID, slot), "Amethyst compat armor", armor);
      }
      if (!toughnessMerged) {
        add(result, Attributes.ARMOR_TOUGHNESS, slotId(TOUGHNESS_ID, slot), "Amethyst compat toughness", toughness);
      }
      if (!knockbackMerged) {
        add(result, Attributes.KNOCKBACK_RESISTANCE, slotId(KNOCKBACK_ID, slot),
            "Amethyst compat knockback resistance", knockback);
      }
    }
    return result.build();
  }

  private static AttributeModifier increased(AttributeModifier original, double amount) {
    return new AttributeModifier(original.getId(), original.getName(), original.getAmount() + amount,
        original.getOperation());
  }

  private static UUID slotId(UUID id, EquipmentSlot slot) {
    return UUID.nameUUIDFromBytes((id + ":" + slot.getName())
        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private static void add(ImmutableMultimap.Builder<Attribute, AttributeModifier> result,
                          Attribute attribute, UUID id, String name, double amount) {
    if (amount != 0) {
      result.put(attribute, new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }
  }
}
