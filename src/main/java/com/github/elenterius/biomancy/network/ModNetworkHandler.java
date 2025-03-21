package com.github.elenterius.biomancy.network;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.recipe.BioForgeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ModNetworkHandler {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel SIMPLE_NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(BiomancyMod.createRL("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	private ModNetworkHandler() {}

	public static void sendKeyBindPressToServer(InteractionHand hand, byte flag) {
		EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		sendKeyBindPressToServer(slot, flag);
	}

	public static void sendKeyBindPressToServer(EquipmentSlot slot, byte flag) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new KeyPressMessage(slot.getFilterFlag(), flag));
	}

	public static void sendKeyBindPressToServer(int slotIndex, byte flag) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new KeyPressMessage(slotIndex, flag));
	}

	public static void sendBioForgeRecipeToServer(int containerId, BioForgeRecipe recipe) {
		SIMPLE_NETWORK_CHANNEL.sendToServer(new BioForgeRecipeMessage(containerId, recipe.getId()));
	}

	public static <T extends BlockEntity & ISyncableAnimation> void sendAnimationToClients(T blockEntity, int id, int data) {
		Level level = blockEntity.getLevel();
		if (level != null && !level.isClientSide()) {
			BlockPos pos = blockEntity.getBlockPos();
			Supplier<PacketDistributor.TargetPoint> targetPoint = PacketDistributor.TargetPoint.p(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 48, level.dimension());
			SIMPLE_NETWORK_CHANNEL.send(PacketDistributor.NEAR.with(targetPoint), new BlockEntityAnimationClientMessage(pos, id, data)); //TODO: use tracked chunk instead?
		}
	}

	public static void register() {
		int id = -1;
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, KeyPressMessage.class, KeyPressMessage::encode, KeyPressMessage::decode, KeyPressMessage::handle);
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, BioForgeRecipeMessage.class, BioForgeRecipeMessage::encode, BioForgeRecipeMessage::decode, BioForgeRecipeMessage::handle);
		SIMPLE_NETWORK_CHANNEL.registerMessage(++id, BlockEntityAnimationClientMessage.class, BlockEntityAnimationClientMessage::encode, BlockEntityAnimationClientMessage::decode, BlockEntityAnimationClientMessage::handle);
	}

}
