package com.macaronsteam.amethysttoolsmod.item;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.DURABILITY_MULTIPLIER;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ATTACK_DAMAGE;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.EXTRA_ENCHANTABILITY;
import java.util.function.Consumer;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentBEWLR;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.client.IItemRenderProperties;

public final class AmethystTridentItem extends TridentItem {

  private final ImmutableMultimap<Attribute, AttributeModifier> defaultModifiers;

  public AmethystTridentItem() {
    super(new Properties().tab(CreativeModeTab.TAB_COMBAT).defaultDurability((int) (Items.TRIDENT.getMaxDamage() * DURABILITY_MULTIPLIER.get())));
    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D + EXTRA_ATTACK_DAMAGE.get(), AttributeModifier.Operation.ADDITION));
    builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9D, AttributeModifier.Operation.ADDITION));
    this.defaultModifiers = builder.build();
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
    return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : ImmutableMultimap.of();
  }

  @Override
  public int getEnchantmentValue() {
    return super.getEnchantmentValue() + EXTRA_ENCHANTABILITY.get();
  }

  @Override
  public void initializeClient(Consumer<IItemRenderProperties> consumer) {
    consumer.accept(new IItemRenderProperties() {

      @Override
      public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
        return AmethystTridentBEWLR.INSTANCE;
      }
    });
  }

}
