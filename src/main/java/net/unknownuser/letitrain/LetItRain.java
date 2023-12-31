package net.unknownuser.letitrain;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.command.v2.*;
import net.fabricmc.fabric.api.entity.event.v1.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import net.minecraft.world.*;
import org.slf4j.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class LetItRain implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LetItRain");

	public static final Config config = Config.getInstance();
	protected static final Random random = new Random();

	protected float rainGradient = 0f;
	protected float thunderGradient = 0f;
	protected boolean isRaining = false;

	public static final float DELTA_CURRENT_STATE = 1f;

	@Override
	public void onInitialize() {
		registerSleepHandlers();
		registerCommandHandler();
	}

	protected void registerSleepHandlers() {
		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			if (!config.isEnabled() && !entity.isPlayer()) {
				return;
			}

			World world = entity.getWorld();
			rainGradient = world.getRainGradient(DELTA_CURRENT_STATE);
			thunderGradient = world.getThunderGradient(DELTA_CURRENT_STATE);
			isRaining = world.isRaining();
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (!config.isEnabled() && !entity.isPlayer()) {
				return;
			}

			if (random.nextInt(Config.NEVER_RESTORE, Config.ALWAYS_RESTORE) > config.getKeepWeatherChance()) {
				LOGGER.info("roll failed, weather is restoring disabled");
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
		// https://github.com/Lucaslah/WeatherChanger/blob/main/src/main/java/me/lucaslah/weatherchanger/WeatherChanger.java

		// LOOK AT THE BRACKETS
		// IF A COMMAND DOES NOT WORK IT'S THE BRACKETS
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("sleepweather")
				.then(literal("keep")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							setChanceTo(context, Config.ALWAYS_RESTORE);

							return 1;
						}))
				.then(literal("reset")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							setChanceTo(context, Config.NEVER_RESTORE);

							return 1;
						}))
				.then(literal("set")
						.then(argument("chance", IntegerArgumentType.integer())
								.requires(source -> source.hasPermissionLevel(2))
								.executes(context -> {
									setChanceToPercent(context, IntegerArgumentType.getInteger(context, "chance"));

									return 1;
								})))
				.executes(context -> {
					context.getSource().sendMessage(Text.literal(String.format("weather chance is %d%% when sleeping", config.getKeepWeatherChance())));
					return 1;
				})
		));
	}

	protected static void setChanceToPercent(CommandContext<ServerCommandSource> context, int percent) {
		setChanceTo(context, percent);
	}

	protected static void setChanceTo(CommandContext<ServerCommandSource> context, int percent) {
		config.setKeepWeatherChance(percent);

		final String message = String.format("weather continuation chance is now at %d%%", config.getKeepWeatherChance());
		context.getSource().sendMessage(Text.literal(message));
		LOGGER.info(message);
	}
}
