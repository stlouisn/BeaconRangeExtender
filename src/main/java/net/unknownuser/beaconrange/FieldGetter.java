package net.unknownuser.beaconrange;

import com.google.gson.*;

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
			BeaconRange.LOGGER.warn(WARN_FORMAT, name, fallback);
			return fallback;
		}

		return element.getAsInt();
	}

	public boolean getBool(String name, boolean fallback) {
		JsonElement element = get(name);

		if (element == null) {
			BeaconRange.LOGGER.warn(WARN_FORMAT, name, fallback);
			return fallback;
		}

		return element.getAsBoolean();
	}

	public boolean hasError() {
		return hasError;
	}
}
