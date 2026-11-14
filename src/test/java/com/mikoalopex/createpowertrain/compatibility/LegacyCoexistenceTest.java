package com.mikoalopex.createpowertrain.compatibility;

public final class LegacyCoexistenceTest {
    static class LegacySliding extends com.mikoalopex.createfirefightingadd.content.blocks.rope_latch.PinnedRopeHandle {}
    public static void main(String[] args) throws Exception {
        check(!LegacyRopeOwnership.isLegacyPinned(null), "Missing handle");
        check(!LegacyRopeOwnership.isLegacyPinned(new Object()), "Native or foreign unpinned handle");
        check(LegacyRopeOwnership.isLegacyPinned(new LegacySliding()), "Legacy sliding subclasses retain ownership");
        check(!KineticReadScope.active(), "Initial state");
        float total = KineticReadScope.read(() -> {
            check(KineticReadScope.active(), "Outer native read");
            float nested = KineticReadScope.read(() -> 12f);
            check(KineticReadScope.active(), "Nested read restores outer scope");
            return nested + 8f;
        });
        check(total == 20 && !KineticReadScope.active(), "Result and scope restored");
        try { KineticReadScope.read(() -> { throw new IllegalStateException("fixture"); }); }
        catch (IllegalStateException expected) { check(!KineticReadScope.active(), "Exception restores scope"); }
        java.util.concurrent.atomic.AtomicBoolean other = new java.util.concurrent.atomic.AtomicBoolean(true);
        KineticReadScope.read(() -> {
            Thread thread = new Thread(() -> other.set(KineticReadScope.active()));
            thread.start();
            try { thread.join(); } catch (InterruptedException e) { throw new AssertionError(e); }
            return 0f;
        });
        check(!other.get(), "Native read does not suppress another thread");
        System.out.println("Legacy coexistence ownership and shared native-read scope passed");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
