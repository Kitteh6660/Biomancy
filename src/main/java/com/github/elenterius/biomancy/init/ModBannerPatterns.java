package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBannerPatterns {

	public static final DeferredRegister<BannerPattern> BANNERS = DeferredRegister.create(Registry.BANNER_PATTERN_REGISTRY, BiomancyMod.MOD_ID);
	public static final RegistryObject<BannerPattern> MASCOT = register("mascot");
	public static final TagKey<BannerPattern> MASCOT_TAG = createTagKey(MASCOT);
	public static final RegistryObject<BannerPattern> MASCOT_ACCENT = register("mascot_accent");
	public static final TagKey<BannerPattern> MASCOT_ACCENT_TAG = createTagKey(MASCOT_ACCENT);
	public static final RegistryObject<BannerPattern> MASCOT_OUTLINE = register("mascot_outline");
	public static final TagKey<BannerPattern> MASCOT_OUTLINE_TAG = createTagKey(MASCOT_OUTLINE);

	private ModBannerPatterns() {}

	private static RegistryObject<BannerPattern> register(String name) {
		return BANNERS.register(name, () -> new BannerPattern(BiomancyMod.MOD_ID + "_" + name));
	}

	private static TagKey<BannerPattern> createTagKey(RegistryObject<BannerPattern> registryObject) {
		ResourceLocation registryName = registryObject.getId();
		String modId = registryName.getNamespace();
		String name = registryName.getPath();
		return TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(modId, modId + "_" + name));
	}

}
