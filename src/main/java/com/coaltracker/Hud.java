package com.coaltracker;

import java.util.Locale;

import com.coaltracker.core.CoalSession;
import com.coaltracker.core.CraftTarget;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/** The small text box. */
public final class Hud {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GOLD = 0xFFFFAA00;

	private Hud() { }

	public static void register() {
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.SUBTITLES,
				Identifier.fromNamespaceAndPath("coaltracker", "hud"),
				Hud::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen instanceof MoveScreen) return;
		// Hidden until the first coal of the session.
		if (!Config.get().showHud || Tracker.SESSION.coal() == 0) return;
		draw(graphics, mc, x(mc, graphics.guiWidth()), y(mc, graphics.guiHeight()));
	}

	public static String[] lines() {
		CoalSession session = Tracker.SESSION;
		CraftTarget target = Config.get().target;
		long coal = session.coal();
		long total = Config.get().collection;
		double money = Prices.money(coal);
		boolean priced = !Double.isNaN(money);
		double rate = session.perHour(coal);

		String status = Tracker.paused ? " (paused)" : session.idle(System.currentTimeMillis()) ? " (idle)" : "";
		return new String[] {
			"Coal Tracker" + status,
			"Collection: " + (total >= 0 ? number(total) + " (+" + number(coal) + ")" : "+" + number(coal)),
			"Collection/h: " + (rate < 0 ? "--" : number(Math.round(rate))),
			"Crafting: " + target.display + " (" + String.format(Locale.US, "%,.1f", target.items(coal)) + ")",
			"Money: " + (priced ? number(Math.round(money)) : "no price yet"),
			"Money/h: " + (priced && rate >= 0 ? number(Math.round(session.perHour(money))) : "--")
		};
	}

	private static String number(long value) {
		return String.format(Locale.US, "%,d", value);
	}

	public static int width(Minecraft mc) {
		int widest = 0;
		for (String line : lines()) widest = Math.max(widest, mc.font.width(line));
		return widest;
	}

	public static int height(Minecraft mc) {
		return lines().length * (mc.font.lineHeight + 1);
	}

	/** Saved position, kept inside the screen. */
	public static int x(Minecraft mc, int screenWidth) {
		return Math.max(0, Math.min(Config.get().hudX, screenWidth - width(mc)));
	}

	public static int y(Minecraft mc, int screenHeight) {
		return Math.max(0, Math.min(Config.get().hudY, screenHeight - height(mc)));
	}

	public static void draw(GuiGraphicsExtractor graphics, Minecraft mc, int x, int y) {
		String[] lines = lines();
		for (int i = 0; i < lines.length; i++) {
			graphics.text(mc.font, lines[i], x, y + i * (mc.font.lineHeight + 1), i == 0 ? GOLD : WHITE);
		}
	}
}
