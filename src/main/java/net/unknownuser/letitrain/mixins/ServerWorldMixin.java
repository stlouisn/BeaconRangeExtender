package net.unknownuser.letitrain.mixins;

import net.minecraft.server.world.*;
import net.minecraft.world.level.*;
import net.unknownuser.letitrain.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import static net.unknownuser.letitrain.LetItRain.*;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

	@Final
	@Shadow
	private ServerWorldProperties worldProperties;

	// as far as I can tell, resetWeather is only called when sleeping
	@Inject(at = @At("HEAD"), method = "resetWeather", cancellable = true)
	private void resetWeather(CallbackInfo ci) {
		final int untilRain = worldProperties.isRaining() ? 0 : worldProperties.getRainTime();
		final int untilThunder = worldProperties.isThundering() ? 0 : worldProperties.getThunderTime();

		if (untilRain == 0) {
			int rainContinuationChance = Config.keepRainChance();
			int rainRoll = RANDOM.nextInt(Config.Defaults.NEVER_KEEP, Config.Defaults.ALWAYS_KEEP);
			logRoll("Rain continuation dice rolled: {}/{}.", rainRoll, rainContinuationChance);
			if (rainRoll < rainContinuationChance) {
				worldProperties.setRaining(true);
				logRoll("Rain continuation passed.");
				if (untilThunder == 0) {
					int thunderContinuationChance = Config.keepThunderChance();
					int thunderRoll = RANDOM.nextInt(Config.Defaults.NEVER_KEEP, Config.Defaults.ALWAYS_KEEP);
					logRoll("Thunder continuation dice rolled: {}/{}.", thunderRoll, thunderContinuationChance);
					if (thunderRoll < thunderContinuationChance) {
						worldProperties.setThundering(true);
						logRoll("Thunder continuation passed.");
					} else {
						LetItRain.LOGGER.info("Thunder continuation not passed.");
					}
				}
			} else {
				LetItRain.LOGGER.info("Rain continuation not passed.");
				worldProperties.setRaining(false);
				worldProperties.setThundering(false);
			}

			ci.cancel();
		} else if (Config.preserveWeatherTime()) {
			worldProperties.setRainTime(untilRain);
			worldProperties.setThunderTime(untilThunder);

			ci.cancel();
		}
	}
}
