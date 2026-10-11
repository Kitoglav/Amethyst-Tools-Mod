package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;

public final class EntitiesInit {
  private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, AmethystToolsMod.MODID);
  public static final Supplier<EntityType<Arrow>> ENTITY_AMETHYST_ARROW = registerArrow(
      "amethyst_arrow_entity", Arrow::new);
  public static final Supplier<EntityType<SpectralArrow>> ENTITY_AMETHYST_SPECTRAL_ARROW = registerArrow(
      "amethyst_spectral_arrow_entity", SpectralArrow::new);

  private EntitiesInit() {
  }

  public static void register(IEventBus bus) {
    ENTITIES.register(bus);
  }

  private static <T extends AbstractArrow> Supplier<EntityType<T>> registerArrow(String id, EntityType.EntityFactory<T> factory) {
    return ENTITIES.register(id, () ->
        EntityType.Builder.of(factory, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(id));
  }
}
