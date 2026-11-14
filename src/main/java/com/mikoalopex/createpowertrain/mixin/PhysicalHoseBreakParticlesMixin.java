package com.mikoalopex.createpowertrain.mixin;

import com.mikoalopex.createpowertrain.content.blocks.physical_hose.PhysicalHoseBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Change only Powertrain hose debris; native ropes retain their own particle material. */
@Pseudo
@Mixin(targets = "dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior", remap = false)
public abstract class PhysicalHoseBreakParticlesMixin {
    @ModifyArg(method = "destroyRope", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/particles/BlockParticleOption;<init>(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/world/level/block/state/BlockState;)V"), index = 1)
    private BlockState powertrain$hoseDebris(BlockState original) {
        return ((RopeStrandHolderBehavior) (Object) this).blockEntity instanceof PhysicalHoseBlockEntity hose
            ? hose.ropeBreakParticleState() : original;
    }
}
