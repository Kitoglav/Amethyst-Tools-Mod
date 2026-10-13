package com.macaronsteam.amethysttoolsmod.mixin.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatPresentation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Index upgraded names and tags too, so creative search can find these variants. */
@Mixin(Minecraft.class)
public abstract class CompatCreativeSearchMixin {
  @SuppressWarnings("unchecked")
  @Redirect(method = "populateSearchTree", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/searchtree/SearchRegistry;populate(Lnet/minecraft/client/searchtree/SearchRegistry$Key;Ljava/util/List;)V"))
  private <T> void amethysttoolsmod$indexVariants(SearchRegistry registry, SearchRegistry.Key<T> key, List<T> entries) {
    // Only these two keys contain ItemStacks; recipe collections must stay untouched.
    if (SearchRegistry.CREATIVE_NAMES.equals(key) || SearchRegistry.CREATIVE_TAGS.equals(key)) {
      entries = (List<T>) CompatPresentation.withVariants((List<ItemStack>) (List<?>) entries);
    }
    registry.populate(key, entries);
  }
}
