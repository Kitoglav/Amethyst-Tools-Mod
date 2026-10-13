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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Adds each bonus once, retaining the base modifier's identity, slot and tooltip styling. */
public final class CompatStats {
  private CompatStats() {}

  /** Keep the stack-sensitive value supplied by the item and other enchantment hooks. */
  public static int enchantability(ItemStack stack, int original) {
    return CompatUpgrade.isUpgraded(stack) && CompatModules.find(stack.getItem()) != null
        ? original + AmethystToolsModConfig.EXTRA_ENCHANTABILITY.get() : original;
  }

  public static ItemAttributeModifiers attributes(ItemAttributeModifiers original) {
    return attributes(original, 1.0);
  }

  /** Subtract only our recorded deltas, keeping later changes made by other mods. */
  public static ItemAttributeModifiers withoutBonuses(ItemAttributeModifiers current,
      ItemAttributeModifiers baseline, ItemAttributeModifiers boosted) {
    List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
    for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
      ItemAttributeModifiers.Entry applied = find(boosted, entry);
      ItemAttributeModifiers.Entry original = find(baseline, entry);
      if (applied == null) {
        entries.add(entry);
        continue;
      }
      double value = entry.modifier().amount() - applied.modifier().amount()
          + (original == null ? 0.0 : original.modifier().amount());
      if (original != null || value != 0.0) {
        entries.add(new ItemAttributeModifiers.Entry(entry.attribute(),
            new AttributeModifier(entry.modifier().id(), value, entry.modifier().operation()), entry.slot()));
      }
    }
    return new ItemAttributeModifiers(List.copyOf(entries), current.showInTooltip());
  }

  private static ItemAttributeModifiers.Entry find(ItemAttributeModifiers modifiers,
      ItemAttributeModifiers.Entry target) {
    return modifiers.modifiers().stream().filter(entry ->
        entry.attribute().equals(target.attribute()) && entry.slot() == target.slot()
            && entry.modifier().id().equals(target.modifier().id())
            && entry.modifier().operation() == target.modifier().operation())
        .findFirst().orElse(null);
  }

  /** Migration fallback for stacks created before 2.0.1 saved their baseline. */
  public static ItemAttributeModifiers withoutBonuses(ItemAttributeModifiers original) {
    ItemAttributeModifiers restored = attributes(original, -1.0);
    return new ItemAttributeModifiers(restored.modifiers().stream()
        .filter(entry -> !entry.modifier().id().getNamespace().equals("amethysttoolsmod")
            || !entry.modifier().id().getPath().startsWith("compat_")
            || entry.modifier().amount() != 0.0)
        .toList(), restored.showInTooltip());
  }

  private static ItemAttributeModifiers attributes(ItemAttributeModifiers original, double scale) {
    ItemAttributeModifiers result = original;
    boolean weapon = original.modifiers().stream().anyMatch(entry ->
        entry.attribute().equals(Attributes.ATTACK_DAMAGE)
            && entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
    ItemAttributeModifiers.Entry armor = original.modifiers().stream()
        .filter(entry -> entry.attribute().equals(Attributes.ARMOR)).findFirst().orElse(null);
    if (weapon) {
      result = increase(result, Attributes.ATTACK_DAMAGE, "damage",
          AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE.get() * scale, EquipmentSlotGroup.MAINHAND);
    }
    if (armor != null) {
      result = increase(result, Attributes.ARMOR, "armor",
          AmethystToolsModConfig.EXTRA_ARMOR.get() * scale, armor.slot());
      result = increase(result, Attributes.ARMOR_TOUGHNESS, "toughness",
          AmethystToolsModConfig.EXTRA_TOUGHNESS.get() * scale, armor.slot());
      result = increase(result, Attributes.KNOCKBACK_RESISTANCE, "knockback",
          AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE.get() * scale, armor.slot());
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
