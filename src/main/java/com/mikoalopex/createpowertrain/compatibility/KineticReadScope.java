package com.mikoalopex.createpowertrain.compatibility;

import java.util.function.Supplier;

/** Both mods must exclude bridged totals when sampling a native Create network. */
public final class KineticReadScope {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private KineticReadScope() {}
    public static boolean active() { return DEPTH.get() > 0; }
    public static float read(Supplier<Float> operation) {
        int depth = DEPTH.get();
        DEPTH.set(depth + 1);
        try { return operation.get(); }
        finally { if (depth == 0) DEPTH.remove(); else DEPTH.set(depth); }
    }
}
