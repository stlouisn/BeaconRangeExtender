package net.unknownuser.letitrain;

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

		int keepRainChance = getter.getInt("keepRainChance", Config.Defaults.KEEP_RAIN_CHANCE);
		int keepThunderChance = getter.getInt("keepThunderChance", Config.Defaults.KEEP_THUNDER_CHANCE);
		boolean preserveWeatherTime = getter.getBool("preserveWeatherTime", Config.Defaults.PRESERVE_WEATHER_TIME);
		boolean logRolls = getter.getBool("logRolls", Config.Defaults.LOG_ROLLS);

		Config config = new Config(
				keepRainChance,
				keepThunderChance,
				preserveWeatherTime,
				logRolls
		);

		if (getter.hasError()) {
			config.hasError();
		}

		return config;
	}
}
