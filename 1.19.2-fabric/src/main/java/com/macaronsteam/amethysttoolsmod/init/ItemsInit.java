package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.item.AmethystArrowItem;
import com.macaronsteam.amethysttoolsmod.item.AmethystDispenseBehavior;
import com.macaronsteam.amethysttoolsmod.item.AmethystTier;
import com.macaronsteam.amethysttoolsmod.item.AmethystTridentItem;
import com.macaronsteam.amethysttoolsmod.item.MaterialItem;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.DispenserBlock;

public final class ItemsInit {
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
    T item = Registry.register(Registry.ITEM, new ResourceLocation(AmethystToolsMod.MODID, id), factory.get());
    return () -> item;
  }

  static Tier buildTier(Tier base) {
    return new AmethystTier(base);
  }

  public static void register() {
    VanillaEquipment.register();
    AmethystDispenseBehavior behavior = new AmethystDispenseBehavior();
    DispenserBlock.registerBehavior(AMETHYST_ARROW.get(), behavior);
    DispenserBlock.registerBehavior(AMETHYST_TIPPED_ARROW.get(), behavior);
    DispenserBlock.registerBehavior(AMETHYST_SPECTRAL_ARROW.get(), behavior);
  }
}
