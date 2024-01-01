package net.unknownuser.letitrain;

import com.google.gson.*;
import com.google.gson.annotations.*;
import net.fabricmc.loader.api.*;
import net.minecraft.util.math.*;

import java.io.*;

public class Config {
	public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("let-it-rain.json").toFile();
	private static final Config INSTANCE = getConfigFromFile();

	public static final int ALWAYS_KEEP = 100;
	public static final int NEVER_KEEP = 0;

	private Config() {
		keepRainChance = ALWAYS_KEEP;
		keepThunderChance = ALWAYS_KEEP;
		preserveWeatherTime = true;
		logRolls = true;
	}

	@Expose
	protected int keepRainChance;
	@Expose
	protected int keepThunderChance;
	@Expose
	protected boolean preserveWeatherTime;
	@Expose
	protected boolean logRolls;

	public static int keepRainChance() {
		return INSTANCE.keepRainChance;
	}

	public static int keepThunderChance() {
		return INSTANCE.keepThunderChance;
	}

	public static boolean preserveWeatherTime() {
		return INSTANCE.preserveWeatherTime;
	}

	public static boolean logRolls() {
		return INSTANCE.logRolls;
	}

	protected static Config getConfigFromFile() {
		Config cfg;

		if (CONFIG_FILE.exists()) {
			// Gson uses setters
			// no verification needed, if already in setters
			cfg = loadConfig();
			cfg.verify();
		} else {
			LetItRain.LOGGER.info("config file is missing, creating default");
			cfg = new Config();
			cfg.writeConfig();
		}

		return cfg;
	}

	// The config file has to exist.
	protected static Config loadConfig() {
		Gson gson = new Gson();
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			return gson.fromJson(reader, Config.class);
		} catch (IOException e) {
			LetItRain.LOGGER.error("Could not read config file! Reason: %s", e);
			LetItRain.LOGGER.error("Using default values for settings!");
			return new Config();
		}
	}

	protected void writeConfig() {
		Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			gson.toJson(this, writer);
		} catch (IOException e) {
			LetItRain.LOGGER.error("Could not write config file! Reason: %s", e);
			throw new RuntimeException(e);
		}
	}

	protected void verify() {
		keepRainChance = MathHelper.clamp(keepRainChance, NEVER_KEEP, ALWAYS_KEEP);
		keepThunderChance = MathHelper.clamp(keepThunderChance, NEVER_KEEP, ALWAYS_KEEP);
	}
}
