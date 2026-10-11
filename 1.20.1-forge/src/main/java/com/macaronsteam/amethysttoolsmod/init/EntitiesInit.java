/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EntitiesInit {
  public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AmethystToolsMod.MODID);
  public static final RegistryObject<EntityType<Arrow>> ENTITY_AMETHYST_ARROW = ENTITIES.register(
      "amethyst_arrow_entity",
      () -> EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC)
          .sized(0.5F, 0.5F)
          .clientTrackingRange(4)
          .updateInterval(20)
          .build(AmethystToolsMod.MODID + ":amethyst_arrow_entity")
  );
  public static final RegistryObject<EntityType<SpectralArrow>> ENTITY_AMETHYST_SPECTRAL_ARROW = ENTITIES.register(
      "amethyst_spectral_arrow_entity",
      () -> EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, MobCategory.MISC)
          .sized(0.5F, 0.5F)
          .clientTrackingRange(4)
          .updateInterval(20)
          .build(AmethystToolsMod.MODID + ":amethyst_spectral_arrow_entity")
  );

  private EntitiesInit() {
  }
}
