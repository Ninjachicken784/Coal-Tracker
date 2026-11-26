package com.coaltracker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.coaltracker.core.CoalSession;
import com.coaltracker.core.SackParser;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

/** Connects Hypixel's sack messages to the session. */
public final class Tracker {
	public static final CoalSession SESSION = new CoalSession();
	public static boolean paused;

	private static final int KEPT = 5;
	private static final Deque<String> recent = new ArrayDeque<>();

	private Tracker() { }

	public static void onChat(Component message) {
		String text = message.getString();
		if (!SackParser.isSackMessage(text)) return;

		List<String> hovers = new ArrayList<>();
		for (Component part : message.toFlatList()) {
			if (part.getStyle().getHoverEvent() instanceof HoverEvent.ShowText show) {
				hovers.add(show.value().getString());
			}
		}
		long coal = SackParser.netCoal(hovers);

		recent.addLast(SackParser.clean(text) + "\ncounted coal: " + coal + "\n" + SackParser.clean(String.join("\n--\n", hovers)));
		if (recent.size() > KEPT) recent.removeFirst();

		if (paused || coal <= 0) return;
		SESSION.gain(coal, SackParser.windowSeconds(text), System.currentTimeMillis());
		if (Config.get().collection >= 0) {
			Config.get().collection += coal;
			Config.save();
		}
	}

	public static void togglePause() {
		paused = !paused;
		SESSION.breakStretch();
	}

	/** The last sack messages as the mod read them, for fixing detection. */
	public static String debugText() {
		return "== Coal Tracker debug ==\n" + String.join("\n== next ==\n", recent);
	}
}
