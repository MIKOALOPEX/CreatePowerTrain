package com.mikoalopex.createpowertrain.integration.synaxis;

/** Loads the real dependency jar's API without starting Minecraft or a physics world. */
public final class SynaxisApiTest {
    public static void main(String[] args) throws Exception {
        if (!CouplingPhysics.available()) throw new AssertionError("Welding API unavailable");
        String physics = "com.verr1.synaxis.foundation.physics.";
        Class<?> body = Class.forName(physics + "BodyId");
        Object a = body.getConstructor(net.minecraft.resources.ResourceKey.class, java.util.UUID.class)
            .newInstance(net.minecraft.world.level.Level.OVERWORLD, new java.util.UUID(0, 0));
        Object b = body.getConstructor(net.minecraft.resources.ResourceKey.class, java.util.UUID.class)
            .newInstance(net.minecraft.world.level.Level.OVERWORLD, java.util.UUID.randomUUID());
        Class<?> axis = Class.forName(physics + "constraint.ConstraintAxis");
        java.util.Set<Object> locked = new java.util.HashSet<>();
        for (String name : new String[] {"LINEAR_X", "LINEAR_Y", "LINEAR_Z", "ANGULAR_Z"})
            locked.add(axis.getField(name).get(null));
        Class<?> generic = Class.forName(physics + "constraint.spec.GenericConstraintSpec");
        Object spec = generic.getConstructor(body, body, org.joml.Vector3dc.class, org.joml.Vector3dc.class,
            org.joml.Quaterniondc.class, org.joml.Quaterniondc.class, java.util.Set.class)
            .newInstance(a, b, new org.joml.Vector3d(), new org.joml.Vector3d(),
                new org.joml.Quaterniond(), new org.joml.Quaterniond(), locked);
        if (!generic.getMethod("lockedAxes").invoke(spec).equals(locked))
            throw new AssertionError("Independent yaw/pitch locks changed");
        try {
            Class<?> config = Class.forName(physics + "constraint.ConstraintConfiguration");
            Object limits = config.getMethod("withGenericLimit", axis, double.class, double.class)
                .invoke(config.getField("EMPTY").get(null), axis.getField("ANGULAR_X").get(null), -Math.PI / 4, Math.PI / 4);
            java.util.Map<?, ?> values = (java.util.Map<?, ?>) config.getMethod("genericLimits").invoke(limits);
            if (values.size() != 1 || !values.containsKey(axis.getField("ANGULAR_X").get(null)))
                throw new AssertionError("Yaw must remain unlimited");
        } catch (ClassNotFoundException legacy) {
            // Synaxis 1.4 uses the angular-limit command after replacing the spec.
            Class<?> limit = Class.forName("com.verr1.synaxis.compat.sableblueprint.weld.WeldAngularLimit");
            Class<?> arc = Class.forName(limit.getName() + "$ArcSelection");
            Object value = limit.getConstructor(double.class, double.class, arc)
                .newInstance(-Math.PI / 4, Math.PI / 4, arc.getField("BETWEEN_ENDPOINTS").get(null));
            if (limit.getMethod("command").invoke(value) == null) throw new AssertionError("Missing pitch limit command");
        }
        System.out.println("Synaxis welding constructors, limits and constraint methods resolved");
    }
}
