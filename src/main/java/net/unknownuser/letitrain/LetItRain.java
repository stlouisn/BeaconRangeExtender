package net.unknownuser.letitrain;

import net.fabricmc.api.*;
import org.slf4j.*;

import java.util.*;

public class LetItRain implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LetItRain");
	public static final Random RANDOM = new Random();

	@Override
	public void onInitialize() {
		Config.init();

		LOGGER.info("Let It Rain loaded!");
	}

	public static void logRoll(String format, Object... objects) {
		if (Config.logRolls()) {
			LOGGER.info(format, objects);
		}
	}
}
