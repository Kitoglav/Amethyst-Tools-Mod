/** 
 * Copyright © 2022 Kitoglav
* Licensed under the Apache License, Version 2.0
**/
package com.macaronsteam.amethysttoolsmod.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

public class AmethystToolsModConfig {

	public static ForgeConfigSpec spec;
	public static ForgeConfigSpec.BooleanValue enableIron, enableDiamond, enableNetherite, enableAmethystArrows,
			enableExtraArrows;
	public static ForgeConfigSpec.DoubleValue durabilityMultiplier, extraDigSpeed, extraAttackDamage, extraToughness,
			extraKR, arrowExtraDamage;
	public static ForgeConfigSpec.IntValue extraEnchantability, extraArmor;
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		enableIron = builder.comment("Should be iron amethyst items created?").define("enableIron", true);
		enableDiamond = builder.comment("Should be diamond amethyst items created?").define("enableDiamond", true);
		enableNetherite = builder.comment("Should be netherite amethyst items created?").define("enableNetherite", true);
		durabilityMultiplier = builder.comment("Multiplies durability of amethyst items").defineInRange("durabilityMultiplier", 1.2, 0.1, Double.MAX_VALUE);
		extraDigSpeed = builder.comment("Adds harvest speed to amethyst items").defineInRange("extraDigSpeed", 1, 0, Double.MAX_VALUE);
		extraAttackDamage = builder.comment("Adds damage to amethyst items").defineInRange("extraAttackDamage", 2, 0, Double.MAX_VALUE);
		extraEnchantability = builder.comment("Adds enchantability to amethyst items").defineInRange("extraEnchantability", 5, 0, Integer.MAX_VALUE);
		extraArmor = builder.comment("Adds armor to amethyst items").defineInRange("extraArmor", 1, 0, Integer.MAX_VALUE);
		extraToughness = builder.comment("Adds toughness to amethyst items").defineInRange("extraToughness", 1, 0, Double.MAX_VALUE);
		extraKR = builder.comment("Adds knockback resistance to amethyst items; 0.1 = +1 knockback resistance").defineInRange("extraKR", 0, 0, Double.MAX_VALUE);
		enableAmethystArrows = builder.comment("Should be any amethyst arrows created?").define("enableAmethystArrows", true);
		enableExtraArrows = builder.comment("Should be extra amethyst arrows created?").define("enableExtraArrows", true);
		arrowExtraDamage = builder.comment("Adds damage to amethyst arrow").defineInRange("arrowExtraDamage", 3, 0, Double.MAX_VALUE);
		spec = builder.build();
	}

	public static void setup() {
		CommentedFileConfig cfgData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("amethysttoolsmod-common.toml")).sync().autosave().preserveInsertionOrder().writingMode(WritingMode.REPLACE).build();
		cfgData.load();
		spec.setConfig(cfgData);

	}
}