/**
 * Copyright © 2022 Kitoglav Licensed under the Apache License, Version 2.0
 **/
package com.macaronsteam.amethysttoolsmod.init;

import java.util.function.Supplier;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import com.google.common.primitives.Booleans;
import com.macaronsteam.amethysttoolsmod.AmethystToolsMod;
import com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig;
import com.macaronsteam.amethysttoolsmod.entity.AmethystArrowEntity;
import com.macaronsteam.amethysttoolsmod.entity.AmethystSpectralArrowEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntitiesInit {
  public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, AmethystToolsMod.MODID);
  public static RegistryObject<EntityType<AmethystArrowEntity>> ENTITY_AMETHYST_ARROW =
      register("amethyst_arrow_entity", () -> EntityType.Builder.<AmethystArrowEntity>of(AmethystArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20)
          .build(AmethystToolsMod.MODID + ":amethyst_arrow_entity"), AmethystToolsModConfig.enableAmethystArrows.get());
  public static RegistryObject<EntityType<AmethystSpectralArrowEntity>> ENTITY_AMETHYST_SPECTRAL_ARROW =
      register("amethyst_spectral_arrow_entity", () -> EntityType.Builder.<AmethystSpectralArrowEntity>of(AmethystSpectralArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4)
          .updateInterval(20).build(AmethystToolsMod.MODID + ":amethyst_spectral_arrow_entity"), AmethystToolsModConfig.enableAmethystArrows.get(), AmethystToolsModConfig.enableExtraArrows.get());

  private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType<T>> entity, boolean... condition) {
    if (!Booleans.contains(condition, false) && name != null && entity != null)
      return ENTITIES.register(name, entity);
    return ReflectionUtil.instantiate(RegistryObject.class);
  }
}
