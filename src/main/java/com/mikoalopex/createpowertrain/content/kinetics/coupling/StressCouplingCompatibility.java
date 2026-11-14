package com.mikoalopex.createpowertrain.content.kinetics.coupling;
import com.mikoalopex.createpowertrain.compatibility.RequiredPhysics;
import com.mikoalopex.createpowertrain.integration.sable.SableBallCouplingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
public final class StressCouplingCompatibility {
    public static boolean enabled() { return RequiredPhysics.ready(); }
    public static boolean visible() { return RequiredPhysics.ready(); }
    public static BallCouplingBlockEntity create(BlockPos pos, BlockState state) {
        RequiredPhysics.initialize();
        return new SableBallCouplingBlockEntity(pos, state);
    }
    public static void notifyPlayer(Player player) { RequiredPhysics.initialize(); }
    private StressCouplingCompatibility() {}
}
