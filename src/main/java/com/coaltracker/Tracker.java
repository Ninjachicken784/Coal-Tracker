package com.coaltracker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import com.coaltracker.core.AbilityAlert;
import com.coaltracker.core.CoalSession;
import com.coaltracker.core.CollectionMenu;
import com.coaltracker.core.SackParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/** Connects Hypixel's sack messages to the session. */
public final class Tracker {
	public static final CoalSession SESSION = new CoalSession();
	public static final AbilityAlert ABILITY = new AbilityAlert();
	public static boolean paused;
	/** The menu the total was already taken from, so it is read once each time it is opened. */
	private static Screen readScreen;

	private static final int KEPT = 5;
	private static final Deque<String> recent = new ArrayDeque<>();

	private Tracker() { }

	public static void onChat(Component message) {
		String text = message.getString();
		ABILITY.chat(text);
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

	/** Takes the total collection from the Coal Collection menu. */
	public static void readMenu(Minecraft mc) {
		if (!(mc.screen instanceof AbstractContainerScreen<?> screen) || screen == readScreen) return;
		if (!SackParser.clean(screen.getTitle().getString()).trim().equals(CollectionMenu.TITLE)) return;
		for (Slot slot : screen.getMenu().slots) {
			if (!slot.hasItem()) continue;
			long total = CollectionMenu.read(itemLines(slot.getItem()));
			if (total < 0) continue;
			readScreen = screen;
			Config.get().collection = total;
			Config.save();
			mc.player.sendSystemMessage(Component.literal(
					"[Coal Tracker] Coal collection set to " + String.format(Locale.US, "%,d", total) + "."));
			return;
		}
	}

	/** The item name followed by its description lines. */
	private static List<String> itemLines(ItemStack stack) {
		List<String> lines = new ArrayList<>();
		lines.add(stack.getHoverName().getString());
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore != null) {
			for (Component line : lore.lines()) lines.add(line.getString());
		}
		return lines;
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
