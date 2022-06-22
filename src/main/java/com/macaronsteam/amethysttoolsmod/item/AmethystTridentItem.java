package com.macaronsteam.amethysttoolsmod.item;

import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.durabilityMultiplier;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.extraAttackDamage;
import static com.macaronsteam.amethysttoolsmod.config.AmethystToolsModConfig.extraEnchantability;
import java.util.function.Consumer;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.macaronsteam.amethysttoolsmod.client.renderer.AmethystTridentBEWLR;
import com.macaronsteam.amethysttoolsmod.entity.ThrownAmethystTrident;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.IItemRenderProperties;

public class AmethystTridentItem extends TridentItem {

  private final ImmutableMultimap<Attribute, AttributeModifier> defaultModifiers;

  public AmethystTridentItem() {
    super(new Properties().tab(CreativeModeTab.TAB_COMBAT).defaultDurability((int) (Items.TRIDENT.getMaxDamage() * durabilityMultiplier.get())));
    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D + extraAttackDamage.get(), AttributeModifier.Operation.ADDITION));
    builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9F, AttributeModifier.Operation.ADDITION));
    this.defaultModifiers = builder.build();
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
    return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : ImmutableMultimap.of();
  }

  @Override
  public int getEnchantmentValue() {
    return super.getEnchantmentValue() + extraEnchantability.get();
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

  @Override
  public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int p) {
    if (livingentity instanceof Player player) {
      int i = this.getUseDuration(itemstack) - p;
      if (i >= 10) {
        int j = EnchantmentHelper.getRiptide(itemstack);
        if (j <= 0 || player.isInWaterOrRain()) {
          if (!level.isClientSide) {
            itemstack.hurtAndBreak(1, player, (p_43388_) -> {
              p_43388_.broadcastBreakEvent(livingentity.getUsedItemHand());
            });
            if (j == 0) {
              ThrownAmethystTrident throwntrident = new ThrownAmethystTrident(level, player, itemstack);
              throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + j * 0.5F, 1.0F);
              if (player.getAbilities().instabuild) {
                throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
              }

              level.addFreshEntity(throwntrident);
              level.playSound((Player) null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
              if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(itemstack);
              }
            }
          }

          player.awardStat(Stats.ITEM_USED.get(this));
          if (j > 0) {
            float f7 = player.getYRot();
            float f = player.getXRot();
            float f1 = -Mth.sin(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
            float f2 = -Mth.sin(f * ((float) Math.PI / 180F));
            float f3 = Mth.cos(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
            float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
            float f5 = 3.0F * ((1.0F + j) / 4.0F);
            f1 *= f5 / f4;
            f2 *= f5 / f4;
            f3 *= f5 / f4;
            player.push(f1, f2, f3);
            player.startAutoSpinAttack(20);
            if (player.isOnGround()) {
              player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
            }

            SoundEvent soundevent;
            if (j >= 3) {
              soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
            } else if (j == 2) {
              soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
            } else {
              soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
            }

            level.playSound((Player) null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
          }

        }
      }
    }
  }
}
