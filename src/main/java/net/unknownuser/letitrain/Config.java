package net.unknownuser.letitrain;

import com.google.gson.*;
import com.google.gson.annotations.*;
import net.fabricmc.loader.api.*;

import java.io.*;

public class Config implements Serializable {
	public static class Defaults {
		private Defaults() {
		}

		public static final int KEEP_RAIN_CHANCE = 100;
		public static final int KEEP_THUNDER_CHANCE = 100;
		public static final boolean PRESERVE_WEATHER_TIME = true;
		public static final boolean LOG_ROLLS = false;

		public static final int ALWAYS_KEEP = 100;
		public static final int NEVER_KEEP = 0;

		private static Config config() {
			return new Config(
					KEEP_RAIN_CHANCE,
					KEEP_THUNDER_CHANCE,
					PRESERVE_WEATHER_TIME,
					LOG_ROLLS
			);
		}
	}

	public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("let-it-rain.json").toFile();
	private static Config instance = null;

	public Config(int keepRainChance, int keepThunderChance, boolean preserveWeatherTime, boolean logRolls) {
		this.keepRainChance = keepRainChance;
		this.keepThunderChance = keepThunderChance;
		this.preserveWeatherTime = preserveWeatherTime;
		this.logRolls = logRolls;
	}

	@Expose
	protected final int keepRainChance;
	@Expose
	protected final int keepThunderChance;
	@Expose
	protected final boolean preserveWeatherTime;
	@Expose
	protected final boolean logRolls;
	// Gson *will* write all fields, even when enabling ignoreNonExposed
	// use transient to even skip the inclusion
	private transient boolean hasError = false;

	public void hasError() {
		hasError = true;
	}

	public static int keepRainChance() {
		return instance.keepRainChance;
	}

	public static int keepThunderChance() {
		return instance.keepThunderChance;
	}

	public static boolean preserveWeatherTime() {
		return instance.preserveWeatherTime;
	}

	public static boolean logRolls() {
		return instance.logRolls;
	}

	private static Config getConfigFromFile() {
		Config cfg;

		if (CONFIG_FILE.exists()) {
			LetItRain.LOGGER.info("using existing config file");
			cfg = loadConfig();
		} else {
			LetItRain.LOGGER.info("config file is missing, creating using default settings!");
			cfg = Defaults.config();
			cfg.write();
		}

		return cfg;
	}

	private static Config loadConfig() {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Config.class, new ConfigDeserializer())
				.excludeFieldsWithoutExposeAnnotation()
				.create();
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			Config config = gson.fromJson(reader, Config.class);

			if (config.hasError) {
				LetItRain.LOGGER.warn("config has errors, rewriting config file");
				config.write();
			}

			return config;
		} catch (IOException e) {
			LetItRain.LOGGER.error("Could not read config file: {}", e.getMessage());
			LetItRain.LOGGER.warn("Using default values for settings!");

			Config config = Defaults.config();
			config.write();
			return config;
		}
	}

	// Fabric need to be fully initialized
	// loading during class-initialization will break FabricLoader and therefore the config dir
	public static void init() {
		instance = getConfigFromFile();
	}

	public void write() {
		Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			gson.toJson(this, writer);
		} catch (IOException e) {
			LetItRain.LOGGER.error("Could not write config file! Reason: %s", e);
		}
	}

	@Override
	public String toString() {
		return "Config{" +
				"keepRainChance=" + keepRainChance +
				", keepThunderChance=" + keepThunderChance +
				", preserveWeatherTime=" + preserveWeatherTime +
				", logRolls=" + logRolls +
				'}';
	}
}
