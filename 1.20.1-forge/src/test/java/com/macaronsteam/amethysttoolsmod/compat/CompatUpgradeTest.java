package com.macaronsteam.amethysttoolsmod.compat;

import static org.junit.Assert.*;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.macaronsteam.amethysttoolsmod.compat.client.CompatSmithingDisplays;
import net.minecraft.world.item.Item;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/** Real stack/recipe regression tests, with an isolated integration configuration. */
public class CompatUpgradeTest {
  @Rule public TemporaryFolder temporary = new TemporaryFolder();
  private Path directory;

  @BeforeClass
  public static void bootstrap() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    AmethystToolsModConfig.SPEC.setConfig(com.electronwill.nightconfig.core.CommentedConfig.inMemory());
  }

  @Before
  public void loadIntegration() throws Exception {
    directory = temporary.getRoot().toPath();
    Files.writeString(directory.resolve("test.json"), """
        {
          "module_name": "Smithing regression",
          "items": [
            {"id": "minecraft:diamond_sword", "overlay_model": "amethysttoolsmod:item/test"},
            {"id": "minecraft:netherite_sword", "overlay_model": "amethysttoolsmod:item/test"}
          ]
        }
        """);
    CompatModules.reload(directory);
    assertNotNull(CompatModules.find(Items.DIAMOND_SWORD));
    assertNotNull(CompatModules.find(Items.NETHERITE_SWORD));
  }

  @After
  public void clearIntegration() {
    CompatModules.reload(directory.resolve("empty"));
  }

  @Test
  public void plainNamesAreUnchanged() {
    Component name = literal("Greatsword");
    assertSame(name, CompatPresentation.name(new ItemStack(Items.DIAMOND_SWORD), name));
  }

  @Test
  public void upgradedNamesKeepTheOriginalComponentAndStyle() {
    Component name = literal("Greatsword").copy().withStyle(ChatFormatting.GOLD);
    Component displayed = CompatPresentation.name(CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD)), name);
    assertNotSame(name, displayed);
    assertEquals(name.getStyle(), displayed.getStyle());
    assertTrue(displayed.getString().endsWith(" Greatsword"));
    assertEquals("Greatsword", name.getString());
    assertSame(name, displayed.getSiblings().get(displayed.getSiblings().size() - 1));
  }

  @Test
  public void disabledItemsDoNotGetThePrefix() {
    Component name = literal("Iron Sword");
    assertSame(name, CompatPresentation.name(CompatUpgrade.upgradedCopy(new ItemStack(Items.IRON_SWORD)), name));
  }

  @Test
  public void creativeVariantsFollowTheirPlainItems() {
    ItemStack diamond = new ItemStack(Items.DIAMOND_SWORD);
    ItemStack iron = new ItemStack(Items.IRON_SWORD);
    ItemStack netherite = new ItemStack(Items.NETHERITE_SWORD);
    List<ItemStack> entries = CompatPresentation.withVariants(List.of(diamond, iron, netherite));
    assertEquals(5, entries.size());
    assertSame(diamond, entries.get(0));
    assertTrue(CompatUpgrade.isUpgraded(entries.get(1)));
    assertSame(iron, entries.get(2));
    assertSame(netherite, entries.get(3));
    assertTrue(CompatUpgrade.isUpgraded(entries.get(4)));
    assertFalse(CompatUpgrade.isUpgraded(diamond));
  }

  @Test
  public void rebuildingCreativeEntriesDoesNotDuplicateVariants() {
    List<ItemStack> entries = CompatPresentation.withVariants(List.of(new ItemStack(Items.DIAMOND_SWORD)));
    assertEquals(2, CompatPresentation.withVariants(entries).size());
  }

  @Test
  public void smithingTransfersTheMarkerAndRecipePreservedData() {
    ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
    original.setHoverName(literal("My sword"));
    original.getOrCreateTag().putString("OtherModData", "keep");
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    ItemStack result = normalSmithing(upgraded, new ItemStack(Items.NETHERITE_SWORD));
    assertTrue(result.is(Items.NETHERITE_SWORD));
    assertTrue(CompatUpgrade.isUpgraded(result));
    assertEquals("My sword", result.getHoverName().getString());
    assertEquals("keep", result.getTag().getString("OtherModData"));
    assertTrue(CompatUpgrade.isUpgraded(upgraded));
  }

  @Test
  public void nonCompatibleOutputHasNoAmethystUpgrade() {
    ItemStack result = normalSmithing(CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD)),
        new ItemStack(Items.IRON_SWORD));
    assertTrue(result.is(Items.IRON_SWORD));
    assertFalse(CompatUpgrade.isUpgraded(result));

  }

  @Test
  public void plainCopyDoesNotMutateTheUpgradedInput() {
    ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    ItemStack plain = CompatUpgrade.plainCopy(upgraded);
    assertFalse(CompatUpgrade.isUpgraded(plain));
    assertTrue(CompatUpgrade.isUpgraded(upgraded));

  }

  @Test
  public void jeiFollowUpShowsUpgradedInputAndTransferredOutput() {
    var displays = followUp(Items.NETHERITE_SWORD, Items.NETHERITE_INGOT);
    assertEquals(1, displays.size());
    var display = displays.get(0);
    assertTrue(CompatUpgrade.isUpgraded(display.base()));
    assertTrue(CompatUpgrade.isUpgraded(display.result()));
    assertTrue(display.result().is(Items.NETHERITE_SWORD));
    assertTrue(display.matches(CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD)), true));
    assertTrue(display.matches(CompatUpgrade.upgradedCopy(new ItemStack(Items.NETHERITE_SWORD)), false));

  }

  @Test
  public void jeiFollowUpsDoNotAppearForPlainInputOrOutputFocus() {
    var display = followUp(Items.NETHERITE_SWORD, Items.NETHERITE_INGOT).get(0);
    assertFalse(display.matches(new ItemStack(Items.DIAMOND_SWORD), true));
    assertFalse(display.matches(new ItemStack(Items.NETHERITE_SWORD), false));
  }

  @Test
  public void jeiDoesNotInventAnUpgradeWithInvalidIngredients() {
    assertTrue(followUp(Items.NETHERITE_SWORD, Items.DIAMOND).isEmpty());
  }

  @Test
  public void jeiCanShowAnOrdinaryResultWithoutPollutingItsRecipeLookup() {
    var display = followUp(Items.IRON_SWORD, Items.NETHERITE_INGOT).get(0);
    assertFalse(CompatUpgrade.isUpgraded(display.result()));
    assertTrue(display.result().is(Items.IRON_SWORD));
    assertTrue(display.matches(display.base(), true));
    assertFalse(display.matches(display.result(), false));
  }

  @Test
  public void jeiDoesNotOfferRepeatedAmethystClusterUpgrades() {
    var recipe = new CompatUpgrade(new ResourceLocation("test", "followup"));
    assertTrue(CompatSmithingDisplays.create(new ResourceLocation("test", "followup"), recipe, List.of(), null).isEmpty());
  }

  private static List<CompatSmithingDisplays.Display> followUp(Item output, Item addition) {
    var recipe = new SmithingTransformRecipe(new ResourceLocation("test", "followup"), Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        Ingredient.of(Items.DIAMOND_SWORD), Ingredient.of(Items.NETHERITE_INGOT), new ItemStack(output));
    return CompatSmithingDisplays.create(new ResourceLocation("test", "followup"), recipe,
        List.of(List.of(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),
        List.of(new ItemStack(Items.DIAMOND_SWORD)), List.of(new ItemStack(addition))), null);
  }

  private static ItemStack normalSmithing(ItemStack base, ItemStack output) {
    SmithingTransformRecipe recipe = new SmithingTransformRecipe(new ResourceLocation("test", "upgrade"),
        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        Ingredient.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD),
        Ingredient.of(Items.NETHERITE_INGOT), output);
    SimpleContainer input = new SimpleContainer(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        CompatUpgrade.plainCopy(base), new ItemStack(Items.NETHERITE_INGOT));
    assertTrue(recipe.matches(input, null));
    ItemStack result = recipe.assemble(input, null);
    return CompatUpgrade.transfer(base, result);
  }

  private static Component literal(String text) {
    return Component.literal(text);
  }
}
