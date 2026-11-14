package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Preserves material face textures and tint; tube UV axes follow the hose, port UVs follow the block. */
final class CopycatHoseRendering {
    private final PhysicalHoseBlockEntity entity;
    private final BlockState material;
    private final boolean filled;
    private final boolean tubeBody;
    private final BakedModel source;
    private final ModelData data;
    private final VertexConsumer buffer;
    private final Map<BakedModel, List<BakedQuad>> mapped = new HashMap<>();
    private final Map<Direction, List<BakedQuad>> faces = new HashMap<>();

    CopycatHoseRendering(PhysicalHoseBlockEntity entity, HoseMaterial slot, MultiBufferSource buffers, boolean tubeBody) {
        this.entity = entity; material = slot.state(); filled = slot.filled();
        this.tubeBody = tubeBody;
        source = Minecraft.getInstance().getBlockRenderer().getBlockModel(material);
        data = source.getModelData(entity.getLevel(), entity.getBlockPos(), material, ModelData.EMPTY);
        buffer = new EmissionConsumer(buffers.getBuffer(ItemBlockRenderTypes.getRenderType(material, false)), slot.emission(entity.getLevel(), entity.getBlockPos()));
        var random = RandomSource.create(42);
        for (var type : source.getRenderTypes(material, random, data)) {
            for (int i = -1; i < 6; i++) {
                random.setSeed(42);
                for (var quad : source.getQuads(material, i < 0 ? null : Direction.from3DDataValue(i), random, data, type))
                    faces.computeIfAbsent(quad.getDirection(), key -> new ArrayList<>()).add(quad);
            }
        }
    }

    private int tint(BakedQuad quad) {
        return quad != null && quad.isTinted() ? Minecraft.getInstance().getBlockColors()
            .getColor(material, entity.getLevel(), entity.getBlockPos(), quad.getTintIndex()) : 0xFFFFFF;
    }

    void draw(BakedModel template, PoseStack pose, int light, int overlay) {
        for (var quad : mapped.computeIfAbsent(template, this::map)) {
            int color = tint(quad);
            buffer.putBulkData(pose.last(), quad, (color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f,
                (color & 255) / 255f, 1, light, overlay);
        }
    }

    private List<BakedQuad> map(BakedModel template) {
        var result = new ArrayList<BakedQuad>();
        var random = RandomSource.create(42);
        for (int i = -1; i < 6; i++) {
            random.setSeed(42);
            for (var shape : template.getQuads(null, i < 0 ? null : Direction.from3DDataValue(i), random)) {
                if (!filled) { result.add(shape); continue; }
                for (var face : faces.getOrDefault(shape.getDirection(), List.of()))
                    result.add(tubeBody ? mapTubeFace(shape, face) : mapFace(shape, face));
            }
        }
        return result;
    }

    static BakedQuad mapTubeFace(BakedQuad shape, BakedQuad face) {
        int[] vertices = shape.getVertices().clone();
        int stride = vertices.length / 4;
        var region = CopycatHoseUV.region(face.getVertices());
        for (int i = 0; i < 4; i++) {
            var p = BakedQuadHelper.getXYZ(vertices, i);
            vertices[i * stride + 4] = Float.floatToRawIntBits(region.u(CopycatHoseUV.across(shape.getDirection(), p.x, p.y)));
            vertices[i * stride + 5] = Float.floatToRawIntBits(region.v(p.z));
        }
        return new BakedQuad(vertices, face.getTintIndex(), shape.getDirection(), face.getSprite(), face.isShade());
    }

    static BakedQuad mapFace(BakedQuad shape, BakedQuad face) {
        int[] vertices = shape.getVertices().clone(), input = face.getVertices();
        int stride = vertices.length / 4, sourceStride = input.length / 4;
        var origin = BakedQuadHelper.getXYZ(input, 0);
        var a = BakedQuadHelper.getXYZ(input, 1).subtract(origin);
        var b = BakedQuadHelper.getXYZ(input, 3).subtract(origin);
        double aa = a.dot(a), ab = a.dot(b), bb = b.dot(b), det = aa * bb - ab * ab;
        if (Math.abs(det) < 1e-12) return shape;
        for (int i = 0; i < 4; i++) {
            var p = BakedQuadHelper.getXYZ(vertices, i).subtract(origin);
            double u = (p.dot(a) * bb - p.dot(b) * ab) / det;
            double v = (p.dot(b) * aa - p.dot(a) * ab) / det;
            for (int coordinate = 4; coordinate <= 5; coordinate++) {
                float start = Float.intBitsToFloat(input[coordinate]);
                vertices[i * stride + coordinate] = Float.floatToRawIntBits((float) (start
                    + u * (Float.intBitsToFloat(input[sourceStride + coordinate]) - start)
                    + v * (Float.intBitsToFloat(input[3 * sourceStride + coordinate]) - start)));
            }
        }
        return new BakedQuad(vertices, face.getTintIndex(), shape.getDirection(), face.getSprite(), face.isShade());
    }

    VertexConsumer tube() {
        VertexConsumer[] surfaces = new VertexConsumer[4];
        Direction[] directions = {Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST};
        for (int i = 0; i < 4; i++) {
            var candidates = faces.getOrDefault(directions[i], List.of());
            var face = candidates.isEmpty() ? null : candidates.getFirst();
            var sprite = face == null ? source.getParticleIcon(data) : face.getSprite();
            var consumer = new TintConsumer(buffer, tint(face));
            surfaces[i] = face == null || !filled ? sprite.wrap(consumer) : new FaceConsumer(consumer, face);
        }
        return new TubeConsumer(surfaces, !filled);
    }

    private static class ForwardConsumer implements VertexConsumer {
        protected VertexConsumer delegate;
        ForwardConsumer(VertexConsumer delegate) { this.delegate = delegate; }
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a); return this; }
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
    }

    /** Applies to both baked models and connecting strips; never modifies world lighting. */
    static final class EmissionConsumer extends ForwardConsumer {
        private final int minimumBlockLight;
        EmissionConsumer(VertexConsumer delegate, int emission) {
            super(delegate); minimumBlockLight = Math.clamp(emission, 0, 15) << 4;
        }
        @Override public VertexConsumer setUv2(int block, int sky) {
            delegate.setUv2(Math.max(block, minimumBlockLight), sky);
            return this;
        }
    }

    private static final class TubeConsumer extends ForwardConsumer
            implements com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer.SurfaceConsumer {
        private final VertexConsumer[] surfaces;
        private final boolean base;
        private float[] runLengths;
        private int selectedFace;
        TubeConsumer(VertexConsumer[] surfaces, boolean base) { super(surfaces[0]); this.surfaces = surfaces; this.base = base; }
        @Override public void selectSurface(int face) { delegate = surfaces[face]; selectedFace = face; }
        @Override public void beginSurfaceRun(float[] lengths) { runLengths = lengths; }
        @Override public float surfaceRunLength() { return runLengths == null ? 0 : runLengths[selectedFace]; }
        @Override public boolean cropCopycatBase() { return base; }
        @Override public boolean centreCopycatTexture() { return true; }
    }

    /** Curves use exactly the same across/along texture axes as the model's four tube faces. */
    private static final class FaceConsumer extends ForwardConsumer {
        private final CopycatHoseUV.Region region;
        FaceConsumer(VertexConsumer delegate, BakedQuad face) {
            super(delegate); region = CopycatHoseUV.region(face.getVertices());
        }
        @Override public VertexConsumer setUv(float u, float v) {
            delegate.setUv(region.u(CopycatHoseUV.bridgeAcross(u)), region.v(v));
            return this;
        }
    }

    private record TintConsumer(VertexConsumer delegate, int tint) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            delegate.setColor(r * (tint >> 16 & 255) / 255, g * (tint >> 8 & 255) / 255, b * (tint & 255) / 255, a); return this;
        }
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
    }
}
