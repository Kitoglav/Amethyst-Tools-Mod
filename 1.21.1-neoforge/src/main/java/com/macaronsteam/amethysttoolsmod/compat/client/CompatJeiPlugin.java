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
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.slf4j.Logger;

/** Concrete smithing displays and upgrade-aware lookups for configured items. */
@JeiPlugin
public final class CompatJeiPlugin implements IModPlugin {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, "compat_jei");
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
      Item item = BuiltInRegistries.ITEM.get(entry.id());
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, new ISubtypeInterpreter<ItemStack>() {
        @Override
        public Object getSubtypeData(ItemStack stack, UidContext context) {
          return CompatUpgrade.isUpgraded(stack) ? Boolean.TRUE : null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
          return CompatUpgrade.isUpgraded(stack) ? "amethysttoolsmod:upgraded" : "";
        }
      });
    }
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    CompatModules.reload();
    upgrades.clear();
    upgradedUses.smithingDisplays.clear();
    List<RecipeHolder<SmithingRecipe>> recipes = new ArrayList<>();
    for (CompatModules.Entry entry : entries()) {
      Item item = BuiltInRegistries.ITEM.get(entry.id());
      ItemStack result = CompatUpgrade.upgradedCopy(new ItemStack(item));
      Item cluster = entry.clusterLv2()
          ? ItemsInit.AMETHYST_CLUSTER_LV2.get() : ItemsInit.AMETHYST_CLUSTER.get();
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID,
          "jei/compat/" + entry.id().getNamespace() + "/" + entry.id().getPath());
      RecipeHolder<SmithingRecipe> recipe = new RecipeHolder<>(id, new SmithingTransformRecipe(Ingredient.of(Items.AMETHYST_SHARD), Ingredient.of(item), Ingredient.of(cluster), result));
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
        CustomData.update(DataComponents.CUSTOM_DATA, plain,
            tag -> tag.remove(CompatUpgrade.UPGRADE_TAG));
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
