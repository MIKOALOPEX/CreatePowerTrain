package com.mikoalopex.createpowertrain.mixin;

import com.mikoalopex.createpowertrain.content.blocks.rope_latch.RopeLatchCompatibility;
import com.mikoalopex.createpowertrain.content.blocks.rope_latch.RopeLatchIndex;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reuses bounds already computed by Sable's ticket update, without scanning rope points again. */
@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.api.physics.object.rope.RopePhysicsObject", remap = false)
public abstract class RopeLatchBoundsMixin {
    @Unique private ServerLevel powertrain$ropeLevel;

    @Inject(method = "onAddition", at = @At("RETURN"))
    private void powertrain$added(SubLevelPhysicsSystem system, CallbackInfo ci) {
        powertrain$ropeLevel = system.getLevel();
    }

    @Inject(method = "getBoundingBox", at = @At("RETURN"))
    private void powertrain$bounds(BoundingBox3d bounds, CallbackInfo ci) {
        if (powertrain$ropeLevel != null)
            RopeLatchIndex.observe(powertrain$ropeLevel, this, bounds);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void powertrain$removed(CallbackInfo ci) {
        if (powertrain$ropeLevel != null) {
            RopeLatchIndex.forget(powertrain$ropeLevel, this);
            powertrain$ropeLevel = null;
        }
    }
}
