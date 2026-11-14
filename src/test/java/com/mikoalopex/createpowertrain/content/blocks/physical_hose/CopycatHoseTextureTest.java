package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class CopycatHoseTextureTest {
    public static void main(String[] args) {
        checkEmission();
        checkFilledConnection();
        checkTubeFaceOrientation();
        checkSubdividedConnection(false);
        checkSubdividedConnection(true);
        for (float start : new float[] {-0.2f, 0, 0.125f, 0.875f, 1}) {
            for (float length : new float[] {0.1f, 0.75f, 1, 2.25f}) {
                double[] coverage = {0};
                CopycatHoseTexture.split(start, start + length, (from, to, low, high) -> {
                    if (low < 0.125f || high > 0.875f) throw new AssertionError("Corner pixels sampled");
                    close((low + high) / 2, 0.5);
                    close(from, coverage[0]); coverage[0] = to;
                    close((to - from) * length, high - low);
                });
                close(coverage[0], 1);
            }
        }
        for (Direction facing : Direction.values()) for (int width : new int[] {4, 6, 8}) {
            Vec3 centre = new Vec3(0.5, 0.5, 0.5);
            if (!PhysicalHoseGeometry.targetsCopycatBody(facing, width, centre, facing)) throw new AssertionError("Centre must select tube");
            if (PhysicalHoseGeometry.targetsCopycatBody(facing, width, centre, facing.getOpposite())) throw new AssertionError("Back must select port");
            Vec3 rim = centre.add(facing.getAxis() == Direction.Axis.X ? new Vec3(0, width / 32.0 + 0.01, 0) : new Vec3(width / 32.0 + 0.01, 0, 0));
            if (PhysicalHoseGeometry.targetsCopycatBody(facing, width, rim, facing)) throw new AssertionError("Rim must select port");
        }
        System.out.println("Copycat hose twelve-pixel tiling and part selection passed");
    }
    private static void checkEmission() {
        int[] actual = new int[2];
        var sink = (com.mojang.blaze3d.vertex.VertexConsumer) java.lang.reflect.Proxy.newProxyInstance(
            CopycatHoseTextureTest.class.getClassLoader(), new Class<?>[] {com.mojang.blaze3d.vertex.VertexConsumer.class},
            (proxy, method, args) -> {
                if (method.getName().equals("setUv2")) { actual[0] = (int) args[0]; actual[1] = (int) args[1]; }
                return proxy;
            });
        for (int emission : new int[] {0, 7, 15}) for (int ambient : new int[] {0, 5, 15}) {
            var consumer = new CopycatHoseRendering.EmissionConsumer(sink, emission);
            consumer.setLight(net.minecraft.client.renderer.LightTexture.pack(ambient, 11));
            if (actual[0] != Math.max(emission, ambient) * 16 || actual[1] != 11 * 16)
                throw new AssertionError("Emission must preserve sky and brighter ambient light");
            // Baked segment quads use putBulkData; curved strips use setLight directly.
            int[] vertices = new int[32];
            for (int vertex = 0; vertex < 4; vertex++) vertices[vertex * 8 + 3] = -1;
            var quad = new net.minecraft.client.renderer.block.model.BakedQuad(vertices, -1, Direction.UP, null, false);
            consumer.putBulkData(new com.mojang.blaze3d.vertex.PoseStack().last(), quad, 1, 1, 1, 1,
                net.minecraft.client.renderer.LightTexture.pack(ambient, 11), 0);
            if (actual[0] != Math.max(emission, ambient) * 16 || actual[1] != 11 * 16)
                throw new AssertionError("Baked and curved surfaces must use identical emission");
        }
    }
    private static void checkTubeFaceOrientation() {
        for (Direction side : new Direction[] {Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST}) {
            for (int width : new int[] {4, 6, 8}) for (int rotation = 0; rotation < 4; rotation++) {
                int[] shape = new int[32], material = new int[32];
                float lo = (16 - width) / 32f, hi = 1 - lo;
                float[] across = {lo, lo, hi, hi}, along = {0.125f, 0.875f, 0.875f, 0.125f};
                float[] us = {0.2f, 0.2f, 0.3f, 0.3f}, vs = {0.4f, 0.6f, 0.6f, 0.4f};
                for (int i = 0; i < 4; i++) {
                    double x = switch (side) { case UP -> across[i]; case DOWN -> 1 - across[i]; case WEST -> lo; default -> hi; };
                    double y = switch (side) { case WEST -> across[i]; case EAST -> 1 - across[i]; case DOWN -> lo; default -> hi; };
                    shape[i * 8] = Float.floatToRawIntBits((float) x);
                    shape[i * 8 + 1] = Float.floatToRawIntBits((float) y);
                    shape[i * 8 + 2] = Float.floatToRawIntBits(along[i]);
                    material[i * 8 + 4] = Float.floatToRawIntBits(us[(i + rotation) % 4]);
                    material[i * 8 + 5] = Float.floatToRawIntBits(vs[(i + rotation) % 4]);
                }
                var template = new net.minecraft.client.renderer.block.model.BakedQuad(shape, -1, side, null, true);
                var source = new net.minecraft.client.renderer.block.model.BakedQuad(material, 2, side, null, true);
                var result = CopycatHoseRendering.mapTubeFace(template, source);
                for (int i = 0; i < 4; i++) {
                    close(Float.intBitsToFloat(result.getVertices()[i * 8 + 4]), 0.2 + across[i] * 0.1);
                    close(Float.intBitsToFloat(result.getVertices()[i * 8 + 5]), 0.4 + along[i] * 0.2);
                    close(CopycatHoseUV.bridgeAcross(across[i] - 0.25f), across[i]);
                }
                if (result.getTintIndex() != 2 || !java.util.Arrays.equals(template.getVertices(), shape))
                    throw new AssertionError("Tint or template geometry changed");
            }
        }
    }
    private static void checkFilledConnection() {
        var uv = new java.util.ArrayList<float[]>();
        // Exercise the actual vertex-emission branch for a filled copycat, not just the tiling helper.
        var sink = (com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.SurfaceConsumer)
            java.lang.reflect.Proxy.newProxyInstance(CopycatHoseTextureTest.class.getClassLoader(),
                new Class<?>[] {com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.SurfaceConsumer.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("cropCopycatBase")) return false;
                    if (method.getName().equals("centreCopycatTexture")) return true;
                    if (method.getName().equals("surfaceRunLength")) return 0f;
                    if (method.getName().equals("setUv")) uv.add(new float[] {(float) arguments[0], (float) arguments[1]});
                    return method.getReturnType() == void.class ? null : proxy;
                });
        for (float length : new float[] {0.25f, 1f}) {
            uv.clear();
            com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.renderSegment(
                new com.mojang.blaze3d.vertex.PoseStack(), new org.joml.Vector3d(0, 0, 1), new org.joml.Vector3d(0, 0, 1),
                new org.joml.Vector3d(0, 1, 0), new org.joml.Vector3d(0, 1, 0),
                new org.joml.Vector3d(), new org.joml.Vector3d(0, 0, length),
                0.875f, 0.875f + length, 0xf000f0, -1, sink, 8, 8);
            if (uv.size() != (length == 0.25f ? 16 : 32)) throw new AssertionError("Wrong filled-copycat tile count");
            for (int i = 0; i < uv.size(); i += 4) {
                float low = uv.get(i)[1], high = uv.get(i + 1)[1];
                close((low + high) / 2, 0.5);
                if (low < 0.125f || high > 0.875f) throw new AssertionError("Filled connector sampled texture ends");
                if (length == 0.25f) { close(low, 6 / 16f); close(high, 10 / 16f); }
            }
        }
    }
    private static void checkSubdividedConnection(boolean base) {
        var uv = new java.util.ArrayList<float[]>();
        var sink = (com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.SurfaceConsumer)
            java.lang.reflect.Proxy.newProxyInstance(CopycatHoseTextureTest.class.getClassLoader(),
                new Class<?>[] {com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.SurfaceConsumer.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("cropCopycatBase")) return base;
                    if (method.getName().equals("centreCopycatTexture")) return true;
                    if (method.getName().equals("surfaceRunLength")) return 0.25f;
                    if (method.getName().equals("setUv")) uv.add(new float[] {(float) arguments[0], (float) arguments[1]});
                    return method.getReturnType() == void.class ? null : proxy;
                });
        for (int pieces : new int[] {1, 8, 32}) {
            float[] phases = new float[4];
            for (int i = 0; i < pieces; i++) {
                float from = i * 0.25f / pieces, to = (i + 1) * 0.25f / pieces;
                uv.clear();
                com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.renderSurfaceSegment(
                    new com.mojang.blaze3d.vertex.PoseStack(), new org.joml.Vector3d(0, 0, 1), new org.joml.Vector3d(0, 0, 1),
                    new org.joml.Vector3d(0, 1, 0), new org.joml.Vector3d(0, 1, 0),
                    new org.joml.Vector3d(0, 0, from), new org.joml.Vector3d(0, 0, to), phases, 0xf000f0, -1, sink, 8, 8);
                if (uv.size() != (base ? 32 : 16)) throw new AssertionError("Unexpected subdivision geometry");
                for (int vertex = 0; vertex < uv.size(); vertex += 4) {
                    close(uv.get(vertex)[1], 0.375 + from);
                    close(uv.get(vertex + 1)[1], 0.375 + to);
                    close(uv.get(vertex + 1)[1] - uv.get(vertex)[1], to - from);
                }
            }
        }
        // Subdivisions crossing a repeat boundary must clip that repeat, not create a new crop.
        for (int i = 0; i < 32; i++) {
            double start = i / 32.0, end = (i + 1) / 32.0;
            CopycatHoseTexture.splitRun(start, end, 1, (from, to, low, high) -> {
                double p = start + from * (end - start);
                double expected = p < 0.75 - 1e-8 ? 0.125 + p : 0.375 + p - 0.75;
                close(low, expected);
                close(high - low, (to - from) * (end - start));
            });
        }
    }
    private static void close(double actual, double expected) { if (Math.abs(actual - expected) > 1e-6) throw new AssertionError(actual + " != " + expected); }
}
