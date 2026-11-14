package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import java.util.LinkedHashMap;
import java.util.Map;
import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseDynamicRenderer;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseBlock;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketRenderer;
import com.mikoalopex.createpowertrain.api.fire_hose.FireHoseEndpointModel;
import com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import static com.mikoalopex.createpowertrain.content.blocks.physical_hose.PhysicalHoseGeometry.vector;

/**
 * Renders hose segment models and joins their exposed faces with textured strips.
 */
public final class PhysicalHoseRenderer extends SafeBlockEntityRenderer<PhysicalHoseBlockEntity> {
    private static final Map<String, PartialModel> MODELS = new LinkedHashMap<>();
    static {
        for (int width : new int[] {4, 6, 8})
            for (String color : new String[] {"", "_black"})
                for (String part : new String[] {"a", "mid", "b"}) {
                    String name = width + "_" + part + color;
                    MODELS.put(name, PartialModel.of(CreatePowertrain.path("block/physical_fire_hose/" + name)));
                }
        for (int width : new int[] {4, 6, 8})
            MODELS.put(width + "_head", PartialModel.of(CreatePowertrain.path("block/physical_fire_hose/" + width + "_head")));
        for (int width : new int[] {4, 6, 8})
            for (String part : new String[] {"a", "mid", "b", "head"})
                MODELS.put("copycat_" + width + "_" + part,
                    PartialModel.of(CreatePowertrain.path("block/copycat_physical_hose/" + width + "_" + part)));
    }

    public PhysicalHoseRenderer(BlockEntityRendererProvider.Context context) {}
    public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        MODELS.values().forEach(model -> event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(model.modelLocation())));
    }

    @Override public boolean shouldRenderOffScreen(PhysicalHoseBlockEntity be) { return true; }
    @Override public boolean shouldRender(PhysicalHoseBlockEntity be, Vec3 camera) { return true; }

    @Override protected void renderSafe(PhysicalHoseBlockEntity be, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        PhysicalHoseBracketClient.track(be);
        var appearance = be.getHoseAppearance();
        if (be.isCopycat() || be.getBlockState().getValue(FireHoseBlock.APPEARANCE) == FireHoseEndpointModel.CUSTOM) {
            var facing = be.getBlockState().getValue(FireHoseBlock.FACING);
            int x = facing == net.minecraft.core.Direction.UP ? 0 : facing == net.minecraft.core.Direction.DOWN ? 180 : 90;
            int y = switch (facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
            pose.pushPose();
            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(net.minecraft.client.resources.model.BlockModelRotation.by(x, y).getRotation().getLeftRotation());
            pose.translate(-0.5, -0.5, -0.5);
            if (be.isCopycat())
                new CopycatHoseRendering(be, be.portMaterial, buffers, false).draw(MODELS.get("copycat_" + be.width() + "_head").get(), pose, light, overlay);
            else HoseBracketRenderer.renderPipe(MODELS.get(be.width() + "_head").get(), pose,
                    buffers.getBuffer(RenderType.entityCutoutNoCull(appearance.endpointTexture())), light);
            pose.popPose();
        }
        boolean belt = be.isCopycat() && be.bodyMaterial.isBelt();
        if (belt) appearance = FireHoseAppearances.get(be.bodyMaterial.beltAppearance());
        var texture = appearance.hoseTexture();
        if (texture == null && !be.isCopycat()) return;
        var indexed = PhysicalHoseAnchors.indexedSegments(be.renderPoints(partial));
        var segments = new java.util.ArrayList<>(indexed.stream().filter(java.util.Objects::nonNull).toList());
        if (segments.isEmpty()) return;
        var copycat = be.isCopycat() && !belt ? new CopycatHoseRendering(be, be.bodyMaterial, buffers, true) : null;
        alignFixedSegments(be, segments, indexed);
        int width = be.width();
        var start = be.renderEnd(true);
        var end = be.renderEnd(false);
        if (start != null && end != null && !PhysicalHoseGeometry.ownerAtFirst(start.point(), end.point(), segments)) {
            var swap = start;
            start = end;
            end = swap;
        }
        for (int i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            if (segment.middleLength() >= 0.75 - 1e-6)
                draw(width + "_mid", segment, pose, buffers, appearance, copycat, belt, light, overlay);
            if (copycat == null && i > 0 && PhysicalHoseGeometry.straight(segments.get(i - 1), segment)) {
                draw(width + "_a", segment, pose, buffers, appearance, copycat, belt, light, overlay);
                draw(width + "_b", segments.get(i - 1), pose, buffers, appearance, copycat, belt, light, overlay);
            }
        }
        VertexConsumer tube = copycat == null ? buffers.getBuffer(RenderType.entityCutoutNoCull(texture)) : copycat.tube();
        for (int i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            if (segment.middleLength() < 0.75 - 1e-6)
                line(segment.midStart(), segment.midEnd(), segment.normal(), segment.up(),
                    (float) (0.5 - segment.middleLength() / 2), pose, tube, light, width);
            if (i > 0 && !PhysicalHoseGeometry.straight(segments.get(i - 1), segment)) {
                var previous = segments.get(i - 1);
                bridge(previous.midEnd(), segment.midStart(), previous.normal(), segment.normal().scale(-1),
                    previous.up(), segment.up(), pose, tube, light, width);
            }
        }
        var first = segments.getFirst();
        var last = segments.getLast();
        if (start != null) headBridge(start, first.midStart(), first.normal().scale(-1), first.up(), pose, tube, light, width);
        else line(first.start(), first.midStart(), first.normal(), first.up(), 0, pose, tube, light, width);
        if (end != null) headBridge(end, last.midEnd(), last.normal(), last.up(), pose, tube, light, width);
        else line(last.midEnd(), last.end(), last.normal(), last.up(), 0.875f, pose, tube, light, width);
    }

    private static void alignFixedSegments(PhysicalHoseBlockEntity hose, java.util.List<PhysicalHoseGeometry.Segment> segments, java.util.List<PhysicalHoseGeometry.Segment> indexed) {
        for (var binding : hose.anchors) {
            var bracket = binding.resolve(hose.getLevel());
            if (bracket == null || binding.segment() >= indexed.size()) continue;
            int renderIndex = segments.indexOf(indexed.get(binding.segment()));
            if (renderIndex < 0) continue;
            Vec3 up = com.mikoalopex.createpowertrain.integration.sable.SableStructureClientCompat.renderNormalToWorld(bracket,
                com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketBlock.rotate(
                    bracket.getBlockState().getValue(com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketBlock.FACING),
                    bracket.angle(), new Vec3(0, 1, 0)));
            Vec3 x = com.mikoalopex.createpowertrain.integration.sable.SableStructureClientCompat.renderNormalToWorld(hose, new Vec3(1, 0, 0));
            Vec3 y = com.mikoalopex.createpowertrain.integration.sable.SableStructureClientCompat.renderNormalToWorld(hose, new Vec3(0, 1, 0));
            Vec3 z = com.mikoalopex.createpowertrain.integration.sable.SableStructureClientCompat.renderNormalToWorld(hose, new Vec3(0, 0, 1));
            var segment = segments.get(renderIndex);
            Vec3 localUp = new Vec3(up.dot(x), up.dot(y), up.dot(z));
            localUp = localUp.subtract(segment.normal().scale(localUp.dot(segment.normal()))).normalize();
            segments.set(renderIndex, new PhysicalHoseGeometry.Segment(segment.start(), segment.end(), segment.normal(),
                localUp, segment.length(), segment.middleLength()));
        }
    }

    private static void draw(String name, PhysicalHoseGeometry.Segment segment, PoseStack pose,
            MultiBufferSource buffers, FireHoseAppearances.Entry appearance, CopycatHoseRendering copycat, boolean belt, int light, int overlay) {
        Vector3d normal = vector(segment.normal()), up = vector(segment.up());
        var rotation = new Matrix3d(up.cross(normal, new Vector3d()), up, normal).getNormalizedRotation(new org.joml.Quaterniond());
        Vec3 center = segment.center();
        pose.pushPose();
        pose.translate(center.x, center.y, center.z);
        pose.mulPose(new Quaternionf(rotation));
        pose.translate(-0.5, -0.5, -0.5);
        if (copycat != null) copycat.draw(MODELS.get("copycat_" + name).get(), pose, light, overlay);
        else if (belt || FireHoseAppearances.endpointModel(appearance.id()) == FireHoseEndpointModel.CUSTOM)
            HoseBracketRenderer.renderPipe(MODELS.get(name).get(), pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(appearance.hoseTexture())), light);
        else
            CachedBuffers.partial(MODELS.get(name + (FireHoseAppearances.isBlack(appearance.id()) ? "_black" : "")),
                Blocks.AIR.defaultBlockState()).light(light).overlay(overlay)
                .renderInto(pose, buffers.getBuffer(RenderType.cutout()));
        pose.popPose();
    }

    private static void headBridge(PhysicalHoseBlockEntity.EndFrame head, Vec3 mid, Vec3 normal, Vec3 up,
            PoseStack pose, VertexConsumer buffer, int light, int width) {
        Vec3 direction = normal.scale(-1);
        Vec3 endUp = PhysicalHoseGeometry.alignedUp(head.normal(), head.up(), direction, up);
        // Join the two model faces directly; a spline can fold back inside a short terminal gap.
        FireHoseDynamicRenderer.renderSurfaceSegment(pose, vector(head.normal()), vector(direction),
            vector(head.up()), vector(endUp), vector(head.point()), vector(mid), new float[4], light, -1, buffer, width, width);
    }

    private static void line(Vec3 a, Vec3 b, Vec3 normal, Vec3 up, float start,
            PoseStack pose, VertexConsumer buffer, int light, int width) {
        if (a.distanceToSqr(b) < 1e-12) return;
        FireHoseDynamicRenderer.renderSegment(pose, vector(normal), vector(normal), vector(up), vector(up),
            vector(a), vector(b), start, start + (float) a.distanceTo(b), light, -1, buffer, width, width);
    }

    private static void bridge(Vec3 a, Vec3 b, Vec3 normalA, Vec3 normalB, Vec3 upA, Vec3 upB,
            PoseStack pose, VertexConsumer buffer, int light, int width) {
        var points = FireHoseDynamicRenderer.attachedSpline(a, b, normalA, normalB, upA, upB, true);
        float[] phases = {0.875f, 0.875f, 0.875f, 0.875f};
        var copycat = buffer instanceof FireHoseDynamicRenderer.SurfaceConsumer surface && surface.centreCopycatTexture() ? surface : null;
        if (copycat != null) {
            float[] totals = new float[4];
            for (int i = 0; i + 1 < points.size(); i++) {
                var from = points.get(i); var to = points.get(i + 1);
                var lengths = FireHoseDynamicRenderer.surfaceLengths(from.normal(), to.normal(), from.up(), to.up(), from.point(), to.point(), width);
                for (int face = 0; face < 4; face++) totals[face] += lengths[face];
            }
            copycat.beginSurfaceRun(totals);
            java.util.Arrays.fill(phases, 0);
        }
        try {
        for (int i = 0; i + 1 < points.size(); i++) {
            var from = points.get(i); var to = points.get(i + 1);
            FireHoseDynamicRenderer.renderSurfaceSegment(pose, from.normal(), to.normal(), from.up(), to.up(), from.point(), to.point(),
                phases, light, -1, buffer, width, width);
        }
        } finally { if (copycat != null) copycat.beginSurfaceRun(null); }
    }
}
