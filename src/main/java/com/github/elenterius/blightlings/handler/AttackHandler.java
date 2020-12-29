package com.github.elenterius.blightlings.handler;

import com.github.elenterius.blightlings.BlightlingsMod;
import com.github.elenterius.blightlings.item.ClawWeaponItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlightlingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AttackHandler {
//    private static final UUID HEALTH_REDUCTION_ID = UUID.fromString("9eee3c0b-43cf-41e5-af9d-8cfd79381515");
//    public static final AttributeModifier negativeHealthModifier = new AttributeModifier(HEALTH_REDUCTION_ID, "health reduction", -2.0F, AttributeModifier.Operation.ADDITION);

	private AttackHandler() {}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingAttack(final LivingAttackEvent event) {
		if (!event.getEntityLiving().isServerWorld()) return;

		DamageSource damageSource = event.getSource();
		if (!event.isCanceled() && damageSource instanceof ModEntityDamageSource) {
			((ModEntityDamageSource) damageSource).updateAttackStrength(event.getAmount()); // calculate attacker strength ("attack cool down")
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingDamageAfterDamageReduction(final LivingDamageEvent event) {
		if (!event.getEntityLiving().isServerWorld()) return;

		//if this is called the victims armor didn't block all damage

		DamageSource damageSource = event.getSource();
		if (damageSource instanceof ModEntityDamageSource && ((ModEntityDamageSource) damageSource).statusProc.equals("blight_thorn")) {
			if (((ModEntityDamageSource) damageSource).getAttackStrength() < 0.9f) return; // only trigger if "attack cool down" was nearly full

			ModifiableAttributeInstance healthAttribute = event.getEntityLiving().getAttribute(Attributes.MAX_HEALTH);
			if (healthAttribute != null) {
				Entity attacker = damageSource.getTrueSource();
				if (attacker instanceof LivingEntity) {
					LivingEntity victim = event.getEntityLiving();
					if (((LivingEntity) attacker).getHealth() < victim.getMaxHealth() * 0.75f && victim.getRNG().nextFloat() < 0.6f) {
						healthAttribute.applyNonPersistentModifier(new AttributeModifier("health reduction", -0.3F, AttributeModifier.Operation.MULTIPLY_BASE));
//                        victim.playSound(ModSoundEvents.IMPACT_SPLAT, 0.25f, 0.8f);
						victim.playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 0.4f, 0.45f);
					}
				}
			}
		}


	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDamageAfterDamageReductionLast(final LivingDamageEvent event) {
		if (event.getAmount() > 0f && event.getSource().getImmediateSource() instanceof LivingEntity) {
			ItemStack heldStack = ((LivingEntity) event.getSource().getImmediateSource()).getHeldItemMainhand();
			if (heldStack.getItem() instanceof ClawWeaponItem) {
				((ClawWeaponItem) heldStack.getItem()).onDamageEntity(heldStack, (LivingEntity) event.getSource().getImmediateSource(), event.getEntityLiving(), event.getAmount());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCriticalHit(final CriticalHitEvent event) {
		if (event.getDamageModifier() > 0 && event.getTarget() instanceof LivingEntity && (event.getResult() == Event.Result.ALLOW || event.isVanillaCritical() && event.getResult() == Event.Result.DEFAULT)) {
			ItemStack heldStack = event.getEntityLiving().getHeldItemMainhand();
			if (heldStack.getItem() instanceof ClawWeaponItem) {
				((ClawWeaponItem) heldStack.getItem()).onCriticalHitEntity(heldStack, event.getPlayer(), (LivingEntity) event.getTarget());
			}
		}
	}

	public static void onAttackEntity(final AttackEntityEvent event) {

	}
}
