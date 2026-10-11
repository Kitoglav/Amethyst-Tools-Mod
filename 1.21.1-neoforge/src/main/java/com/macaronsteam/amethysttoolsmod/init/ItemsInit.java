package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.item.AmethystArrowItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystArmorMaterial;
import com.macaronsteam.amethysttoolsmod.item.AmethystDispenseBehavior;
import com.macaronsteam.amethysttoolsmod.item.AmethystTier;
import com.macaronsteam.amethysttoolsmod.item.AmethystTridentItem;
import com.macaronsteam.amethysttoolsmod.item.MaterialItem;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.DispenserBlock;

public final class ItemsInit {
  private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AmethystToolsMod.MODID);
  public static final Supplier<Item> AMETHYST_DUST = register("amethyst_dust", MaterialItem::new);
  public static final Supplier<Item> AMETHYST_CLUSTER = register("amethyst_cluster", MaterialItem::new);
  public static final Supplier<Item> AMETHYST_CLUSTER_LV2 = register(
      "amethyst_cluster_lv2", () -> new MaterialItem(Rarity.UNCOMMON));
  public static final Supplier<AmethystArrowItem> AMETHYST_ARROW = register("amethyst_arrow", AmethystArrowItem::new);
  public static final Supplier<AmethystArrowItem.TippedItem> AMETHYST_TIPPED_ARROW = register(
      "amethyst_tipped_arrow", AmethystArrowItem.TippedItem::new);
  public static final Supplier<AmethystArrowItem.SpectralItem> AMETHYST_SPECTRAL_ARROW = register(
      "amethyst_spectral_arrow", AmethystArrowItem.SpectralItem::new);
  public static final Supplier<AmethystTridentItem> AMETHYST_TRIDENT = register(
      "amethyst_trident", AmethystTridentItem::new);

  private ItemsInit() {
  }

  static <T extends Item> Supplier<T> register(String id, Supplier<T> factory) {
    return ITEMS.register(id, factory);
  }

  static Tier buildTier(Tier base) {
    return new AmethystTier(base);
  }

  public static void register(IEventBus bus) {
    VanillaEquipment.register();
    AmethystArmorMaterial.register(bus);
    ITEMS.register(bus);
    bus.addListener(ItemsInit::addCreative);
    bus.addListener(ItemsInit::commonSetup);
  }

  private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
      event.accept(AMETHYST_DUST.get());
      event.accept(AMETHYST_CLUSTER.get());
      event.accept(AMETHYST_CLUSTER_LV2.get());
    } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
      event.accept(AMETHYST_ARROW.get());
      event.accept(AMETHYST_TIPPED_ARROW.get());
      event.accept(AMETHYST_SPECTRAL_ARROW.get());
      event.accept(AMETHYST_TRIDENT.get());
      VanillaEquipment.combatItems().forEach(item -> event.accept(item.get()));
    } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      VanillaEquipment.toolItems().forEach(item -> event.accept(item.get()));
    }
  }

  private static void commonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      DispenserBlock.registerBehavior(AMETHYST_ARROW.get(), new AmethystDispenseBehavior(AMETHYST_ARROW.get()));
      DispenserBlock.registerBehavior(AMETHYST_TIPPED_ARROW.get(), new AmethystDispenseBehavior(AMETHYST_TIPPED_ARROW.get()));
      DispenserBlock.registerBehavior(AMETHYST_SPECTRAL_ARROW.get(), new AmethystDispenseBehavior(AMETHYST_SPECTRAL_ARROW.get()));
    });
  }
}
