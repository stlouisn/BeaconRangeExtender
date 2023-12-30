package net.unknownuser.letitrain;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.entity.event.v1.*;
import net.minecraft.text.*;
import net.minecraft.world.*;
import org.slf4j.*;

import java.util.*;

public class LetItRain implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("letitrain");

	float rainGradient = 0f;
	float thunderGradient = 0f;
	boolean isRaining = false;

	@Override
	public void onInitialize() {

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			if(!entity.isPlayer()) {
				return;
			}

			World world = entity.getWorld();
			rainGradient = world.getRainGradient(1f);
			thunderGradient = world.getThunderGradient(1f);
			isRaining = world.getLevelProperties().isRaining();
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			final World world = entity.getWorld();

			if(!entity.isPlayer() || world.getRegistryKey() != World.OVERWORLD) {
				return;
			}

			// without delay, restoring the weather is ignored
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					world.getLevelProperties().setRaining(true);
					world.setRainGradient(rainGradient);
					world.setThunderGradient(thunderGradient);
				}
			}, 10);
		});

	}
}