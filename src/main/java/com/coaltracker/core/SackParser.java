package com.coaltracker.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads Hypixel's "[Sacks]" chat message and the item list in its hover text. */
public final class SackParser {
	private static final Pattern ITEM = Pattern.compile("^\\s*([+-][\\d,]+)\\s+(.+?)\\s+\\([^()]*Sacks?[^()]*\\)\\s*$");
	private static final Pattern WINDOW = Pattern.compile("\\(Last (\\d+)s\\.?\\)");

	private SackParser() { }

	public static String clean(String text) {
		return text.replaceAll("§.", "");
	}

	public static boolean isSackMessage(String message) {
		return clean(message).startsWith("[Sacks]");
	}

	/** Raw coal in one of this item, or 0 if it is not coal. */
	public static int coalValue(String item) {
		return switch (item.toLowerCase(Locale.ROOT)) {
			case "coal" -> 1;
			case "block of coal" -> 9;
			case "enchanted coal" -> 160;
			case "enchanted block of coal", "enchanted coal block" -> 160 * 160;
			default -> 0;
		};
	}

	/**
	 * Coal gained, in raw coal. Removed coal is subtracted, so compacting inside the sacks is not counted twice.
	 * The same hover text can be attached to several parts of a message, so repeats are dropped.
	 */
	public static long netCoal(Collection<String> hoverTexts) {
		long total = 0;
		for (String hover : new LinkedHashSet<>(hoverTexts)) {
			for (String line : clean(hover).split("\n")) {
				Matcher m = ITEM.matcher(line);
				if (!m.matches()) continue;
				total += Long.parseLong(m.group(1).replace(",", "").replace("+", "")) * coalValue(m.group(2));
			}
		}
		return total;
	}

	/** The time the message covers. */
	public static int windowSeconds(String message) {
		Matcher m = WINDOW.matcher(clean(message));
		return m.find() ? Integer.parseInt(m.group(1)) : 30;
	}
}
