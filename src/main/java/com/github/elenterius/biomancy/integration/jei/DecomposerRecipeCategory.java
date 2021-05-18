package com.github.elenterius.biomancy.integration.jei;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.recipe.Byproduct;
import com.github.elenterius.biomancy.recipe.DecomposerRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class DecomposerRecipeCategory implements IRecipeCategory<DecomposerRecipe> {

	public static final ResourceLocation ID = BiomancyMod.createRL("jei_" + ModRecipes.DECOMPOSING_RECIPE_TYPE);
	private static final int OUTPUT_SLOT = 0;
	private static final int INPUT_SLOT = 4;
	private final IDrawable background;
	private final IDrawable icon;

	public DecomposerRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.DECOMPOSER.get()));
		background = guiHelper.drawableBuilder(BiomancyMod.createRL("textures/gui/jei/gui_decomposer.png"), 0, 0, 144, 60).setTextureSize(144, 60).build();
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public Class<? extends DecomposerRecipe> getRecipeClass() {
		return DecomposerRecipe.class;
	}

	@Override
	public String getTitle() {
		return I18n.format("jei.biomancy.recipe.decomposer");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(DecomposerRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		List<ItemStack> outputs = new ArrayList<>();
		outputs.add(recipe.getRecipeOutput());
		for (Byproduct byproduct : recipe.getByproducts()) {
			outputs.add(byproduct.getItemStack());
		}
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	@Override
	public void setRecipe(IRecipeLayout layout, DecomposerRecipe recipe, IIngredients ingredients) {
		layout.setShapeless();

		IGuiItemStackGroup guiISGroup = layout.getItemStacks();
		guiISGroup.init(OUTPUT_SLOT, false, 94, 18);
		guiISGroup.init(OUTPUT_SLOT + 1, false, 90, 42);
		guiISGroup.init(OUTPUT_SLOT + 2, false, 90 + 18, 42);
		guiISGroup.init(OUTPUT_SLOT + 3, false, 90 + 18 * 2, 42);

		int idx = INPUT_SLOT;
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 3; x++) {
				guiISGroup.init(idx++, true, x * 18, 9 + y * 18);
			}
		}

		guiISGroup.set(ingredients);

		guiISGroup.addTooltipCallback((index, input, stack, tooltip) -> {
			if (index > OUTPUT_SLOT && index < INPUT_SLOT) {
				if (!stack.isEmpty()) {
					int chance = 100;
					for (Byproduct byproduct : recipe.getByproducts()) {
						if (byproduct.getItem() == stack.getItem()) {
							chance = Math.round(byproduct.getChance() * 100);
							break;
						}
					}
//					tooltip.add(new TranslationTextComponent(BiomancyMod.getTranslationKey("tooltip", "byproduct")));
					tooltip.add(new StringTextComponent(chance + "% ").appendSibling(new TranslationTextComponent(BiomancyMod.getTranslationKey("tooltip", "chance"))).mergeStyle(TextFormatting.GRAY));
				}
			}
		});
	}

	@Override
	public void draw(DecomposerRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		int ticks = recipe.getDecomposingTime();
		if (ticks > 0) {
			int seconds = ticks / 20;
			TranslationTextComponent timeString = new TranslationTextComponent("gui.jei.category.smelting.time.seconds", seconds);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.getStringPropertyWidth(timeString);
			fontRenderer.drawText(matrixStack, timeString, (float) (background.getWidth() - stringWidth), 42 - fontRenderer.FONT_HEIGHT, 0xff808080);
		}
	}
}
