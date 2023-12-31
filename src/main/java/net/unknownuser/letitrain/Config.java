package net.unknownuser.letitrain;

import com.google.gson.*;
import com.google.gson.annotations.*;
import net.fabricmc.loader.api.*;
import net.minecraft.util.math.*;

import java.io.*;

public class Config {
	private static Config INSTANCE = null;

	public static final int ALWAYS_RESTORE = 100;
	public static final int NEVER_RESTORE = 0;

	private Config() {
		keepWeatherChance = ALWAYS_RESTORE;
	}

	@Expose
	protected int keepWeatherChance;

	public int getKeepWeatherChance() {
		return keepWeatherChance;
	}

	public void setKeepWeatherChance(int keepWeatherChance) {
		this.keepWeatherChance = MathHelper.clamp(keepWeatherChance, NEVER_RESTORE, ALWAYS_RESTORE);
		writeConfig();
	}

	public boolean isEnabled() {
		return getKeepWeatherChance() != NEVER_RESTORE;
	}

	public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("let-it-rain.json").toFile();

	public static Config getInstance() {
		if (INSTANCE == null) {
			INSTANCE = getConfigFromFile();
		}

		return INSTANCE;
	}

	protected static Config getConfigFromFile() {
		Config cfg;

		if (CONFIG_FILE.exists()) {
			// Gson uses setters
			// no verification needed, if already in setters
			cfg = loadConfig();
		} else {
			LetItRain.LOGGER.info("config file is missing, creating default");
			cfg = new Config();
			cfg.writeConfig();
		}

		return cfg;
	}

	protected static Config loadConfig() {
		Gson gson = new Gson();
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			return gson.fromJson(reader, Config.class);
		} catch (IOException e) {
			LetItRain.LOGGER.error("Could not read config file! Reason: %s", e);
			throw new RuntimeException(e);
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
}
