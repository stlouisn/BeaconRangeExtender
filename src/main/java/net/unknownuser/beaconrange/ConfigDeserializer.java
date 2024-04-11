package net.unknownuser.beaconrange;

import com.google.gson.*;

import java.lang.reflect.*;

// if this does not exist, Gson uses type defaults for each variable
// I hate this
// just use Jackson.

// This is technically not needed, but I hate whatever Gson is doing too much to just keep it
public class ConfigDeserializer implements JsonDeserializer<Config> {
	@Override
	public Config deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		JsonObject object = jsonElement.getAsJsonObject();
		FieldGetter getter = new FieldGetter(object);

		int rangePerLevel = getter.getInt("rangePerLevel", Config.Defaults.RANGE_PER_LEVEL);
		int baseOffset = getter.getInt("baseOffset", Config.Defaults.BASE_OFFSET);

		Config config = new Config(
			rangePerLevel,
			baseOffset
		);

		if (getter.hasError()) {
			config.hasError();
		}

		return config;
	}
}
