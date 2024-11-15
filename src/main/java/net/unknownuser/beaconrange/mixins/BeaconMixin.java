package net.unknownuser.beaconrange.mixins;

import net.minecraft.block.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.unknownuser.beaconrange.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BeaconBlockEntity.class)
public class BeaconMixin {

  @Inject(at = @At("HEAD"), method = "applyPlayerEffects", cancellable = true)
  private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, RegistryEntry < StatusEffect > primaryEffect, RegistryEntry < StatusEffect > secondaryEffect, CallbackInfo ci) {

    // replace method entirely
    ci.cancel();

    if (!world.isClient && primaryEffect != null) {

      double range = 10 + (beaconLevel * 10);
      Box rangeBox = new Box(pos).expand(range).stretch(0.0, world.getHeight(), 0.0);

      int effectDuration = 180 + (beaconLevel * 400);

      List < PlayerEntity > players = world.getNonSpectatingEntities(PlayerEntity.class, rangeBox);
      Iterator < PlayerEntity > playerIterator = players.iterator();
      PlayerEntity playerEntity;

      if (beaconLevel >= 4 && secondaryEffect != null) {
        if (Objects.equals(primaryEffect, secondaryEffect)) {
          while (playerIterator.hasNext()) {
            playerEntity = playerIterator.next();
            playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, effectDuration, 1, true, true));
          }
        } 
	else {
          while (playerIterator.hasNext()) {
            playerEntity = playerIterator.next();
            playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, effectDuration, 0, true, true));
            playerEntity.addStatusEffect(new StatusEffectInstance(secondaryEffect, effectDuration, 0, true, true));
          }
        }
      } 
      else {
        while (playerIterator.hasNext()) {
          playerEntity = playerIterator.next();
          playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, effectDuration, 0, true, true));
        }
      }
    }
  }
}
