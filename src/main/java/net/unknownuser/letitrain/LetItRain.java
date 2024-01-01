package net.unknownuser.letitrain;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.entity.event.v1.*;
import net.minecraft.entity.*;
import net.minecraft.server.world.*;
import net.minecraft.world.level.*;
import org.slf4j.*;

import java.util.*;

public class LetItRain implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LetItRain");
	protected static final Random random = new Random();

	@Override
	public void onInitialize() {
		registerSleepHandlers();
	}

	protected void registerSleepHandlers() {
		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (!isPlayerOnServer(entity)) {
				return;
			}

			final ServerWorldProperties properties = (ServerWorldProperties) entity.getWorld().getLevelProperties();

			final int untilRain = properties.isRaining() ? 0 : properties.getRainTime();
			final int untilThunder = properties.isThundering() ? 0 : properties.getThunderTime();

			// without delay, restoring the weather is ignored
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if (untilRain == 0) {
						int rainContinuationChance = Config.keepRainChance();
						int rainRoll = random.nextInt(Config.NEVER_KEEP, Config.ALWAYS_KEEP);
						loginfo("Rain continuation dice rolled: {}/{}.", rainRoll, rainContinuationChance);
						if (rainRoll < rainContinuationChance) {
							properties.setRaining(true);
							loginfo("Rain continuation passed.");
							if (untilThunder == 0) {
								int thunderContinuationChance = Config.keepThunderChance();
								int thunderRoll = random.nextInt(Config.NEVER_KEEP, Config.ALWAYS_KEEP);
								loginfo("Thunder continuation dice rolled: {}/{}.", thunderRoll, thunderContinuationChance);
								if (thunderRoll < thunderContinuationChance) {
									properties.setThundering(true);
									loginfo("Thunder continuation passed.");
								} else {
									LetItRain.LOGGER.info("Thunder continuation not passed.");
								}
							}
						} else {
							LetItRain.LOGGER.info("Rain continuation not passed.");
							properties.setRaining(false);
							properties.setThundering(false);
						}
					} else if (Config.preserveWeatherTime()) {
						properties.setRainTime(untilRain);
						properties.setThunderTime(untilThunder);
					}
				}
			}, 10);
		});
	}

	private static boolean isPlayerOnServer(LivingEntity entity) {
		return entity.isPlayer() && entity.getWorld() instanceof ServerWorld && entity.getWorld().getLevelProperties() instanceof ServerWorldProperties;
	}

	protected void loginfo(String format, Object... objects) {
		if (Config.logRolls()) {
			LOGGER.info(format, objects);
		}
	}
}
