package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

/** JEI-only projections of real smithing recipes; never registered with Minecraft. */
public final class CompatSmithingDisplays {
  private CompatSmithingDisplays() {}

  public static List<Display> create(ResourceLocation id, SmithingRecipe recipe,
      List<List<ItemStack>> inputs, Level level) {
    if (recipe instanceof CompatUpgrade || inputs.size() != 3) {
      return List.of();
    }
    List<Display> displays = new ArrayList<>();
    for (ItemStack base : inputs.get(1)) {
      if (base.isEmpty() || CompatUpgrade.isUpgraded(base) || CompatModules.find(base.getItem()) == null) {
        continue;
      }
      ItemStack upgraded = CompatUpgrade.upgradedCopy(base);
      for (ItemStack template : inputs.get(0)) {
        for (ItemStack addition : inputs.get(2)) {
          SmithingRecipeInput input = new SmithingRecipeInput(template.copy(), upgraded, addition.copy());
          if (!recipe.matches(input, level)) {
            continue;
          }
          SmithingRecipeInput plain = new SmithingRecipeInput(
              template.copy(), CompatUpgrade.plainCopy(upgraded), addition.copy());
          ItemStack result = CompatUpgrade.transfer(upgraded,
              recipe.assemble(plain, level == null ? null : level.registryAccess()));
          if (!result.isEmpty()) {
            ResourceLocation displayId = displayId(id, displays.size());
            RecipeHolder<SmithingRecipe> display = new RecipeHolder<>(displayId, new SmithingTransformRecipe(
                Ingredient.of(template), Ingredient.of(upgraded), Ingredient.of(addition), result));
            displays.add(new Display(display, upgraded, result));
          }
        }
      }
    }
    return List.copyOf(displays);
  }

  private static ResourceLocation displayId(ResourceLocation source, int index) {
    return ResourceLocation.fromNamespaceAndPath("amethysttoolsmod",
        "jei/followup/" + source.getNamespace() + "/" + source.getPath() + "/" + index);
  }

  public record Display(RecipeHolder<SmithingRecipe> recipe, ItemStack base, ItemStack result) {
    /** Match our existing upgraded subtype without depending on damage or enchantments. */
    public boolean matches(ItemStack focus, boolean input) {
      ItemStack target = input ? base : result;
      return CompatUpgrade.isUpgraded(focus) && CompatUpgrade.isUpgraded(target)
          && focus.is(target.getItem());
    }
  }
}
