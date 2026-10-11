package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import com.macaronsteam.amethysttoolsmod.compat.CompatUpgrade;
import com.macaronsteam.amethysttoolsmod.init.ItemsInit;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.slf4j.Logger;

/** Concrete smithing displays and upgrade-aware lookups for configured items. */
@JeiPlugin
public final class CompatJeiPlugin implements IModPlugin {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final ResourceLocation UID = new ResourceLocation(AmethystToolsMod.MODID, "compat_jei");
  private final UpgradedUses upgradedUses = new UpgradedUses();
  private final List<ItemStack> upgrades = new ArrayList<>();

  @Override
  public ResourceLocation getPluginUid() {
    return UID;
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    CompatModules.reload();
    for (CompatModules.Entry entry : entries()) {
      Item item = ForgeRegistries.ITEMS.getValue(entry.id());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (stack, context) ->
          CompatUpgrade.isUpgraded(stack) ? "amethysttoolsmod:upgraded" : IIngredientSubtypeInterpreter.NONE);
    }
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    CompatModules.reload();
    upgrades.clear();
    upgradedUses.smithingDisplays.clear();
    List<UpgradeRecipe> recipes = new ArrayList<>();
    for (CompatModules.Entry entry : entries()) {
      Item item = ForgeRegistries.ITEMS.getValue(entry.id());
      ItemStack result = new ItemStack(item);
      result.getOrCreateTag().putBoolean(CompatUpgrade.UPGRADE_TAG, true);
      Item cluster = entry.clusterLv2()
          ? ItemsInit.AMETHYST_CLUSTER_LV2.get() : ItemsInit.AMETHYST_CLUSTER.get();
      ResourceLocation id = new ResourceLocation(AmethystToolsMod.MODID,
          "jei/compat/" + entry.id().getNamespace() + "/" + entry.id().getPath());
      UpgradeRecipe recipe = new UpgradeRecipe(id, Ingredient.of(item), Ingredient.of(cluster), result);
      recipes.add(recipe);
      upgradedUses.smithingDisplays.add(recipe);
      upgrades.add(result);
    }
    registration.addRecipes(RecipeTypes.SMITHING, recipes);
    LOGGER.info("Amethyst Tools JEI: registered {} configured smithing upgrades", recipes.size());
  }

  @Override
  public void registerAdvanced(IAdvancedRegistration registration) {
    registration.addRecipeManagerPlugin(upgradedUses);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime runtime) {
    upgradedUses.runtime = runtime;
    runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, upgrades);
  }

  private static List<CompatModules.Entry> entries() {
    return CompatModules.items().values().stream()
        .sorted(Comparator.comparing(entry -> entry.id().toString())).toList();
  }

  /** Only upgraded input focuses delegate to ordinary uses; output lookups stay subtype-specific. */
  private static final class UpgradedUses implements IRecipeManagerPlugin {
    private final Set<Object> smithingDisplays = new HashSet<>();
    private IJeiRuntime runtime;

    private Optional<IFocus<ItemStack>> plainInput(IFocus<?> focus) {
      if (runtime == null || focus.getRole() != RecipeIngredientRole.INPUT) {
        return Optional.empty();
      }
      return focus.checkedCast(VanillaTypes.ITEM_STACK).filter(typed -> {
        ItemStack stack = typed.getTypedValue().getIngredient();
        return CompatUpgrade.isUpgraded(stack) && CompatModules.find(stack.getItem()) != null;
      }).map(typed -> {
        ItemStack plain = typed.getTypedValue().getIngredient().copy();
        plain.getOrCreateTag().remove(CompatUpgrade.UPGRADE_TAG);
        return runtime.getJeiHelpers().getFocusFactory()
            .createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, plain);
      });
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
      List<RecipeType<?>> types = new ArrayList<>();
      plainInput(focus).ifPresent(plain -> {
        runtime.getRecipeManager().createRecipeCategoryLookup().limitFocus(List.of(plain)).get()
            .filter(category -> !getRecipes(category, focus).isEmpty())
            .forEach(category -> types.add(category.getRecipeType()));
      });
      return types;
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> category, IFocus<V> focus) {
      // The nested lookup uses an unmarked stack, so it cannot re-enter this branch.
      return plainInput(focus).map(plain ->
          runtime.getRecipeManager().createRecipeLookup(category.getRecipeType())
              .limitFocus(List.of(plain)).get()
              .filter(recipe -> !smithingDisplays.contains(recipe)).toList()).orElseGet(List::of);
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> category) {
      return List.of();
    }
  }
}
