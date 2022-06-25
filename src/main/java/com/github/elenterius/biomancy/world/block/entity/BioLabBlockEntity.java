package com.github.elenterius.biomancy.world.block.entity;

import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.recipe.BioLabRecipe;
import com.github.elenterius.biomancy.recipe.IngredientQuantity;
import com.github.elenterius.biomancy.recipe.RecipeTypeImpl;
import com.github.elenterius.biomancy.util.TextComponentUtil;
import com.github.elenterius.biomancy.world.block.entity.state.BioLabStateData;
import com.github.elenterius.biomancy.world.inventory.BehavioralInventory;
import com.github.elenterius.biomancy.world.inventory.SimpleInventory;
import com.github.elenterius.biomancy.world.inventory.itemhandler.HandlerBehaviors;
import com.github.elenterius.biomancy.world.inventory.menu.BioLabMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class BioLabBlockEntity extends MachineBlockEntity<BioLabRecipe, BioLabStateData> implements MenuProvider, IAnimatable {

	public static final int FUEL_SLOTS = 1;
	public static final int INPUT_SLOTS = BioLabRecipe.MAX_INGREDIENTS + BioLabRecipe.MAX_REACTANT;
	public static final int OUTPUT_SLOTS = 2;

	public static final int MAX_FUEL = 32_000;
	public static final short FUEL_COST = 2;
	public static final RecipeTypeImpl.ItemStackRecipeType<BioLabRecipe> RECIPE_TYPE = ModRecipes.BIO_BREWING_RECIPE_TYPE;

	private final BioLabStateData stateData = new BioLabStateData();
	private final BehavioralInventory<?> fuelInventory;
	private final SimpleInventory inputInventory;
	private final BehavioralInventory<?> outputInventory;

	private final AnimationFactory animationFactory = new AnimationFactory(this);

	public BioLabBlockEntity(BlockPos worldPosition, BlockState blockState) {
		super(ModBlockEntities.BIO_LAB.get(), worldPosition, blockState);
		fuelInventory = BehavioralInventory.createServerContents(FUEL_SLOTS, HandlerBehaviors::filterFuel, this::canPlayerOpenInv, this::setChanged);
		inputInventory = SimpleInventory.createServerContents(INPUT_SLOTS, this::canPlayerOpenInv, this::setChanged);
		outputInventory = BehavioralInventory.createServerContents(OUTPUT_SLOTS, HandlerBehaviors::denyInput, this::canPlayerOpenInv, this::setChanged);
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	public Component getName() {
		return TextComponentUtil.getTranslationText("container", "bio_lab");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return BioLabMenu.createServerMenu(containerId, playerInventory, fuelInventory, inputInventory, outputInventory, stateData);
	}

	@Override
	protected BioLabStateData getStateData() {
		return stateData;
	}

	@Override
	public int getFuelAmount() {
		return stateData.getFuelAmount();
	}

	@Override
	public void setFuelAmount(int newAmount) {
		stateData.setFuelAmount((short) newAmount);
	}

	@Override
	public void addFuelAmount(int addAmount) {
		stateData.setFuelAmount((short) (stateData.getFuelAmount() + addAmount));
	}

	@Override
	public int getMaxFuelAmount() {
		return MAX_FUEL;
	}

	@Override
	public int getFuelCost() {
		return FUEL_COST;
	}

	@Override
	public ItemStack getStackInFuelSlot() {
		return fuelInventory.getItem(0);
	}

	@Override
	public void setStackInFuelSlot(ItemStack stack) {
		fuelInventory.setItem(0, stack);
	}

	@Override
	protected boolean doesItemFitIntoOutputInventory(ItemStack stackToCraft) {
		return outputInventory.getItem(0).isEmpty() || outputInventory.doesItemStackFit(0, stackToCraft);
	}

	@Override
	protected boolean craftRecipe(BioLabRecipe recipeToCraft, Level level) {
		ItemStack result = recipeToCraft.getResultItem().copy();
		if (result.isEmpty() || !doesItemFitIntoOutputInventory(result)) {
			return false;
		}

		//get ingredients cost
		List<IngredientQuantity> ingredients = recipeToCraft.getIngredientQuantities();
		int[] ingredientCost = new int[ingredients.size()];
		for (int i = 0; i < ingredients.size(); i++) {
			ingredientCost[i] = ingredients.get(i).count();
		}

		//check if we can output all container items
		if (!canContainerItemsFitIntoTrashSlot(ingredients, Arrays.copyOf(ingredientCost, ingredientCost.length))) return false;

		//consume reactant
		final int lastIndex = inputInventory.getContainerSize() - 1;
		inputInventory.removeItem(lastIndex, 1);

		//consume ingredients
		for (int idx = 0; idx < lastIndex; idx++) {
			final ItemStack foundStack = inputInventory.getItem(idx); //do not modify this stack
			if (!foundStack.isEmpty()) {
				for (int i = 0; i < ingredients.size(); i++) {
					int remainingCost = ingredientCost[i];
					if (remainingCost > 0 && ingredients.get(i).testItem(foundStack)) {
						int amount = Math.min(remainingCost, foundStack.getCount());
						inputInventory.removeItem(idx, amount);
						outputContainerItems(foundStack, amount);
						ingredientCost[i] -= amount;
						break;
					}
				}
			}
		}

		//output result
		outputInventory.insertItemStack(0, result);

		setChanged();
		return true;
	}

	private boolean canContainerItemsFitIntoTrashSlot(List<IngredientQuantity> ingredients, int[] ingredientCost) {
		int lastIndex = inputInventory.getContainerSize() - 1;
		int trashAmount = outputInventory.getItem(1).getCount();

		if (trashAmount >= outputInventory.getMaxStackSize()) return false;
		if (trashAmount < 1) return true;

		for (int idx = 0; idx < lastIndex; idx++) {
			final ItemStack foundStack = inputInventory.getItem(idx);
			if (!foundStack.isEmpty() && foundStack.hasContainerItem()) {
				ItemStack containerItem = foundStack.getContainerItem();
				if (!containerItem.isEmpty()) {
					for (int i = 0; i < ingredients.size(); i++) {
						int remainingCost = ingredientCost[i];
						if (remainingCost > 0 && ingredients.get(i).testItem(foundStack)) {
							int amount = Math.min(remainingCost, foundStack.getCount());
							ingredientCost[i] -= amount;
							containerItem.setCount(amount);
							if (!outputInventory.doesItemStackFit(1, containerItem)) return false;
							break;
						}
					}
				}
			}
		}

		return true;
	}

	private void outputContainerItems(ItemStack foundStack, int amount) {
		if (foundStack.hasContainerItem()) {
			ItemStack containerItem = foundStack.getContainerItem();
			if (!containerItem.isEmpty()) {
				containerItem.setCount(amount);
				outputInventory.insertItemStack(1, containerItem);
			}
		}
	}

	@Nullable
	@Override
	protected BioLabRecipe resolveRecipeFromInput(Level level) {
		return RECIPE_TYPE.getRecipeFromContainer(level, inputInventory).orElse(null);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		stateData.serialize(tag);
		tag.put("FuelSlots", fuelInventory.serializeNBT());
		tag.put("InputSlots", inputInventory.serializeNBT());
		tag.put("OutputSlots", outputInventory.serializeNBT());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		stateData.deserialize(tag);
		fuelInventory.deserializeNBT(tag.getCompound("FuelSlots"));
		inputInventory.deserializeNBT(tag.getCompound("InputSlots"));
		outputInventory.deserializeNBT(tag.getCompound("OutputSlots"));
	}

	@Override
	public void dropAllInvContents(Level level, BlockPos pos) {
		Containers.dropContents(level, pos, fuelInventory);
		Containers.dropContents(level, pos, inputInventory);
		Containers.dropContents(level, pos, outputInventory);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == null || side == Direction.DOWN) return outputInventory.getOptionalItemHandler().cast();
			if (side == Direction.UP) return inputInventory.getOptionalItemHandler().cast();
			return fuelInventory.getOptionalItemHandler().cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		fuelInventory.invalidate();
		inputInventory.invalidate();
		outputInventory.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		fuelInventory.revive();
		inputInventory.revive();
		outputInventory.revive();
	}

	private <E extends BlockEntity & IAnimatable> PlayState handleAnim(AnimationEvent<E> event) {
		event.getController().setAnimation(new AnimationBuilder().addAnimation("bio_lab.idle"));
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 0, this::handleAnim));
	}

	@Override
	public AnimationFactory getFactory() {
		return animationFactory;
	}

}
