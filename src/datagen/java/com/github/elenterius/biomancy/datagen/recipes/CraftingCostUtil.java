package com.github.elenterius.biomancy.datagen.recipes;

import net.minecraft.util.Mth;

public final class CraftingCostUtil {
	public static final int SIXTY_SECONDS_IN_TICKS = 20 * 60;

	private CraftingCostUtil() {}

	public static int getCostMultiplier(int craftingTimeInTicks) {
		return 1 + Mth.floor(craftingTimeInTicks / (float) SIXTY_SECONDS_IN_TICKS);
	}

	public static int getCost(int baseCost, int craftingTimeInTicks) {
		return baseCost * getCostMultiplier(craftingTimeInTicks);
	}

}
