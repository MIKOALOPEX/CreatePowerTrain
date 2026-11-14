package com.mikoalopex.createpowertrain.compatibility;

import com.mikoalopex.createpowertrain.integration.sable.SableStructureCompat;
import com.mikoalopex.createpowertrain.integration.synaxis.CouplingPhysics;
import com.mikoalopex.createpowertrain.integration.synaxis.CouplingAlignment;
import com.mikoalopex.createpowertrain.integration.simulated.SimulatedPhysicalHoseBlockEntity;

/** Validates the required physics API as one unit. There are no partial feature tiers. */
public final class RequiredPhysics {
    private static boolean initialized;
    public static synchronized void initialize() {
        if (initialized) return;
        try {
            if (!SableStructureCompat.available()) throw new IllegalStateException("Sable spatial API unavailable");
            if (!CouplingPhysics.available()) throw new IllegalStateException("Synaxis constraint API unavailable");
            if (!CouplingAlignment.available()) throw new IllegalStateException("Sable alignment API unavailable");
            SimulatedPhysicalHoseBlockEntity.probe();
            ClassLoader loader = RequiredPhysics.class.getClassLoader();
            String rope = "dev.simulated_team.simulated.content.blocks.rope.";
            Class<?> holder = Class.forName(rope + "RopeStrandHolderBehavior", false, loader);
            holder.getMethod("getAttachedStrand");
            holder.getMethod("isAttached");
            holder.getMethod("destroyRope", net.minecraft.server.level.ServerPlayer.class, net.minecraft.world.phys.Vec3.class, boolean.class);
            Class<?> strand = Class.forName(rope + "strand.server.ServerRopeStrand", false, loader);
            strand.getMethod("getUUID");
            Class<?> physics = Class.forName("dev.ryanhcode.sable.api.physics.object.rope.RopePhysicsObject", false, loader);
            Class<?> system = Class.forName("dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem", false, loader);
            physics.getMethod("onAddition", system);
            physics.getDeclaredMethod("remove");
            physics.getDeclaredField("handle");
            strand.getMethod("prePhysicsTick", system, net.minecraft.server.level.ServerLevel.class, double.class);
            Class.forName("dev.ryanhcode.sable.api.physics.object.rope.RopeHandle", false, loader);
            com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterCompatibility.validateNativeAdapter();
            initialized = true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            throw new IllegalStateException("Create: Powertrain requires a compatible complete Sable/Simulated/Synaxis physics installation", error);
        }
    }
    public static boolean ready() { initialize(); return initialized; }
    private RequiredPhysics() {}
}
