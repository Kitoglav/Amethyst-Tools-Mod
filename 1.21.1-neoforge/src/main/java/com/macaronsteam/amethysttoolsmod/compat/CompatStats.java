package com.macaronsteam.amethysttoolsmod.compat;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Adds each bonus once, retaining the base modifier's identity, slot and tooltip styling. */
public final class CompatStats {
  private CompatStats() {}

  public static ItemAttributeModifiers attributes(ItemAttributeModifiers original) {
    ItemAttributeModifiers result = original;
    boolean weapon = original.modifiers().stream().anyMatch(entry ->
        entry.attribute().equals(Attributes.ATTACK_DAMAGE)
            && entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
    ItemAttributeModifiers.Entry armor = original.modifiers().stream()
        .filter(entry -> entry.attribute().equals(Attributes.ARMOR)).findFirst().orElse(null);
    if (weapon) {
      result = increase(result, Attributes.ATTACK_DAMAGE, "damage",
          AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get(), EquipmentSlotGroup.MAINHAND);
    }
    if (armor != null) {
      result = increase(result, Attributes.ARMOR, "armor",
          AmethystToolsModConfig.EXTRA_ARMOR.get(), armor.slot());
      result = increase(result, Attributes.ARMOR_TOUGHNESS, "toughness",
          AmethystToolsModConfig.EXTRA_TOUGHNESS.get(), armor.slot());
      result = increase(result, Attributes.KNOCKBACK_RESISTANCE, "knockback",
          AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE.get(), armor.slot());
    }
    return result;
  }

  private static ItemAttributeModifiers increase(ItemAttributeModifiers original,
      Holder<Attribute> attribute, String name, double amount, EquipmentSlotGroup slot) {
    if (amount == 0) {
      return original;
    }
    List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(original.modifiers());
    int selected = -1;
    for (int index = 0; index < entries.size(); index++) {
      ItemAttributeModifiers.Entry entry = entries.get(index);
      if (entry.attribute().equals(attribute) && entry.slot() == slot
          && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
        if (selected == -1) {
          selected = index;
        }
        if (entry.modifier().id().equals(ResourceLocation.withDefaultNamespace("base_attack_damage"))) {
          selected = index;
          break;
        }
      }
    }
    if (selected >= 0) {
      ItemAttributeModifiers.Entry entry = entries.get(selected);
      AttributeModifier modifier = entry.modifier();
      entries.set(selected, new ItemAttributeModifiers.Entry(attribute,
          new AttributeModifier(modifier.id(), modifier.amount() + amount, modifier.operation()), entry.slot()));
    } else {
      entries.add(new ItemAttributeModifiers.Entry(attribute,
          new AttributeModifier(ResourceLocation.fromNamespaceAndPath("amethysttoolsmod",
              "compat_" + name + "_" + slot.getSerializedName()),
              amount, AttributeModifier.Operation.ADD_VALUE), slot));
    }
    return new ItemAttributeModifiers(List.copyOf(entries), original.showInTooltip());
  }
}
