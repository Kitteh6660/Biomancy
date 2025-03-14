package com.github.elenterius.biomancy.datagen.advancements;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.advancements.trigger.SacrificedItemTrigger;
import com.github.elenterius.biomancy.datagen.lang.LangProvider;
import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.tags.ModEntityTags;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.function.Consumer;

import static com.github.elenterius.biomancy.BiomancyMod.LOGGER;

public class ModAdvancementProvider extends AdvancementProvider {

	private final Marker LOG_MARKER = MarkerManager.getMarker("AdvancementProvider");
	private final LangProvider lang;

	public ModAdvancementProvider(DataGenerator generator, ExistingFileHelper fileHelper, LangProvider lang) {
		super(generator, fileHelper);
		this.lang = lang;
	}

	protected static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... items) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(items);
	}

	protected static InventoryChangeTrigger.TriggerInstance hasTag(TagKey<Item> tag) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
	}

	protected static PlacedBlockTrigger.TriggerInstance hasPlacedBlock(Block block) {
		return PlacedBlockTrigger.TriggerInstance.placedBlock(block);
	}

	protected static KilledTrigger.TriggerInstance hasKilledEntity(EntityType<?> entityType) {
		return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityType).build());
	}

	protected static KilledTrigger.TriggerInstance hasKilledEntityTag(TagKey<EntityType<?>> tag) {
		return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(tag).build());
	}

	protected static SacrificedItemTrigger.TriggerInstance hasSacrificedItem(ItemLike item) {
		return SacrificedItemTrigger.TriggerInstance.sacrificedItem(item);
	}

	protected static SacrificedItemTrigger.TriggerInstance hasSacrificedTag(TagKey<Item> tag) {
		return SacrificedItemTrigger.TriggerInstance.sacrificedItem(tag);
	}

	protected static TradeTrigger.TriggerInstance hasTradedItems(ItemLike... items) {
		return new TradeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, ItemPredicate.Builder.item().of(items).build());
	}

	@Override
	public String getName() {
		return StringUtils.capitalize(BiomancyMod.MOD_ID) + " Advancements";
	}

	@Override
	protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
		LOGGER.info(LOG_MARKER, "registering advancements...");
		try {
			registerBiomancyAdvancements(consumer, fileHelper);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private AdvancementBuilder createAdvancement(String id) {
		return AdvancementBuilder.create(BiomancyMod.MOD_ID, id, lang);
	}

	private void registerBiomancyAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) throws Exception {
		Advancement root = createAdvancement("root").icon(ModItems.FLESH_BITS.get()).background("textures/block/flesh.png")
				.title("Meat Whisperer")
				.description("You feel a presence in the flesh, it whispers into your ears: \"Raw Meat is useful...\"")
				.showToast().announceToChat()
				.addHasCriterion(ModItemTags.RAW_MEATS)
				.save(consumer, fileHelper);

		Advancement primalCore = createAdvancement("flesh").parent(root).icon(ModItems.PRIMORDIAL_CORE.get())
				.title("Primal Vision")
				.description("You feel an ancient presence from the ender pearl. As you gaze into the pearl... portals... you feel compelled to combine it with raw meat... cross the boundary...")
				.frameType(FrameType.CHALLENGE).showToast()
				.addHasCriterion(Items.ENDER_PEARL)
				.save(consumer, fileHelper);

		Advancement greedyButcher = createAdvancement("greedy_butcher").parent(primalCore).icon(ModItems.DESPOIL_SICKLE.get())
				.title("Greedy Butcher")
				.description("You've acquired a taste for organs. Craft the plundering Sickle to get them fresh from your victims death.")
				.showToast()
				.addHasCriterion(ModItems.DESPOIL_SICKLE.get())
				.save(consumer, fileHelper);

		createAdvancement("organ_trader").parent(greedyButcher).icon(ModItems.VOLATILE_GLAND.get())
				.title("Organ Trader")
				.description("Become the funnel for fleshy parts and trade various organs with villagers.")
				.frameType(FrameType.CHALLENGE).showToast().announceToChat().hidden()
				.addCriterion("has_traded_organs", hasTradedItems(ModItems.VOLATILE_GLAND.get(), ModItems.GENERIC_MOB_GLAND.get(), ModItems.TOXIN_GLAND.get()))
				.save(consumer, fileHelper);

		createAdvancement("poacher").parent(greedyButcher).icon(Items.LEATHER)
				.title("Rare Animal Poacher")
				.description("You have no morals and poached endangered Animals.")
				.frameType(FrameType.CHALLENGE).showToast().announceToChat().hidden()
				.addCriterion("has_killed_ocelot", hasKilledEntity(EntityType.OCELOT))
				.addCriterion("has_killed_panda", hasKilledEntity(EntityType.PANDA))
				.addCriterion("has_killed_polar_bear", hasKilledEntity(EntityType.POLAR_BEAR))
				.addCriterion("has_killed_axolotl", hasKilledEntity(EntityType.AXOLOTL))
				.save(consumer, fileHelper);

		createAdvancement("predator_killer").parent(greedyButcher).icon(ModItems.MOB_FANG.get())
				.title("Predator Killer")
				.description("Hunt predators and collect their fangs and claws.")
				.frameType(FrameType.CHALLENGE).showToast().announceToChat()
				.addCriterion("has_killed_fangs_mob", hasKilledEntityTag(ModEntityTags.SHARP_FANG))
				.addCriterion("has_killed_claws_mob", hasKilledEntityTag(ModEntityTags.SHARP_CLAW))
				.addHasCriterion(ModItems.MOB_FANG.get())
				.addHasCriterion(ModItems.MOB_CLAW.get())
				.save(consumer, fileHelper);

		createAdvancement("cat_killer").parent(greedyButcher).icon(Items.STRING)
				.title("Kitty Cat Killer")
				.description("Kill a innocent cat.")
				.frameType(FrameType.CHALLENGE).showToast().announceToChat().hidden()
				.addCriterion("has_killed_cat", hasKilledEntity(EntityType.CAT))
				.save(consumer, fileHelper);

		Advancement primalCradle = createAdvancement("cradle").parent(primalCore).icon(ModItems.PRIMORDIAL_CRADLE.get())
				.title("Cradle of Life")
				.description("The Primordial Core is whispering of a bowl filled with life... build the flesh construct and feed it with organic materials.")
				.showToast()
				.addCriterion("has_placed_cradle", hasPlacedBlock(ModBlocks.PRIMORDIAL_CRADLE.get()))
				.save(consumer, fileHelper);

		createAdvancement("healing_activator_sacrifice").parent(primalCradle).icon(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HEALING))
				.title("Healing Activator")
				.description("It's seems like fluids enriched in life energy are needed. Jumpstart the process with a few healing potions.")
				.showToast()
				.addCriterion("has_sacrificed_healing_activator", hasSacrificedItem(Items.POTION))
				.save(consumer, fileHelper);

		createAdvancement("raw_meat_sacrifice").parent(primalCradle).icon(Items.BEEF)
				.title("Tartar Delight")
				.description("Serve raw meat to the Primordial Cradle.")
				.showToast()
				.addCriterion("has_sacrificed_raw_meat", hasSacrificedTag(ModItemTags.RAW_MEATS))
				.save(consumer, fileHelper);

		createAdvancement("cooked_meat_sacrifice").parent(primalCradle).icon(Items.COOKED_BEEF)
				.title("Cooked Meat Disrelish")
				.description("Serve cooked meat to the Primordial Cradle.")
				.showToast().hidden()
				.addCriterion("has_sacrificed_cooked_meat", hasSacrificedTag(ModItemTags.COOKED_MEATS))
				.save(consumer, fileHelper);

		createAdvancement("decomposer").parent(primalCradle).icon(ModItems.DECOMPOSER.get())
				.title("Munch & Crunch")
				.description("You suddenly feel disgusted by the composter. You should use a semi-living construct to decompose things into their base parts.")
				.showToast()
				.addHasCriterion(ModItems.DECOMPOSER.get())
				.save(consumer, fileHelper);

		Advancement bioForge = createAdvancement("bio_forge").parent(primalCradle).icon(ModItems.BIO_FORGE.get())
				.title("Organic Smithing")
				.description("You dreamt of a Bio-Construct weaving organic parts together into intricate semi-living things... You don't know when, but you built it.")
				.frameType(FrameType.GOAL).showToast().announceToChat()
				.addHasCriterion(ModItems.BIO_FORGE.get())
				.save(consumer, fileHelper);

		createAdvancement("digester").parent(bioForge).icon(ModItems.DIGESTER.get())
				.title("Yummy Paste")
				.description("You feel tired of feeding your Bio-Constructs with poor quality food. You have the urge to produce a nutrients enriched yellow-green paste.")
				.frameType(FrameType.CHALLENGE).showToast()
				.addHasCriterion(ModItems.DIGESTER.get())
				.save(consumer, fileHelper);

		Advancement bioLab = createAdvancement("bio_lab").parent(bioForge).icon(ModItems.BIO_LAB.get())
				.title("Is this still Alchemy?")
				.description("You had an epiphany, why use crude inorganic tools to brew potions if an Bio-Construct can do it better. No longer do you need to mix or adjust the heat by yourself.")
				.frameType(FrameType.CHALLENGE).showToast()
				.addHasCriterion(ModItems.BIO_LAB.get())
				.save(consumer, fileHelper);

		createAdvancement("bio_injector").parent(bioLab).icon(ModItems.INJECTOR.get())
				.title("Injections")
				.description("Craft a Injector to be able to forcefully inject Serums into all living things.")
				.showToast()
				.addHasCriterion(ModItems.INJECTOR.get())
				.save(consumer, fileHelper);

		Advancement organicCompounds = createAdvancement("organic_compounds").parent(bioLab).icon(ModItems.ORGANIC_COMPOUND.get())
				.title("Organic Bio-Alchemy")
				.description("Combine various organic secretions and substances to create Compounds and Serums.")
				.showToast()
				.addHasCriterion(ModItems.ORGANIC_COMPOUND.get())
				.save(consumer, fileHelper);

		createAdvancement("exotic_compounds").parent(organicCompounds).icon(ModItems.EXOTIC_COMPOUND.get())
				.title("Exotic Bio-Alchemy")
				.description("Combine organic things with exotic compounds to create cures and cleansing fluids.")
				.showToast()
				.addHasCriterion(ModItems.EXOTIC_COMPOUND.get())
				.save(consumer, fileHelper);

		createAdvancement("genetic_compounds").parent(organicCompounds).icon(ModItems.GENETIC_COMPOUND.get())
				.title("Genetic Bio-Alchemy")
				.description("Combine organic things with genetic compounds to create fluids that influence growth and fertility.")
				.showToast()
				.addHasCriterion(ModItems.GENETIC_COMPOUND.get())
				.save(consumer, fileHelper);
	}

}
