package com.coaltracker.core;

import java.util.List;

public class CoalLogicTest {
	private static int checks;

	private static void check(boolean ok, String name) {
		checks++;
		if (!ok) throw new AssertionError(name);
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < 0.0001;
	}

	public static void main(String[] args) {
		// Sulphuric: 2560 coal + 1 enchanted sulphur = 4 sulphuric coal
		CraftTarget s = CraftTarget.SULPHURIC_COAL;
		check(near(s.items(2560), 4), "sulphuric output");
		check(near(s.sulphurNeeded(2560), 1), "sulphur used");
		check(near(s.money(2560, 1000, 600), 3400), "sulphur cost subtracted");
		check(Double.isNaN(s.money(2560, 1000, -1)), "unknown sulphur price");
		check(near(CraftTarget.ENCHANTED_COAL.money(1600, 500, -1), 5000), "enchanted coal ignores sulphur");
		check(near(CraftTarget.COAL_BLOCK.money(90, 20, -1), 200), "coal block");
		check(near(CraftTarget.ENCHANTED_COAL_BLOCK.items(51200), 2), "enchanted block");

		String added = "Added items:\n  +1,234 Coal (Mining Sack)\n  +2 Enchanted Coal (Mining Sack)\n  +50 Cobblestone (Mining Sack)\n\nThis message can be disabled in the settings.";
		check(SackParser.netCoal(List.of(added)) == 1554, "added coal");
		check(SackParser.netCoal(List.of(added, added)) == 1554, "repeated hover");
		String removed = "Removed items:\n  -160 Coal (Mining Sack)";
		check(SackParser.netCoal(List.of("  +1 Enchanted Coal (Mining Sack)", removed)) == 0, "compacting");
		check(SackParser.netCoal(List.of("§a  +5 §fCoal §7(Mining Sack)")) == 5, "color codes");
		check(SackParser.netCoal(List.of("  +5 Enchanted Charcoal (Mining Sack)")) == 0, "other item");
		check(SackParser.isSackMessage("[Sacks] +5 items. (Last 5s.)"), "sack message");
		check(!SackParser.isSackMessage("Player: [Sacks] +5 items."), "player chat ignored");
		check(SackParser.windowSeconds("[Sacks] +5 items. (Last 5s.)") == 5, "window");

		CoalSession session = new CoalSession();
		check(session.perHour(0) < 0, "no rate yet");
		session.gain(1000, 30, 100_000);
		session.gain(1000, 30, 130_000);
		check(session.activeMs() == 60_000, "active time");
		check(near(session.perHour(session.coal()), 120_000), "per hour");
		check(session.idle(300_000), "idle");
		session.gain(1000, 30, 600_000);
		check(session.activeMs() == 90_000, "break not counted");
		session.breakStretch();
		session.gain(1000, 30, 610_000);
		check(session.activeMs() == 120_000, "pause not counted");
		session.reset();
		check(session.coal() == 0 && session.activeMs() == 0, "reset");

		List<String> item = List.of("Coal X", "View all your Coal Collection", "progress and rewards!", "", "Total Collected: 70,931,544");
		check(CollectionMenu.read(item) == 70_931_544L, "menu total");
		check(CollectionMenu.read(List.of("Coal 5", "Total Collected: 5")) == -1, "other item");
		check(CollectionMenu.read(List.of("Coal X", "Progress")) == -1, "no total line");

		AbilityAlert alert = new AbilityAlert();
		alert.chat("Player: Maniac Miner is now available!");
		check(alert.ready() == null, "player chat");
		alert.chat("\u00a7r\u00a76Maniac Miner \u00a7r\u00a7ais now available!\u00a7r");
		check("Maniac Miner".equals(alert.ready()), "ready");
		alert.chat("You earned 730 GEXP");
		check(alert.ready() != null, "stays");
		alert.chat("You used your Maniac Miner Pickaxe Ability!");
		check(alert.ready() == null, "used");

		System.out.println("All " + checks + " checks passed.");
	}
}
