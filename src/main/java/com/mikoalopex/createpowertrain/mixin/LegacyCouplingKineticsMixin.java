package com.mikoalopex.createpowertrain.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mikoalopex.createpowertrain.compatibility.KineticReadScope;
import com.simibubi.create.content.kinetics.KineticNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional path for the integrated mod; it keeps both sets of couplings enabled. */
@Pseudo
@Mixin(targets = "com.mikoalopex.createfirefightingadd.content.kinetics.coupling.CouplingKinetics", remap = false)
public class LegacyCouplingKineticsMixin {
    @Inject(method = "extra", at = @At("HEAD"), cancellable = true, require = 0)
    private static void powertrain$nativeTotals(KineticNetwork network, boolean capacity, CallbackInfoReturnable<Float> result) {
        if (KineticReadScope.active()) result.setReturnValue(0f);
    }
    @WrapOperation(method = "writeLocalNetwork", at = @At(value = "INVOKE",
        target = "Lcom/simibubi/create/content/kinetics/KineticNetwork;calculateCapacity()F"), require = 0)
    private static float powertrain$readCapacity(KineticNetwork network, Operation<Float> original) {
        return KineticReadScope.read(() -> original.call(network));
    }
    @WrapOperation(method = "writeLocalNetwork", at = @At(value = "INVOKE",
        target = "Lcom/simibubi/create/content/kinetics/KineticNetwork;calculateStress()F"), require = 0)
    private static float powertrain$readStress(KineticNetwork network, Operation<Float> original) {
        return KineticReadScope.read(() -> original.call(network));
    }
    @WrapOperation(method = "update", at = @At(value = "INVOKE",
        target = "Lcom/simibubi/create/content/kinetics/KineticNetwork;calculateCapacity()F"), require = 0)
    private float powertrain$updateCapacity(KineticNetwork network, Operation<Float> original) {
        return KineticReadScope.read(() -> original.call(network));
    }
    @WrapOperation(method = "update", at = @At(value = "INVOKE",
        target = "Lcom/simibubi/create/content/kinetics/KineticNetwork;calculateStress()F"), require = 0)
    private float powertrain$updateStress(KineticNetwork network, Operation<Float> original) {
        return KineticReadScope.read(() -> original.call(network));
    }
}
