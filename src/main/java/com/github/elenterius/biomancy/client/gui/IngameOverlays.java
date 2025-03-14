package com.github.elenterius.biomancy.client.gui;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.client.util.GuiRenderUtil;
import com.github.elenterius.biomancy.client.util.GuiUtil;
import com.github.elenterius.biomancy.entity.ownable.IControllableMob;
import com.github.elenterius.biomancy.item.AttackReachIndicator;
import com.github.elenterius.biomancy.item.ItemCharge;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.github.elenterius.biomancy.item.weapon.Gun;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class IngameOverlays {

	//	public static final ResourceLocation COMMAND_ICONS = BiomancyMod.createRL("textures/gui/command_icons.png");
	public static final ResourceLocation INJECTOR_COOL_DOWN = BiomancyMod.createRL("textures/gui/indicator_injector_cooldown.png");
	public static final ResourceLocation ATTACK_REACH = BiomancyMod.createRL("textures/gui/indicator_attack_reach.png");
	public static final ResourceLocation ORNATE_CORNER_BOTTOM_RIGHT = BiomancyMod.createRL("textures/gui/ornate_corner_br.png");
	public static final ResourceLocation CHARGE_BAR = BiomancyMod.createRL("textures/gui/charge_bar.png");

	//	public static final IIngameOverlay CONTROL_STAFF_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
	//		Minecraft minecraft = Minecraft.getInstance();
	//		if (!minecraft.options.hideGui && minecraft.options.getCameraType().isFirstPerson() && minecraft.player != null) {
	//			ItemStack itemStack = minecraft.player.getMainHandItem();
	//			if (itemStack.isEmpty() || !itemStack.is(ModItems.CONTROL_STAFF.get())) return;
	//			IControllableMob.Command command = ModItems.CONTROL_STAFF.get().getCommand(itemStack);
	//
	//			gui.setupOverlayRenderState(true, false, COMMAND_ICONS);
	//			gui.setBlitOffset(-90);
	//			renderCommandOverlay(poseStack, screenWidth, screenHeight, command);
	//		}
	//	};

	//	public static final IGuiOverlay GUN_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
	//		Minecraft minecraft = Minecraft.getInstance();
	//		if (!minecraft.options.hideGui && minecraft.player != null) {
	//			ItemStack itemStack = minecraft.player.getMainHandItem();
	//			if (itemStack.isEmpty() || !(itemStack.getItem() instanceof IGun gun)) return;
	//
	//			gui.setupOverlayRenderState(true, false);
	//			gui.setBlitOffset(-90);
	//			renderGunOverlay(gui, poseStack, screenWidth, screenHeight, minecraft.player, itemStack, gun);
	//		}
	//	};

	public static final IGuiOverlay INJECTOR_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack itemStack = minecraft.player.getMainHandItem();
			if (itemStack.isEmpty() || !(itemStack.getItem() instanceof InjectorItem injector)) return;

			gui.setupOverlayRenderState(true, false);
			gui.setBlitOffset(-90);
			renderInjectorOverlay(gui, poseStack, partialTicks, screenWidth, screenHeight, minecraft.player, itemStack, injector);
		}
	};

	public static final IGuiOverlay CHARGE_BAR_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (stack.isEmpty() || !(stack.getItem() instanceof ItemCharge abilityCharge)) return;

			if (GuiUtil.isFirstPersonView()) {
				gui.setupOverlayRenderState(true, false);
				gui.setBlitOffset(-90);
				renderChargeBar(poseStack, screenWidth, screenHeight, gui.getFont(), abilityCharge.getCharge(stack), abilityCharge.getChargePct(stack));
			}
		}
	};

	public static final IGuiOverlay ATTACK_REACH_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.options.hideGui && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (stack.isEmpty() || !(stack.getItem() instanceof AttackReachIndicator)) return;

			if (GuiUtil.isFirstPersonView()) {
				gui.setupOverlayRenderState(true, false);
				gui.setBlitOffset(-90);

				if (minecraft.crosshairPickEntity instanceof LivingEntity crosshairTarget && crosshairTarget.isAlive() && minecraft.player.canHit(crosshairTarget, 0)) {
					int x = screenWidth / 2 - 8;
					int y = screenHeight / 2 - 16 - 8;
					RenderSystem.setShaderTexture(0, ATTACK_REACH);
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GuiComponent.blit(poseStack, x, y, gui.getBlitOffset(), 0, 0, 16, 16, 16, 16);
				}
			}
		}
	};

	private IngameOverlays() {}

	static void renderCommandOverlay(PoseStack poseStack, int screenWidth, int screenHeight, IControllableMob.Command command) {
		//		if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
		int x = screenWidth / 2 + 16;
		int y = screenHeight / 2 - 16;
		GuiComponent.blit(poseStack, x, y, command.serialize() * 32f, 0, 32, 32, 160, 32);
		GuiComponent.drawString(poseStack, Minecraft.getInstance().font, command.name(), x, y + 16 + 18, 0x55ffff);
	}

	static void renderGunOverlay(ForgeGui gui, PoseStack poseStack, int screenWidth, int screenHeight, LocalPlayer player, ItemStack stack, Gun gun) {
		renderAmmoOverlay(poseStack, screenWidth, screenHeight, stack, gun);

		if (GuiUtil.isFirstPersonView()) {
			renderReloadIndicator(gui, poseStack, screenWidth, screenHeight, player, stack, gun);
		}
	}

	static void renderInjectorOverlay(ForgeGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight, LocalPlayer player, ItemStack stack, InjectorItem injector) {
		if (GuiUtil.isFirstPersonView()) {
			float progress = 1f - player.getCooldowns().getCooldownPercent(injector, partialTicks);
			if (progress < 1f) {
				int x = screenWidth / 2 - 8;
				int y = screenHeight / 2 - 7 - 8;
				RenderSystem.setShaderTexture(0, INJECTOR_COOL_DOWN);
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GuiComponent.blit(poseStack, x, y, gui.getBlitOffset(), 0, 0, 16, 7, 16, 16);
				GuiComponent.blit(poseStack, x, y, gui.getBlitOffset(), 0, 7, (int) (progress * 16f), 7, 16, 16);
			}

			ItemStack serumItemStack = injector.getSerumItemStack(stack);
			if (serumItemStack.getItem() instanceof SerumContainer container && !container.getSerum().isEmpty()) {
				Font font = gui.getFont();
				short maxAmmo = InjectorItem.MAX_SLOT_SIZE;
				renderAmmoCount(poseStack, font, screenWidth, screenHeight, maxAmmo, serumItemStack.getCount(), 0xFFFEFEFE, 0xFF9E9E9E);
			}
		}
	}

	static void renderOrnateCorner(PoseStack poseStack, int x, int y) {
		RenderSystem.setShaderTexture(0, ORNATE_CORNER_BOTTOM_RIGHT);
		GuiComponent.blit(poseStack, x, y, 0, 0, 44, 28, 44, 28);
	}

	static void renderChargeBar(PoseStack poseStack, int screenWidth, int screenHeight, Font font, int charge, float chargePct) {
		int x = screenWidth - 51 - 16;
		int y = screenHeight - 5 - 16;

		RenderSystem.setShaderTexture(0, CHARGE_BAR);
		GuiComponent.blit(poseStack, x, y, 6, 6, 51, 5, 64, 16); //background
		GuiComponent.blit(poseStack, x, y, 6, 11, (int) (chargePct * 51), 5, 64, 16); //foreground
		GuiComponent.blit(poseStack, x, y - 5, 6, 0, 51, 6, 64, 16); //ornament

		if (charge <= 0) return;

		String number = String.valueOf(charge);
		int pX = x + 26 - font.width(number) / 2;
		int pY = y - 5 - 4;

		font.draw(poseStack, number, pX + 1f, pY, 0);
		font.draw(poseStack, number, pX - 1f, pY, 0);
		font.draw(poseStack, number, pX, pY + 1f, 0);
		font.draw(poseStack, number, pX, pY - 1f, 0);
		font.draw(poseStack, number, pX, pY, 0xac0404);
	}

	static void renderReloadIndicator(ForgeGui gui, PoseStack poseStack, int screenWidth, int screenHeight, LocalPlayer player, ItemStack stack, Gun gun) {
		Gun.State gunState = gun.getState(stack);
		if (gunState == Gun.State.RELOADING) {
			long elapsedTime = player.clientLevel.getGameTime() - gun.getReloadStartTime(stack);
			float reloadProgress = gun.getReloadProgress(elapsedTime, gun.getReloadTime(stack));
			GuiRenderUtil.drawSquareProgressBar(poseStack, screenWidth / 2, screenHeight / 2, gui.getBlitOffset(), 10, reloadProgress);
		}
		else {
			long elapsedTime = player.clientLevel.getGameTime() - gun.getShootTimestamp(stack);
			renderAttackIndicator(gui, poseStack, screenWidth, screenHeight, player, elapsedTime, gun.getShootDelay(stack));
		}
	}

	static void renderAmmoOverlay(PoseStack poseStack, int screenWidth, int screenHeight, ItemStack stack, Gun gun) {
		int maxAmmo = gun.getMaxAmmo(stack);
		int ammo = gun.getAmmo(stack);
		renderOrnateCorner(poseStack, screenWidth - 44, screenHeight - 28);
		Minecraft.getInstance().getItemRenderer().renderGuiItem(gun.getAmmoIcon(stack), screenWidth - 16 - 4, screenHeight - 28 - 8);
		renderAmmoCount(poseStack, Minecraft.getInstance().font, screenWidth, screenHeight, maxAmmo, ammo, 0xFFFEFEFE, 0xFF9E9E9E);
	}

	static void renderAmmoCount(PoseStack poseStack, Font font, int screenWidth, int screenHeight, int maxAmmoIn, int ammoIn, int primaryColor, int secondaryColor) {
		String maxAmmo = "/" + maxAmmoIn;
		String ammo = String.valueOf(ammoIn);
		int x = screenWidth - font.width(maxAmmo) - 4;
		int y = screenHeight - font.lineHeight - 4;
		GuiComponent.drawString(poseStack, font, maxAmmo, x, y, secondaryColor);
		poseStack.pushPose();
		float scale = 1.5f; //make font bigger
		poseStack.translate(x - font.width(ammo) * scale, y - font.lineHeight * scale * 0.5f, 0);
		poseStack.scale(scale, scale, 0);
		GuiComponent.drawString(poseStack, font, ammo, 0, 0, primaryColor);
		poseStack.popPose();
	}

	public static void renderAttackIndicator(ForgeGui gui, PoseStack poseStack, int screenWidth, int screenHeight, LocalPlayer player, long elapsedTime, int shootDelay) {
		if (elapsedTime < shootDelay && GuiUtil.canDrawAttackIndicator(player)) {
			float progress = (float) elapsedTime / shootDelay;
			if (progress < 1f) {
				int x = screenWidth / 2 - 8;
				int y = screenHeight / 2 - 7 + 16;
				GuiRenderUtil.drawAttackIndicator(gui, poseStack, x, y, progress);
			}
		}
	}

}
