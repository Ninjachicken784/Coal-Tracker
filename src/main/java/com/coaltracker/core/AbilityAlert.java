package com.coaltracker.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Remembers whether a mining ability is ready. Only using an ability clears it. */
public final class AbilityAlert {
	private static final String NAMES = "(Mining Speed Boost|Pickobulus|Tunnel Vision|Maniac Miner|Gemstone Infusion"
			+ "|Sheer Force|Hazardous Miner|Vein Seeker)";
	// The whole line has to match, so player chat cannot set it off.
	private static final Pattern READY = Pattern.compile("^" + NAMES + " is now available!$");
	private static final Pattern USED = Pattern.compile("^You used your " + NAMES + " Pickaxe Ability!$");

	private String ready;

	public void chat(String message) {
		String line = SackParser.clean(message).trim();
		Matcher m = READY.matcher(line);
		if (m.matches()) ready = m.group(1);
		else if (USED.matcher(line).matches()) ready = null;
	}

	/** The ready ability's name, or null. */
	public String ready() {
		return ready;
	}
}
