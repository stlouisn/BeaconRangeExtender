package net.unknownuser.letitrain;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.command.v2.*;
import net.fabricmc.fabric.api.entity.event.v1.*;
import net.minecraft.text.*;
import net.minecraft.world.*;
import org.slf4j.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class LetItRain implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LetItRain");

	protected boolean enabled = true;
	protected float rainGradient = 0f;
	protected float thunderGradient = 0f;
	protected boolean isRaining = false;

	@Override
	public void onInitialize() {
		registerSleepHandlers();
		registerCommandHandler();
	}

	private void registerSleepHandlers() {
		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			if (!enabled && !entity.isPlayer()) {
				return;
			}

			World world = entity.getWorld();
			rainGradient = world.getRainGradient(1f);
			thunderGradient = world.getThunderGradient(1f);
			isRaining = world.getLevelProperties().isRaining();
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (!enabled && !entity.isPlayer() && entity.getWorld().getRegistryKey() == World.OVERWORLD) {
				return;
			}

			final World world = entity.getWorld();

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

	protected void registerCommandHandler() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("sleepweather")
				.then(literal("keep")
				.executes(context -> {
					if (!enabled) {
						enabled = true;
						LOGGER.info("sleep now keeps weather state");
						context.getSource().sendMessage(Text.literal("weather will now be kept when sleeping"));
					} else {
						context.getSource().sendMessage(Text.literal("weather is already kept"));
					}
					return 1;
				}))
				.then(literal("reset")
				.executes(context -> {
					if (enabled) {
						enabled = false;
						LOGGER.info("sleep now resets weather state");
						context.getSource().sendMessage(Text.literal("weather will now reset when sleeping"));
					} else {
						context.getSource().sendMessage(Text.literal("weather already resets"));
					}
					return 1;
				}))
				.executes(context -> {
					context.getSource().sendMessage(Text.literal("weather " + (enabled ? "is kept" : "resets") + " when sleeping"));
					return 1;
				})
		));
	}
}