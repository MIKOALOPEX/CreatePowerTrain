package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

/** The middle twelve texels repeat at native density; the four corner patches are never sampled. */
public final class CopycatHoseTexture {
    @FunctionalInterface public interface Tile { void accept(double from, double to, float low, float high); }
    public static void split(float start, float end, Tile output) {
        double length = (double) end - start;
        if (!Double.isFinite(length) || length < 1e-9) return;
        // Every partial tile is a centred crop, independent of the old hose texture phase.
        for (double cursor = 0; cursor < length - 1e-9;) {
            double span = Math.min(0.75, length - cursor);
            output.accept(cursor / length, (cursor + span) / length,
                (float) (0.5 - span / 2), (float) (0.5 + span / 2));
            cursor += span;
        }
    }
    /** Clip a subdivision against the UV layout of the whole connection, without restarting its crop. */
    public static void splitRun(double start, double end, double total, Tile output) {
        double length = end - start;
        if (!Double.isFinite(total) || !Double.isFinite(length) || length < 1e-9 || total <= 0) return;
        double limit = Math.min(end, total);
        for (double cursor = Math.max(0, start); cursor < limit - 1e-9;) {
            double tileStart = Math.floor(cursor / 0.75 + 1e-9) * 0.75;
            double span = Math.min(0.75, total - tileStart);
            double next = Math.min(limit, tileStart + span);
            if (next <= cursor) break;
            double low = 0.5 - span / 2;
            output.accept((cursor - start) / length, (next - start) / length,
                (float) (low + cursor - tileStart), (float) (low + next - tileStart));
            cursor = next;
        }
    }
    private CopycatHoseTexture() {}
}
