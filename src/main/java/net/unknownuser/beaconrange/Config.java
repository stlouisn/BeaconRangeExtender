package net.unknownuser.beaconrange;

import com.google.gson.*;
import com.google.gson.annotations.*;
import net.fabricmc.loader.api.*;

import java.io.*;

public class Config implements Serializable {
	public static class Defaults {
		private Defaults() {}

		public static final int RANGE_PER_LEVEL = 100;
		public static final int BASE_OFFSET = 0;

		private static Config config() {
			return new Config(
				RANGE_PER_LEVEL,
				BASE_OFFSET
			);
		}
	}

	public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("beacon-range-extender.json").toFile();
	private static Config instance = null;
	
	public Config(int rangePerLevel, int baseOffset) {
		this.rangePerLevel = rangePerLevel;
		this.baseOffset = baseOffset;
	}
	
	@Expose
	protected final int rangePerLevel;
	protected final int baseOffset;
	// Gson *will* write all fields, even when enabling ignoreNonExposed
	// use transient to even skip the inclusion
	private transient boolean hasError = false;

	public void hasError() {
		hasError = true;
	}
	
	public static int rangePerLevel() {
		return instance.rangePerLevel;
	}
	
	public static int baseOffset() {
		return instance.baseOffset;
	}

	private static Config getConfigFromFile() {
		Config cfg;

		if (CONFIG_FILE.exists()) {
			BeaconRange.LOGGER.info("using existing config file");
			cfg = loadConfig();
		} else {
			BeaconRange.LOGGER.info("config file is missing, creating using default settings!");
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
				BeaconRange.LOGGER.warn("config has errors, rewriting config file");
				config.write();
			}

			return config;
		} catch (IOException e) {
			BeaconRange.LOGGER.error("Could not read config file: {}", e.getMessage());
			BeaconRange.LOGGER.warn("Using default values for settings!");

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
			BeaconRange.LOGGER.error("Could not write config file! Reason: %s", e);
		}
	}
	
	@Override
	public String toString() {
		return "Config{" + "rangePerLevel=" + rangePerLevel + ", baseOffset=" + baseOffset + ", hasError=" + hasError + '}';
	}
}
