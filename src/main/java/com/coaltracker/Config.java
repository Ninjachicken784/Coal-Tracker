package com.coaltracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.coaltracker.core.CraftTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/** Saved settings and total collection (config/coaltracker.json). */
public final class Config {
	public boolean showHud = true;
	public int hudX = 4;
	public int hudY = 40;
	public CraftTarget target = CraftTarget.ENCHANTED_COAL;
	/** true: sell instantly and buy sulphur instantly. false: sell offer and buy order. */
	public boolean instantPrices = true;
	/** Total coal collection, -1 until the player types it in. */
	public long collection = -1;
	/** Prices typed in by hand, by bazaar id. These replace the bazaar price. */
	public Map<String, Double> manualPrices = new HashMap<>();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Config instance = new Config();

	public static Config get() {
		return instance;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("coaltracker.json");
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) return;
		try {
			Config loaded = GSON.fromJson(Files.readString(path), Config.class);
			if (loaded != null) instance = loaded;
		} catch (Exception e) {
			System.err.println("[coaltracker] Could not read config, using defaults: " + e);
		}
		if (instance.target == null) instance.target = CraftTarget.ENCHANTED_COAL;
		if (instance.manualPrices == null) instance.manualPrices = new HashMap<>();
	}

	public static void save() {
		try {
			Files.writeString(file(), GSON.toJson(instance));
		} catch (IOException e) {
			System.err.println("[coaltracker] Could not save config: " + e);
		}
	}
}
