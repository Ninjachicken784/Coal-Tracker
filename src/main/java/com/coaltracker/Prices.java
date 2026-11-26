package com.coaltracker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.coaltracker.core.CraftTarget;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Bazaar prices from Hypixel's public API (no key needed). */
public final class Prices {
	private static final String URL = "https://api.hypixel.net/v2/skyblock/bazaar";
	private static final long REFRESH_MS = 5 * 60_000;

	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	/** bazaar id to {instant sell price, instant buy price} */
	private static volatile Map<String, double[]> bazaar = Map.of();
	private static long lastRequest;

	private Prices() { }

	public static void refreshIfOld(long now) {
		if (now - lastRequest >= REFRESH_MS) refresh(now);
	}

	public static void refresh(long now) {
		lastRequest = now;
		HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).timeout(Duration.ofSeconds(20)).build();
		HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenAccept(response -> {
					if (response.statusCode() == 200) bazaar = parse(response.body());
					else System.err.println("[coaltracker] Bazaar request failed: " + response.statusCode());
				})
				.exceptionally(e -> {
					System.err.println("[coaltracker] Bazaar request failed: " + e);
					return null;
				});
	}

	private static Map<String, double[]> parse(String body) {
		JsonObject products = JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("products");
		Map<String, double[]> out = new HashMap<>();
		for (String id : products.keySet()) {
			JsonObject status = products.getAsJsonObject(id).getAsJsonObject("quick_status");
			// In this API "sellPrice" is what you get selling instantly, "buyPrice" what you pay buying instantly.
			out.put(id, new double[] { status.get("sellPrice").getAsDouble(), status.get("buyPrice").getAsDouble() });
		}
		return out;
	}

	/** Price for selling the product, -1 if unknown. */
	public static double sell(String id) {
		return price(id, Config.get().instantPrices ? 0 : 1);
	}

	/** Price for buying an ingredient, -1 if unknown. */
	public static double buy(String id) {
		return price(id, Config.get().instantPrices ? 1 : 0);
	}

	private static double price(String id, int index) {
		Double manual = Config.get().manualPrices.get(id);
		if (manual != null) return manual;
		double[] found = bazaar.get(id);
		return found == null || found[index] <= 0 ? -1 : found[index];
	}

	/** Coins from this much raw coal with the chosen craft. NaN if a price is unknown. */
	public static double money(long coal) {
		CraftTarget target = Config.get().target;
		return target.money(coal, sell(target.bazaarId), buy(CraftTarget.SULPHUR_ID));
	}
}
