package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

/** JEI-only projections of real smithing recipes; never registered with Minecraft. */
public final class CompatSmithingDisplays {
  private CompatSmithingDisplays() {}

  public static List<Display> create(ResourceLocation id, UpgradeRecipe recipe,
      List<List<ItemStack>> inputs, Level level) {
    if (recipe instanceof CompatUpgrade || inputs.size() != 2) {
      return List.of();
    }
    List<Display> displays = new ArrayList<>();
    for (ItemStack base : inputs.get(0)) {
      if (base.isEmpty() || CompatUpgrade.isUpgraded(base) || CompatModules.find(base.getItem()) == null) {
        continue;
      }
      ItemStack upgraded = CompatUpgrade.upgradedCopy(base);
      for (ItemStack addition : inputs.get(1)) {
        SimpleContainer input = new SimpleContainer(upgraded, addition.copy());
        if (!recipe.matches(input, level)) {
          continue;
        }
        SimpleContainer plain = new SimpleContainer(CompatUpgrade.plainCopy(upgraded), addition.copy());
        ItemStack result = CompatUpgrade.transfer(upgraded, recipe.assemble(plain));
        if (!result.isEmpty()) {
          ResourceLocation displayId = displayId(id, displays.size());
          UpgradeRecipe display = new UpgradeRecipe(displayId,
              Ingredient.of(upgraded), Ingredient.of(addition), result);
          displays.add(new Display(display, upgraded, result));
        }
      }
    }
    return List.copyOf(displays);
  }

  private static ResourceLocation displayId(ResourceLocation source, int index) {
    return new ResourceLocation("amethysttoolsmod",
        "jei/followup/" + source.getNamespace() + "/" + source.getPath() + "/" + index);
  }

  public record Display(UpgradeRecipe recipe, ItemStack base, ItemStack result) {
    /** Match our existing upgraded subtype without depending on damage or enchantments. */
    public boolean matches(ItemStack focus, boolean input) {
      ItemStack target = input ? base : result;
      return CompatUpgrade.isUpgraded(focus) && CompatUpgrade.isUpgraded(target)
          && focus.is(target.getItem());
    }
  }
}
