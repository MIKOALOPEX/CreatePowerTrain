package com.mikoalopex.createpowertrain.compatibility;

/** Recognizes the legacy wrapper without loading any optional Firefighting class. */
public final class LegacyRopeOwnership {
    private LegacyRopeOwnership() {}
    public static boolean isLegacyPinned(Object handle) {
        for (Class<?> type = handle == null ? null : handle.getClass(); type != null; type = type.getSuperclass())
            if (type.getName().equals("com.mikoalopex.createfirefightingadd.content.blocks.rope_latch.PinnedRopeHandle"))
                return true;
        return false;
    }
}
