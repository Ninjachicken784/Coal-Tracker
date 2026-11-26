package com.coaltracker.core;

/** What the mined coal gets turned into. One craft uses coalPerCraft raw coal. */
public enum CraftTarget {
	ENCHANTED_COAL("Enchanted Coal", "ENCHANTED_COAL", 160, 1, 0),
	COAL_BLOCK("Block of Coal", "COAL_BLOCK", 9, 1, 0),
	ENCHANTED_COAL_BLOCK("Enchanted Block of Coal", "ENCHANTED_COAL_BLOCK", 160 * 160, 1, 0),
	// 16 Enchanted Coal + 1 Enchanted Sulphur = 4 Sulphuric Coal
	SULPHURIC_COAL("Sulphuric Coal", "SULPHURIC_COAL", 16 * 160, 4, 1);

	public static final String SULPHUR_ID = "ENCHANTED_SULPHUR";

	public final String display;
	public final String bazaarId;
	public final int coalPerCraft;
	public final int outputPerCraft;
	public final int sulphurPerCraft;

	CraftTarget(String display, String bazaarId, int coalPerCraft, int outputPerCraft, int sulphurPerCraft) {
		this.display = display;
		this.bazaarId = bazaarId;
		this.coalPerCraft = coalPerCraft;
		this.outputPerCraft = outputPerCraft;
		this.sulphurPerCraft = sulphurPerCraft;
	}

	public CraftTarget next() {
		return values()[(ordinal() + 1) % values().length];
	}

	/** How many of the product this much coal makes (fractions count, so money rises smoothly). */
	public double items(long coal) {
		return (double) coal / coalPerCraft * outputPerCraft;
	}

	public double sulphurNeeded(long coal) {
		return (double) coal / coalPerCraft * sulphurPerCraft;
	}

	/** Coins from this much coal. NaN if a needed price is unknown (negative). */
	public double money(long coal, double productPrice, double sulphurPrice) {
		if (productPrice < 0 || (sulphurPerCraft > 0 && sulphurPrice < 0)) return Double.NaN;
		double cost = sulphurPerCraft > 0 ? sulphurNeeded(coal) * sulphurPrice : 0;
		return items(coal) * productPrice - cost;
	}
}
