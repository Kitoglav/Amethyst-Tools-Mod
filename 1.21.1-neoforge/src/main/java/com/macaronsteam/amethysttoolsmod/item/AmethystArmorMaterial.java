package com.macaronsteam.amethysttoolsmod.item;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ARMOR;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ENCHANTABILITY;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_KNOCKBACK_RESISTANCE;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_TOUGHNESS;

/** Registers the 1.21 armor material records with amethyst-adjusted values. */
public final class AmethystArmorMaterial {
  private static final DeferredRegister<ArmorMaterial> MATERIALS =
      DeferredRegister.create(Registries.ARMOR_MATERIAL, AmethystToolsMod.MODID);

  private AmethystArmorMaterial() {
  }

  public static void register(IEventBus bus) {
    MATERIALS.register(bus);
  }

  public static Holder<ArmorMaterial> register(String name, Holder<ArmorMaterial> base) {
    ArmorMaterial vanilla = base.value();
    Map<ArmorItem.Type, Integer> defense = Arrays.stream(ArmorItem.Type.values())
        .collect(Collectors.toMap(type -> type, type -> vanilla.getDefense(type) + EXTRA_ARMOR.get()));
    ArmorMaterial material = new ArmorMaterial(
        defense,
        vanilla.enchantmentValue() + EXTRA_ENCHANTABILITY.get(),
        vanilla.equipSound(),
        vanilla.repairIngredient(),
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(AmethystToolsMod.MODID, name + "_amethyst"))),
        vanilla.toughness() + EXTRA_TOUGHNESS.get().floatValue(),
        vanilla.knockbackResistance() + EXTRA_KNOCKBACK_RESISTANCE.get().floatValue());
    return MATERIALS.register(name + "_amethyst", () -> material);
  }
}
