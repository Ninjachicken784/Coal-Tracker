package com.coaltracker.core;

/** Coal gained and the time spent mining it. */
public final class CoalSession {
	/** With no coal for this long, the timer counts as paused. */
	public static final long IDLE_MS = 90_000;

	private long coal;
	private long activeMs;
	private long lastGainMs = -1;

	public void gain(long amount, int windowSeconds, long now) {
		if (amount <= 0) return;
		if (lastGainMs >= 0 && now - lastGainMs <= IDLE_MS) {
			activeMs += now - lastGainMs;
		} else {
			// First message, or first after a break: count only the time the message covers.
			activeMs += Math.max(1, windowSeconds) * 1000L;
		}
		lastGainMs = now;
		coal += amount;
	}

	/** Stops the current stretch, so time until the next gain is not counted. */
	public void breakStretch() {
		lastGainMs = -1;
	}

	public void reset() {
		coal = 0;
		activeMs = 0;
		lastGainMs = -1;
	}

	public long coal() {
		return coal;
	}

	public long activeMs() {
		return activeMs;
	}

	public boolean idle(long now) {
		return lastGainMs < 0 || now - lastGainMs > IDLE_MS;
	}

	/** Turns a session total into a per hour rate. -1 before any gain. */
	public double perHour(double total) {
		return activeMs <= 0 ? -1 : total * 3_600_000.0 / activeMs;
	}
}
