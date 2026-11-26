package com.coaltracker.core;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads "Total Collected" from the Coal item in the "Coal Collection" menu. */
public final class CollectionMenu {
	public static final String TITLE = "Coal Collection";
	private static final Pattern NAME = Pattern.compile("^Coal(?: [IVXL]+)?$");
	private static final Pattern TOTAL = Pattern.compile("^Total Collected: ([\\d,]{1,19})$");

	private CollectionMenu() { }

	/** Lines are the item name followed by its description. -1 if this is not the coal item. */
	public static long read(List<String> lines) {
		if (lines.isEmpty() || !NAME.matcher(SackParser.clean(lines.get(0)).trim()).matches()) return -1;
		for (String line : lines) {
			Matcher m = TOTAL.matcher(SackParser.clean(line).trim());
			if (m.matches()) return Long.parseLong(m.group(1).replace(",", ""));
		}
		return -1;
	}
}
