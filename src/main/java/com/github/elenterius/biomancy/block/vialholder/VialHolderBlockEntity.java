package com.github.elenterius.biomancy.block.vialholder;

import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.block.entity.SimpleSyncedBlockEntity;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class VialHolderBlockEntity extends SimpleSyncedBlockEntity {
	public static final String INVENTORY_TAG = "Inventory";
	private final ItemStackHandler inventory;

	public VialHolderBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.VIAL_HOLDER.get(), pos, blockState);
		inventory = new ItemStackHandler(5) {
			@Override
			public int getSlotLimit(int slot) {
				return 1;
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return stack.getItem() instanceof SerumContainer;
			}

			@Override
			protected void onContentsChanged(int slot) {
				setChanged();
				syncToClient();
			}
		};
	}

	protected void syncToClient() {
		if (level != null && !level.isClientSide) {

			BlockState newState = getBlockState();
			for (int i = 0; i < VialHolderBlock.VIAL_PROPERTIES.length; i++) {
				BooleanProperty vialProperty = VialHolderBlock.VIAL_PROPERTIES[i];
				newState = newState.setValue(vialProperty, inventory.getStackInSlot(i).getItem() instanceof SerumContainer);
			}

			level.setBlockAndUpdate(getBlockPos(), newState);

			BlockState state = getBlockState();
			level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put(INVENTORY_TAG, inventory.serializeNBT());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));

		if (level != null && !level.isClientSide) {
			BlockState state = getBlockState();
			for (int i = 0; i < VialHolderBlock.VIAL_PROPERTIES.length; i++) {
				BooleanProperty vialProperty = VialHolderBlock.VIAL_PROPERTIES[i];
				state = state.setValue(vialProperty, inventory.getStackInSlot(i).getItem() instanceof SerumContainer);
			}
			if (state != getBlockState()) {
				level.setBlockAndUpdate(getBlockPos(), state);
			}
		}
	}

	@Override
	protected void saveForSyncToClient(CompoundTag tag) {
		tag.put(INVENTORY_TAG, inventory.serializeNBT());
	}

	public void dropInventoryContents(Level level, BlockPos pos, boolean removeWithoutUpdate) {
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.extractItem(i, inventory.getSlotLimit(i), removeWithoutUpdate);
			if (stack.isEmpty()) continue;
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
		}
	}

	public void extractVial(Player player, int slot) {
		if (slot < 0 || slot >= inventory.getSlots()) return;

		ItemStack stack = inventory.extractItem(slot, inventory.getSlotLimit(slot), false);
		if (!stack.isEmpty() && !player.addItem(stack)) {
			player.drop(stack, false);
		}
	}

	public ItemStack insertVial(ItemStack stack, int slot) {
		if (slot < 0 || slot >= inventory.getSlots()) return stack;
		return inventory.insertItem(slot, stack, false);
	}

	public boolean hasVial(int slot) {
		if (slot < 0 || slot >= inventory.getSlots()) return false;
		return inventory.getStackInSlot(slot).getItem() instanceof SerumContainer;
	}

	public boolean isValidSlotIndex(int slot) {
		return slot >= 0 && slot < inventory.getSlots();
	}

	public int getVialColor(int slot) {
		if (slot < 0 || slot >= inventory.getSlots()) return 0xFFFFFFFF;
		if (inventory.getStackInSlot(slot).getItem() instanceof SerumContainer container) {
			return container.getSerum().getColor();
		}
		return 0xFFFFFFFF;
	}
}
