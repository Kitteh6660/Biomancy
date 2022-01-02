package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.world.block.CreatorBlock;
import com.github.elenterius.biomancy.world.block.FleshBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BiomancyMod.MOD_ID);

	//# Plant Types
	public static final PlantType FLESH_PLANT_TYPE = PlantType.get("flesh");

	//# Materials
	public static final Material FLESH_MATERIAL = new Material.Builder(MaterialColor.COLOR_PINK).build();

	//# Block Properties
	public static final BooleanProperty CRAFTING_PROPERTY = BooleanProperty.create("crafting");

	//# Blocks
	public static final RegistryObject<Block> FLESH_BLOCK = BLOCKS.register("flesh_block", () -> new FleshBlock(createFleshProperties()));
	public static final RegistryObject<SlabBlock> FLESH_BLOCK_SLAB = BLOCKS.register("flesh_block_slab", () -> new SlabBlock(createFleshProperties()));
	public static final RegistryObject<StairBlock> FLESH_BLOCK_STAIRS = BLOCKS.register("flesh_block_stairs", () -> new StairBlock(() -> FLESH_BLOCK.get().defaultBlockState(), createFleshProperties()));
	public static final RegistryObject<Block> NECROTIC_FLESH_BLOCK = BLOCKS.register("necrotic_flesh_block", () -> new FleshBlock(createFleshProperties()));

	//## Machines
	public static final RegistryObject<CreatorBlock> CREATOR = BLOCKS.register("creator", () -> new CreatorBlock(createFleshProperties()));

	private ModBlocks() {}

	@OnlyIn(Dist.CLIENT)
	static void setRenderLayers() {
		ItemBlockRenderTypes.setRenderLayer(CREATOR.get(), RenderType.cutout());

//		ItemBlockRenderTypes.setRenderLayer(FLESH_TENTACLE.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(FLESHBORN_DOOR.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(FLESHBORN_TRAPDOOR.get(), RenderType.cutout());
//
//		ItemBlockRenderTypes.setRenderLayer(DIGESTER.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(CHEWER.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(SOLIDIFIER.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(EVOLUTION_POOL.get(), RenderType.translucent());
//		ItemBlockRenderTypes.setRenderLayer(SCENT_DIFFUSER.get(), RenderType.cutout());
//		ItemBlockRenderTypes.setRenderLayer(VOICE_BOX.get(), RenderType.translucent());

		//block with "glowing" overlay texture, also needs a overlay model see onModelBakeEvent() in ClientSetupHandler
		//ItemBlockRenderTypes.setRenderLayer(ModBlocks.FOOBAR.get(), renderType -> renderType == RenderType.getCutout() || renderType == RenderType.getTranslucent());
	}

	public static BlockBehaviour.Properties createFleshProperties() {
		return BlockBehaviour.Properties.of(FLESH_MATERIAL).strength(3f, 3f).sound(SoundType.SLIME_BLOCK).isValidSpawn(ModBlocks::limitEntitySpawnToFlesh);
	}

	public static BlockBehaviour.Properties createGlowingPlantProperties(int i) {
		return BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(0.2f).sound(SoundType.GRASS).lightLevel(v -> i);
	}

	public static BlockBehaviour.Properties createFleshPlantProperties() {
		return BlockBehaviour.Properties.of(getReplaceablePlantMat(), MaterialColor.COLOR_PINK).noCollission().strength(0f).sound(SoundType.SLIME_BLOCK);
	}

	private static Material getReplaceablePlantMat() {
		return Material.REPLACEABLE_PLANT; //is flammable
	}

	private static Material getPlantMat() {
		return Material.PLANT; // is not replaceable nor flammable
	}

	public static boolean limitEntitySpawnToFlesh(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entityType) {
//		entityType.getTags().contains(); //TODO: implement this
		return false;
	}

	private static boolean neverAllowSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entityType) {
		return false;
	}

}
