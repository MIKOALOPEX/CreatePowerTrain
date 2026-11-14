package com.mikoalopex.createpowertrain.mixin;

import com.mikoalopex.createpowertrain.content.blocks.rope_latch.PinnedRopeHandle;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Older hooks must not replace a solver handle already owned by Powertrain. */
@Pseudo
@Mixin(targets = "com.mikoalopex.createfirefightingadd.content.blocks.rope_latch.PinnedRopeHandle", remap = false)
public class LegacyRopeCaptureMixin {
    @Inject(method = "canCapture", at = @At("HEAD"), cancellable = true, require = 0)
    private static void powertrain$keepOwner(ServerLevel level, ServerRopeStrand rope, CallbackInfoReturnable<Boolean> result) {
        if (PinnedRopeHandle.handle(rope) instanceof PinnedRopeHandle) result.setReturnValue(false);
    }
}
