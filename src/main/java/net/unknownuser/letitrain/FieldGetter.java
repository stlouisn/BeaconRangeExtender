package net.unknownuser.letitrain;

import com.google.gson.*;
import net.minecraft.util.math.*;

// utility class
class FieldGetter {
	private static final String WARN_FORMAT = "config entry {} is missing, using '{}' as value";

	private final JsonObject object;
	private boolean hasError;

	public FieldGetter(JsonObject object) {
		this.object = object;
		this.hasError = false;
	}

	private JsonElement get(String name) {
		JsonElement value = object.get(name);

		if (value == null) {
			this.hasError = true;
			return null;
		}

		return value;
	}

	public int getInt(String name, int fallback) {
		JsonElement element = get(name);

		if (element == null) {
			LetItRain.LOGGER.warn(WARN_FORMAT, name, fallback);
			return fallback;
		}

		return clampWarn(element.getAsInt(), name);
	}

	public boolean getBool(String name, boolean fallback) {
		JsonElement element = get(name);

		if (element == null) {
			LetItRain.LOGGER.warn(WARN_FORMAT, name, fallback);
			return fallback;
		}

		return element.getAsBoolean();
	}

	public boolean hasError() {
		return hasError;
	}

	private int clampWarn(int value, String name) {
		int clamped = MathHelper.clamp(value, Config.Defaults.NEVER_KEEP, Config.Defaults.ALWAYS_KEEP);

		if (clamped != value) {
			this.hasError = true;
			LetItRain.LOGGER.info("value for {} is not in range 0-100 (is {}, using {})", name, value, clamped);
		}

		return clamped;
	}
}
