package com.mikoalopex.createpowertrain.content.blocks.fire_hose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mikoalopex.createpowertrain.Config;
import com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances;
import com.mikoalopex.createpowertrain.integration.sable.SableStructureCompat;
import com.simibubi.create.content.fluids.FluidPropagator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class FireHoseConnections {

	public enum Result {
		SUCCESS,
		MISSING_ENDPOINT,
		SAME_ENDPOINT,
		OUT_OF_RANGE
	}

	public record ConnectionAttempt(Result result, @Nullable FireHoseBlockEntity endpoint) {
		public boolean successful() {
			return result == Result.SUCCESS && endpoint != null;
		}
	}

	private FireHoseConnections() {
	}

	public static Result tryConnect(Level level, BlockPos firstPos, BlockPos secondPos) {
		if (level == null)
			return Result.MISSING_ENDPOINT;
		if (firstPos.equals(secondPos))
			return Result.SAME_ENDPOINT;

		FireHoseBlockEntity first = getEndpoint(level, firstPos);
		FireHoseBlockEntity second = getEndpoint(level, secondPos);
		if (first == null || second == null)
			return Result.MISSING_ENDPOINT;
		return tryConnect(first, second);
	}

	public static Result tryConnect(FireHoseBlockEntity first, FireHoseBlockEntity second) {
		if (first == null || second == null)
			return Result.MISSING_ENDPOINT;
		if (!first.allowsHoseConnection() || !second.allowsHoseConnection()) return Result.MISSING_ENDPOINT;
		if (first == second || first.getFireHoseEndpointId().equals(second.getFireHoseEndpointId()))
			return Result.SAME_ENDPOINT;
		if (!isWithinRange(first, second))
			return Result.OUT_OF_RANGE;

		Level firstLevel = first.getLevel();
		Level secondLevel = second.getLevel();
		if (firstLevel == null || secondLevel == null)
			return Result.MISSING_ENDPOINT;

		FireHoseBlockEntity controller = first.isController() || !second.isController() ? first : second;
		FireHoseBlockEntity partner = controller == first ? second : first;
		ResourceLocation appearance = FireHoseAppearances.selectForConnection(
			first.getHoseAppearanceId(), second.getHoseAppearanceId());

		disconnect(first);
		disconnect(second);

		controller.setFireHoseConnection(true, partner.getBlockPos(),
			SableStructureCompat.containingSubLevelId(partner.getLevel(), partner.getBlockPos()),
			partner.getFireHoseEndpointId(), false, appearance);
		partner.setFireHoseConnection(false, controller.getBlockPos(),
			SableStructureCompat.containingSubLevelId(controller.getLevel(), controller.getBlockPos()),
			controller.getFireHoseEndpointId(), false, appearance);
		markConnectionChanged(controller);
		markConnectionChanged(partner);
		return Result.SUCCESS;
	}

	public static void disconnect(FireHoseBlockEntity hose) {
		if (hose == null)
			return;

		Level level = hose.getLevel();
		BlockPos partnerPos = hose.getFireHosePartnerPos();
		hose.clearFireHoseConnection();
		markConnectionChanged(hose);

		if (level == null)
			return;
		if (partnerPos == null)
			return;
		FireHoseBlockEntity partner = getEndpoint(level, partnerPos);
		if (partner == null) {
			return;
		}
		boolean modernBackReference = hose.getFireHoseEndpointId().equals(partner.getFireHosePartnerEndpointId());
		boolean legacyBackReference = partner.getFireHosePartnerEndpointId() == null;
		if (hose.getBlockPos().equals(partner.getFireHosePartnerPos())
			&& (modernBackReference || legacyBackReference)) {
			partner.clearFireHoseConnection();
			markConnectionChanged(partner);
		}
	}

	public static boolean isWithinRange(Level level, BlockPos firstPos, BlockPos secondPos) {
		double maxLength = Config.hoseMaxLength + 1;
		return SableStructureCompat.distanceSquared(level, firstPos.getCenter(), secondPos.getCenter())
			<= maxLength * maxLength;
	}

	public static boolean isWithinRange(FireHoseBlockEntity first, FireHoseBlockEntity second) {
		if (first == null || second == null || first.getLevel() == null || second.getLevel() == null)
			return false;
		Level firstWorld = SableStructureCompat.worldLevel(first);
		Level secondWorld = SableStructureCompat.worldLevel(second);
		if (firstWorld == null || secondWorld == null || !firstWorld.dimension().equals(secondWorld.dimension()))
			return false;

		double maxLength = Config.hoseMaxLength + 1;
		return worldCenter(first).distanceToSqr(worldCenter(second)) <= maxLength * maxLength;
	}

	public static Vec3 worldCenter(FireHoseBlockEntity hose) {
		return SableStructureCompat.transformPositionToWorld(hose, hose.getBlockPos().getCenter());
	}

	private static FireHoseBlockEntity getEndpoint(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		return be instanceof FireHoseBlockEntity hose ? hose : null;
	}

	private static void markConnectionChanged(FireHoseBlockEntity hose) {
		hose.onFireHoseConnectionChanged();
		Level level = hose.getLevel();
		if (level == null)
			return;

		BlockPos pos = hose.getBlockPos();
		FluidPropagator.propagateChangedPipe(level, pos, level.getBlockState(pos));
		BlockPos backPos = pos.relative(hose.getBack());
		FluidPropagator.propagateChangedPipe(level, backPos, level.getBlockState(backPos));
	}

	private record EndpointSearchResult(
		@Nullable FireHoseBlockEntity preferred,
		@Nullable FireHoseBlockEntity claimedFallback
	) {
		private static final EndpointSearchResult NONE = new EndpointSearchResult(null, null);
	}
}
