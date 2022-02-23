package com.github.elenterius.biomancy.world.entity.ai.goal.controllable;

import com.github.elenterius.biomancy.world.entity.ownable.IControllableMob;
import com.github.elenterius.biomancy.world.entity.ownable.IOwnableMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PatrolAreaGoal<T extends PathfinderMob & IOwnableMob & IControllableMob> extends RandomStrollGoal {

	private final T entity;

	public PatrolAreaGoal(T goalOwner, double speedIn) {
		super(goalOwner, speedIn, 240, false);
		entity = goalOwner;
	}

	@Override
	public boolean canUse() {
		if (entity.canExecuteCommand()) {
			IControllableMob.Command command = entity.getActiveCommand();
			if (command == IControllableMob.Command.PATROL_AREA || command == IControllableMob.Command.DEFEND_OWNER) {
				return super.canUse();
			}
		}
		return false;
	}

	@Override
	@Nullable
	protected Vec3 getPosition() {
		if (entity.getActiveCommand() == IControllableMob.Command.DEFEND_OWNER) {
			Optional<Player> owner = entity.getOwner();
			return owner.isPresent() ? findPosTowards(owner.get().position()) : findPosNearby();
		}
		if (mob.level.random.nextFloat() < 0.3F) {
			return findPosNearby();
		}
		else {
			Vec3 pos = findPosTowardsRandomPlayer();
			return pos != null ? pos : findPosNearby();
		}
	}

	@Nullable
	private Vec3 findPosTowards(Vec3 pos) {
		return LandRandomPos.getPosTowards(mob, 10, 7, pos); // includes water
	}

	@Nullable
	private Vec3 findPosNearby() {
		return LandRandomPos.getPos(mob, 10, 7);
	}

	@Nullable
	private Vec3 findPosTowardsRandomPlayer() {
		List<Player> list = mob.level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(32d));
		if (!list.isEmpty()) {
			Player player = list.get(mob.level.random.nextInt(list.size()));
			return LandRandomPos.getPosTowards(mob, 10, 7, player.position());
		}
		return null;
	}

}
