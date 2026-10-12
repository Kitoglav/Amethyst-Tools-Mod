package com.macaronsteam.amethysttoolsmod.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shared presentation for real stacks, creative entries and JEI previews. */
public final class CompatPresentation {
  private CompatPresentation() {}

  public static Component name(ItemStack stack, Component original) {
    if (!CompatUpgrade.isUpgraded(stack) || CompatModules.find(stack.getItem()) == null) {
      return original;
    }
    return Component.translatable("upgraded.amethyst")
        .withStyle(original.getStyle()).append(" ").append(original);
  }

  /** Preserve tab order and put each configured upgrade beside its plain variant. */
  public static List<ItemStack> withVariants(Collection<ItemStack> entries) {
    List<ItemStack> result = new ArrayList<>();
    for (ItemStack stack : entries) {
      result.add(stack);
      if (!stack.isEmpty() && !CompatUpgrade.isUpgraded(stack)
          && CompatModules.find(stack.getItem()) != null) {
        ItemStack upgraded = CompatUpgrade.upgradedCopy(stack);
        boolean alreadyPresent = entries.stream().anyMatch(existing ->
            ItemStack.isSameItemSameComponents(existing, upgraded));
        if (!alreadyPresent) {
          result.add(upgraded);
        }
      }
    }
    return result;
  }
}
