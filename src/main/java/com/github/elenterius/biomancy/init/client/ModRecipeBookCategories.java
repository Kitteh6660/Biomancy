package com.github.elenterius.biomancy.init.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModRecipeBookTypes;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.recipe.BioForgeRecipe;
import com.github.elenterius.biomancy.world.inventory.menu.BioForgeTab;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = BiomancyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModRecipeBookCategories {

	private static final Map<String, RecipeBookCategories> BIO_FORGE_TAB_TO_CATEGORY = new HashMap<>();

	public static final RecipeBookCategories SEARCH_CATEGORY = createRecipeBookCategories(ModBioForgeTabs.SEARCH);
	public static final RecipeBookCategories MISC_CATEGORY = createRecipeBookCategories(ModBioForgeTabs.MISC);
	public static final RecipeBookCategories BLOCKS_CATEGORY = createRecipeBookCategories(ModBioForgeTabs.BLOCKS);
	public static final RecipeBookCategories MACHINES_CATEGORY = createRecipeBookCategories(ModBioForgeTabs.MACHINES);
	public static final RecipeBookCategories WEAPONS_CATEGORY = createRecipeBookCategories(ModBioForgeTabs.WEAPONS);
	public static final List<RecipeBookCategories> BIO_FORGE_CATEGORIES = List.of(SEARCH_CATEGORY, MISC_CATEGORY, BLOCKS_CATEGORY, MACHINES_CATEGORY, WEAPONS_CATEGORY);
	
	public static final Function<Recipe<?>, RecipeBookCategories> BIO_FORGE_BOOK_CATEGORIES_FINDER = recipe -> {
		if (recipe instanceof BioForgeRecipe bioForgeRecipe) {
			return BIO_FORGE_TAB_TO_CATEGORY.get(bioForgeRecipe.getTab().enumId());
		}
		return null;
	};

	private ModRecipeBookCategories() {}

	private static RecipeBookCategories createRecipeBookCategories(RegistryObject<BioForgeTab> tab) {
		String name = tab.getId().toString().replace(":", "_");
		RecipeBookCategories categories = RecipeBookCategories.create(name, tab.get().getIcon());
		BIO_FORGE_TAB_TO_CATEGORY.put(name, categories);
		return categories;
	}

	public static RecipeBookCategories getRecipeBookCategories(BioForgeTab category) {
		return BIO_FORGE_TAB_TO_CATEGORY.get(category.enumId());
	}

	@SubscribeEvent
	public static void registerRecipeBooks(RegisterRecipeBookCategoriesEvent event) {
		event.registerBookCategories(ModRecipeBookTypes.BIO_FORGE, ModRecipeBookCategories.BIO_FORGE_CATEGORIES);
		event.registerAggregateCategory(ModRecipeBookCategories.SEARCH_CATEGORY, ModRecipeBookCategories.BIO_FORGE_CATEGORIES);
		event.registerRecipeCategoryFinder(ModRecipes.BIO_FORGING_RECIPE_TYPE.get(), ModRecipeBookCategories.BIO_FORGE_BOOK_CATEGORIES_FINDER);

		event.registerRecipeCategoryFinder(ModRecipes.BIO_BREWING_RECIPE_TYPE.get(), rc -> RecipeBookCategories.UNKNOWN);
		event.registerRecipeCategoryFinder(ModRecipes.DECOMPOSING_RECIPE_TYPE.get(), rc -> RecipeBookCategories.UNKNOWN);
		event.registerRecipeCategoryFinder(ModRecipes.DIGESTING_RECIPE_TYPE.get(), rc -> RecipeBookCategories.UNKNOWN);
	}

}
