package com.coaltracker;

import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class CoalMod implements ClientModInitializer {
	private static Supplier<Screen> nextScreen;

	@Override
	public void onInitializeClient() {
		Config.load();
		Hud.register();

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!overlay) Tracker.onChat(message);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Tracker.SESSION.breakStretch());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				Prices.refreshIfOld(System.currentTimeMillis());
				Tracker.readMenu(client);
			}
			// Opened on the next tick, otherwise closing the chat would close it too.
			if (nextScreen != null) {
				client.setScreen(nextScreen.get());
				nextScreen = null;
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("coaltracker")
						.executes(context -> {
							nextScreen = MenuScreen::new;
							return 1;
						})
						.then(ClientCommands.literal("reset").executes(context -> {
							Tracker.SESSION.reset();
							context.getSource().sendFeedback(Component.literal("[Coal Tracker] Session reset. Total collection kept."));
							return 1;
						}))
						.then(ClientCommands.literal("debug").executes(context -> {
							Minecraft.getInstance().keyboardHandler.setClipboard(Tracker.debugText());
							context.getSource().sendFeedback(Component.literal("[Coal Tracker] Debug text copied."));
							return 1;
						}))));
	}
}
