package com.github.elenterius.biomancy.datagen.particles;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModParticleSpriteProvider extends ParticleSpriteProvider {
	public ModParticleSpriteProvider(DataGenerator generator, ExistingFileHelper fileHelper) {
		super(generator, BiomancyMod.MOD_ID, fileHelper);
	}

	@Override
	public void registerParticles() {
		addParticle(ModParticleTypes.BLOODY_CLAWS_ATTACK, 8, 1);
		addParticle(ModParticleTypes.FALLING_BLOOD, "minecraft:drip_fall");
		addParticle(ModParticleTypes.LANDING_BLOOD, "minecraft:drip_land");
		addParticle(ModParticleTypes.CORROSIVE_SWIPE_ATTACK, 8, 1);
	}

}
