package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import net.minecraft.core.Direction;

/** Common texture axes for straight and curved tube surfaces: V always follows local +Z. */
final class CopycatHoseUV {
    record Region(float minU, float maxU, float minV, float maxV) {
        float u(double across) { return (float) (minU + (maxU - minU) * across); }
        float v(double along) { return (float) (minV + (maxV - minV) * along); }
    }

    static Region region(int[] vertices) {
        int stride = vertices.length / 4;
        float minU = Float.POSITIVE_INFINITY, maxU = Float.NEGATIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            float u = Float.intBitsToFloat(vertices[i * stride + 4]);
            float v = Float.intBitsToFloat(vertices[i * stride + 5]);
            minU = Math.min(minU, u); maxU = Math.max(maxU, u);
            minV = Math.min(minV, v); maxV = Math.max(maxV, v);
        }
        // Retain the selected atlas/connected-texture region, rather than sampling the entire sprite.
        return new Region(minU, maxU, minV, maxV);
    }

    static double across(Direction side, double x, double y) {
        return switch (side) {
            case DOWN -> 1 - x;
            case UP -> x;
            case WEST -> y;
            case EAST -> 1 - y;
            default -> throw new IllegalArgumentException("Not a tube side: " + side);
        };
    }
    static double bridgeAcross(float hoseU) { return hoseU + 0.25; }
    private CopycatHoseUV() {}
}
