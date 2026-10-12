package com.macaronsteam.amethysttoolsmod.compat;

import static org.junit.Assert.*;

import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.macaronsteam.amethysttoolsmod.compat.client.CompatSmithingDisplays;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
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
    original.set(DataComponents.CUSTOM_NAME, literal("My sword"));
    CustomData.update(DataComponents.CUSTOM_DATA, original, tag -> tag.putString("OtherModData", "keep"));
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    ItemStack result = normalSmithing(upgraded, new ItemStack(Items.NETHERITE_SWORD));
    assertTrue(result.is(Items.NETHERITE_SWORD));
    assertTrue(CompatUpgrade.isUpgraded(result));
    assertEquals(original.get(DataComponents.CUSTOM_NAME), result.get(DataComponents.CUSTOM_NAME));
    assertEquals("keep", result.get(DataComponents.CUSTOM_DATA).copyTag().getString("OtherModData"));
    assertTrue(CompatUpgrade.isUpgraded(upgraded));
  }

  @Test
  public void nonCompatibleOutputHasNoAmethystUpgrade() {
    ItemStack result = normalSmithing(CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD)),
        new ItemStack(Items.IRON_SWORD));
    assertTrue(result.is(Items.IRON_SWORD));
    assertFalse(CompatUpgrade.isUpgraded(result));
    assertEquals(Items.IRON_SWORD.components().get(DataComponents.MAX_DAMAGE), result.get(DataComponents.MAX_DAMAGE));
    assertEquals(Items.IRON_SWORD.components().get(DataComponents.ATTRIBUTE_MODIFIERS),
        result.get(DataComponents.ATTRIBUTE_MODIFIERS));
  }

  @Test
  public void plainCopyDoesNotMutateTheUpgradedInput() {
    ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    ItemStack plain = CompatUpgrade.plainCopy(upgraded);
    assertFalse(CompatUpgrade.isUpgraded(plain));
    assertTrue(CompatUpgrade.isUpgraded(upgraded));
    assertEquals(original.get(DataComponents.MAX_DAMAGE), plain.get(DataComponents.MAX_DAMAGE));
    assertEquals(original.get(DataComponents.ATTRIBUTE_MODIFIERS), plain.get(DataComponents.ATTRIBUTE_MODIFIERS));
  }

  @Test
  public void destinationStatsAreRecalculatedWithoutStackingBonuses() {
    ItemStack base = CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD));
    ItemStack first = normalSmithing(base, new ItemStack(Items.NETHERITE_SWORD));
    ItemStack second = normalSmithing(first, new ItemStack(Items.NETHERITE_SWORD));
    ItemStack expected = CompatUpgrade.upgradedCopy(new ItemStack(Items.NETHERITE_SWORD));
    assertEquals(expected.get(DataComponents.MAX_DAMAGE), first.get(DataComponents.MAX_DAMAGE));
    assertEquals(expected.get(DataComponents.ATTRIBUTE_MODIFIERS), first.get(DataComponents.ATTRIBUTE_MODIFIERS));
    assertEquals(first.get(DataComponents.MAX_DAMAGE), second.get(DataComponents.MAX_DAMAGE));
    assertEquals(first.get(DataComponents.ATTRIBUTE_MODIFIERS), second.get(DataComponents.ATTRIBUTE_MODIFIERS));
  }

  @Test
  public void upgradingAnUpgradedCopyIsIdempotent() {
    ItemStack first = CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD));
    ItemStack second = CompatUpgrade.upgradedCopy(first);
    assertEquals(first.getComponents(), second.getComponents());
  }

  @Test
  public void legacy200StacksCanBeMigratedThroughSmithing() {
    ItemStack legacy = CompatUpgrade.upgradedCopy(new ItemStack(Items.DIAMOND_SWORD));
    CustomData.update(DataComponents.CUSTOM_DATA, legacy, tag -> tag.remove("AmethystToolsBaseStats"));
    ItemStack result = normalSmithing(legacy, new ItemStack(Items.NETHERITE_SWORD));
    ItemStack expected = CompatUpgrade.upgradedCopy(new ItemStack(Items.NETHERITE_SWORD));
    assertEquals(expected.get(DataComponents.MAX_DAMAGE), result.get(DataComponents.MAX_DAMAGE));
    assertEquals(expected.get(DataComponents.ATTRIBUTE_MODIFIERS), result.get(DataComponents.ATTRIBUTE_MODIFIERS));
  }

  @Test
  public void strippingTheUpgradePreservesLaterAttributeChanges() {
    ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    var attributes = upgraded.get(DataComponents.ATTRIBUTE_MODIFIERS);
    var entries = new java.util.ArrayList<>(attributes.modifiers());
    var attack = entries.getFirst();
    entries.set(0, new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(
        attack.attribute(), new net.minecraft.world.entity.ai.attributes.AttributeModifier(
            attack.modifier().id(), attack.modifier().amount() + 3, attack.modifier().operation()), attack.slot()));
    var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("othermod", "speed");
    entries.add(new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(
        net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
        new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, 0.1,
            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), attack.slot()));
    upgraded.set(DataComponents.ATTRIBUTE_MODIFIERS,
        new net.minecraft.world.item.component.ItemAttributeModifiers(entries, false));
    upgraded.set(DataComponents.MAX_DAMAGE, upgraded.getMaxDamage() + 50);
    ItemStack plain = CompatUpgrade.plainCopy(upgraded);
    var restored = plain.get(DataComponents.ATTRIBUTE_MODIFIERS);
    assertEquals(original.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().getFirst().modifier().amount() + 3,
        restored.modifiers().getFirst().modifier().amount(), 0);
    assertEquals(id, restored.modifiers().getLast().modifier().id());
    assertFalse(restored.showInTooltip());
    assertEquals(original.getMaxDamage() + 50, plain.getMaxDamage());
  }

  @Test
  public void recordedBonusesAreRemovedIndependentlyOfCurrentConfig() {
    ItemStack original = new ItemStack(Items.DIAMOND_SWORD);
    ItemStack upgraded = CompatUpgrade.upgradedCopy(original);
    // Simulate a stack created with different bonus settings, without mutating global config.
    var modifiers = original.get(DataComponents.ATTRIBUTE_MODIFIERS);
    var entries = new java.util.ArrayList<>(modifiers.modifiers());
    var attack = entries.getFirst();
    entries.set(0, new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(
        attack.attribute(), new net.minecraft.world.entity.ai.attributes.AttributeModifier(
            attack.modifier().id(), attack.modifier().amount() + 5, attack.modifier().operation()), attack.slot()));
    var boosted = new net.minecraft.world.item.component.ItemAttributeModifiers(entries, true);
    upgraded.set(DataComponents.ATTRIBUTE_MODIFIERS, boosted);
    upgraded.set(DataComponents.MAX_DAMAGE, original.getMaxDamage() * 2);
    CustomData.update(DataComponents.CUSTOM_DATA, upgraded, tag -> {
      var recorded = tag.getCompound("AmethystToolsBaseStats");
      recorded.putInt("boosted_max_damage", original.getMaxDamage() * 2);
      recorded.put("boosted_attributes", net.minecraft.world.item.component.ItemAttributeModifiers.CODEC
          .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, boosted).getOrThrow());
    });
    ItemStack plain = CompatUpgrade.plainCopy(upgraded);
    assertEquals(original.get(DataComponents.MAX_DAMAGE), plain.get(DataComponents.MAX_DAMAGE));
    assertEquals(original.get(DataComponents.ATTRIBUTE_MODIFIERS), plain.get(DataComponents.ATTRIBUTE_MODIFIERS));
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
    ItemStack expected = CompatUpgrade.upgradedCopy(new ItemStack(Items.NETHERITE_SWORD));
    assertEquals(expected.get(DataComponents.MAX_DAMAGE), display.result().get(DataComponents.MAX_DAMAGE));
    assertEquals(expected.get(DataComponents.ATTRIBUTE_MODIFIERS),
        display.result().get(DataComponents.ATTRIBUTE_MODIFIERS));
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
    var recipe = new CompatUpgrade();
    assertTrue(CompatSmithingDisplays.create(ResourceLocation.fromNamespaceAndPath("test", "followup"), recipe, List.of(), null).isEmpty());
  }

  private static List<CompatSmithingDisplays.Display> followUp(Item output, Item addition) {
    var recipe = new SmithingTransformRecipe(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        Ingredient.of(Items.DIAMOND_SWORD), Ingredient.of(Items.NETHERITE_INGOT), new ItemStack(output));
    return CompatSmithingDisplays.create(ResourceLocation.fromNamespaceAndPath("test", "followup"), recipe,
        List.of(List.of(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),
        List.of(new ItemStack(Items.DIAMOND_SWORD)), List.of(new ItemStack(addition))), null);
  }

  private static ItemStack normalSmithing(ItemStack base, ItemStack output) {
    SmithingTransformRecipe recipe = new SmithingTransformRecipe(
        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        Ingredient.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD),
        Ingredient.of(Items.NETHERITE_INGOT), output);
    SmithingRecipeInput input = new SmithingRecipeInput(
        new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        CompatUpgrade.plainCopy(base), new ItemStack(Items.NETHERITE_INGOT));
    assertTrue(recipe.matches(input, null));
    ItemStack result = recipe.assemble(input, null);
    return CompatUpgrade.transfer(base, result);
  }

  private static Component literal(String text) {
    return Component.literal(text);
  }
}
