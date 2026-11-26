package com.coaltracker;

import java.util.Locale;

import com.coaltracker.core.CraftTarget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The /coaltracker menu. */
public class MenuScreen extends Screen {
	private static final int WIDTH = 220;
	private static final int ROW = 22;
	private static final int TOP = 24;

	public MenuScreen() {
		super(Component.literal("Coal Tracker"));
	}

	@Override
	protected void init() {
		Config config = Config.get();
		CraftTarget target = config.target;
		int left = this.width / 2 - WIDTH / 2;

		button("Crafting: " + target.display, left, row(0), WIDTH, () -> {
			config.target = target.next();
			Config.save();
			this.rebuildWidgets();
		});
		button("Prices: " + (config.instantPrices ? "Instant sell / instant buy" : "Sell offer / buy order"), left, row(1), WIDTH, () -> {
			config.instantPrices = !config.instantPrices;
			Config.save();
			this.rebuildWidgets();
		});

		priceBox(target.bazaarId, left, row(2));
		if (target.sulphurPerCraft > 0) priceBox(CraftTarget.SULPHUR_ID, left, row(3));

		EditBox collection = box(left, row(4), config.collection >= 0 ? Long.toString(config.collection) : "");
		collection.setResponder(text -> {
			double value = parse(text);
			config.collection = value < 0 ? -1 : (long) value;
			Config.save();
		});

		int half = WIDTH / 2 - 2;
		button(Tracker.paused ? "Resume" : "Pause", left, row(5), half, () -> {
			Tracker.togglePause();
			this.rebuildWidgets();
		});
		button("Reset session", left + half + 4, row(5), half, () -> Tracker.SESSION.reset());
		button("GUI: " + (config.showHud ? "On" : "Off"), left, row(6), half, () -> {
			config.showHud = !config.showHud;
			Config.save();
			this.rebuildWidgets();
		});
		button("Move GUI", left + half + 4, row(6), half, () -> this.minecraft.setScreen(new MoveScreen()));
		button("Ability popup: " + (config.abilityPopup ? "On" : "Off"), left, row(7), WIDTH, () -> {
			config.abilityPopup = !config.abilityPopup;
			Config.save();
			this.rebuildWidgets();
		});
		button("Done", this.width / 2 - 50, row(8), 100, this::onClose);
	}

	private static int row(int index) {
		return TOP + index * ROW;
	}

	private void button(String label, int x, int y, int width, Runnable action) {
		this.addRenderableWidget(Button.builder(Component.literal(label), pressed -> action.run())
				.pos(x, y).size(width, 20).build());
	}

	/** A text box on the right half of a row; the label is drawn on the left half. */
	private EditBox box(int left, int y, String value) {
		EditBox box = new EditBox(this.font, left + WIDTH / 2, y, WIDTH / 2, 20, Component.literal("value"));
		box.setMaxLength(15);
		box.setValue(value);
		this.addRenderableWidget(box);
		return box;
	}

	/** Empty means: use the bazaar price. */
	private void priceBox(String id, int left, int y) {
		Double manual = Config.get().manualPrices.get(id);
		EditBox box = box(left, y, manual == null ? "" : String.format(Locale.US, "%.1f", manual));
		box.setResponder(text -> {
			double value = parse(text);
			if (value < 0) Config.get().manualPrices.remove(id);
			else Config.get().manualPrices.put(id, value);
			Config.save();
		});
	}

	/** -1 if empty or not a number. */
	private static double parse(String text) {
		try {
			double value = Double.parseDouble(text.replace(",", "").trim());
			return value >= 0 && Double.isFinite(value) ? value : -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static String shown(double price) {
		return price < 0 ? "unknown" : String.format(Locale.US, "%,.1f", price);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xAA000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		CraftTarget target = Config.get().target;
		int left = this.width / 2 - WIDTH / 2;
		graphics.centeredText(this.font, "Coal Tracker", this.width / 2, 10, 0xFFFFAA00);
		label(graphics, "Sell: " + shown(Prices.sell(target.bazaarId)), left, row(2));
		if (target.sulphurPerCraft > 0) {
			label(graphics, "E.Sulphur: " + shown(Prices.buy(CraftTarget.SULPHUR_ID)), left, row(3));
		}
		label(graphics, "Total collection", left, row(4));
		graphics.centeredText(this.font, "Price boxes: leave empty to use the bazaar", this.width / 2, row(9), 0xFFAAAAAA);
	}

	private void label(GuiGraphicsExtractor graphics, String text, int x, int y) {
		graphics.text(this.font, text, x, y + 6, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		Config.save();
		super.onClose();
	}
}
