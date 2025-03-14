package com.github.elenterius.biomancy.init.tags;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
	public static final TagKey<Block> FLESHY_FENCES = tag("fleshy_fences");

	//CONVERTABLE_TO_PRIMAL_FLESH
	//CONVERTABLE_TO_MALIGNANT_FLESH
	public static final TagKey<Block> PRIMORDIAL_ECO_SYSTEM_REPLACEABLE = tag("primordial_ecosystem_replaceable");

	private ModBlockTags() {}

	private static TagKey<Block> tag(String name) {
		return BlockTags.create(BiomancyMod.createRL(name));
	}

}
