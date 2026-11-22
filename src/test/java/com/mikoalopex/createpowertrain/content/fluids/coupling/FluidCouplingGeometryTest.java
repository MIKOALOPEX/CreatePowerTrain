package com.mikoalopex.createpowertrain.content.fluids.coupling;

import net.minecraft.core.Direction;
import com.mikoalopex.createpowertrain.integration.synaxis.CouplingPhysics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Verifies six-face geometry without loading a world or an optional physics backend. */
public final class FluidCouplingGeometryTest {
    private static int checks;

    public static void main(String[] args) {
        check(FluidCouplingGeometry.pitchToward(new Vec3(0, 1, 1)) == 45, "Aim toward target position");
        check(FluidCouplingGeometry.pitchToward(new Vec3(0, 0, 1)) == 45, "Unreachable open-side target clamps");
        check(FluidCouplingGeometry.pitchToward(new Vec3(0, 0, -1)) == -45, "Unreachable rim-side target clamps");
        check(FluidCouplingGeometry.pitchToward(new Vec3(9, 1, 1)) == 45, "Off-plane target still tracks pitch");
        check(FluidCouplingGeometry.pitchToward(Vec3.ZERO) == 0, "Coincident target stays finite");
        check(FluidCouplingIndex.inSearchRange(FluidCouplingInterfaces.RECEPTACLE, Vec3.ZERO, new Vec3(3, 0, 0)), "Three block pitch search");
        check(!FluidCouplingIndex.inSearchRange(FluidCouplingInterfaces.RECEPTACLE, Vec3.ZERO, new Vec3(3.01, 0, 0)), "Pitch search boundary");
        check(!FluidCouplingIndex.inSearchRange(FluidCouplingInterfaces.RECEPTACLE, Vec3.ZERO, new Vec3(3, 3, 0)), "Spherical rather than cubic search");
        check(!FluidCouplingIndex.inSearchRange(FluidCouplingInterfaces.FREE, Vec3.ZERO, new Vec3(2, 0, 0)), "Free search unchanged");
        check(FluidCouplingIndex.inside(Vec3.ZERO, new Vec3(0.75, 0.75, 0.75), 0.75), "Forced box boundary");
        check(!FluidCouplingIndex.inside(Vec3.ZERO, new Vec3(0.751, 0, 0), 0.75), "Forced box unchanged");
        for (Direction facing : Direction.values()) {
            Vec3 normal = FluidCouplingGeometry.orient(new Vec3(0, 1, 0), facing);
            close(normal, Vec3.atLowerCornerOf(facing.getNormal()), "Mount normal");
            Vec3 point = new Vec3(0.3, -0.7, 2.1);
            close(FluidCouplingGeometry.unorient(FluidCouplingGeometry.orient(point, facing), facing), point, "Inverse frame");
            for (int i = 0; i < 5; i++) {
                close(FluidCouplingGeometry.modelPoint(FluidCouplingGeometry.freeAnchor(i), facing),
                    new Vec3(0.5, 0.5, 0.5).add(normal.scale(i / 4.0)), "Free height");
            }
            for (int step = -90; step <= 90; step++) {
                double pitch = step / 2.0;
                Vec3 axis = FluidCouplingGeometry.receptacleAxis(pitch);
                Vec3 mouth = FluidCouplingGeometry.pitch(FluidCouplingGeometry.RECEPTACLE_MOUTH, pitch);
                Vec3 contact = FluidCouplingGeometry.receptacleContact(pitch);
                close(mouth.subtract(contact), axis.scale(7), "Seven pixel insertion");
                Vec3 pivot = FluidCouplingGeometry.PITCH_PIVOT;
                close(contact.subtract(pivot), axis.scale(2), "Contact follows fixed physical pivot");
                Vec3 orientedContact = FluidCouplingGeometry.modelPoint(contact, facing);
                Vec3 orientedMouth = FluidCouplingGeometry.modelPoint(mouth, facing);
                close(orientedMouth.subtract(orientedContact),
                    FluidCouplingGeometry.orient(axis, facing).scale(7.0 / 16), "Rotated insertion");
                check(Math.abs(FluidCouplingGeometry.trackingPitch(axis.scale(-1)) - pitch) < 1e-8, "Tracking sign");
            }
        }
        check(FluidCouplingGeometry.clampPitch(90) == 45, "Upper limit");
        check(FluidCouplingGeometry.clampPitch(-90) == -45, "Rim-side lower limit");
        check(Math.abs(Math.toDegrees(Math.asin(FluidCouplingGeometry.receptacleAxis(-90).y)) - 45) < 1e-8,
            "Rim-side minimum angle above base plane");
        check(Math.abs(Math.toDegrees(Math.asin(FluidCouplingGeometry.receptacleAxis(45).y)) - 45) < 1e-8,
            "Minimum angle above base plane");
        check(FluidCouplingGeometry.clampPitch(Double.NaN) == 0, "Finite animation");
        check(FluidCouplingGeometry.trackingPitch(new Vec3(1, 0, 0)) == 0, "Out-of-plane target");
        close(FluidCouplingGeometry.nozzleJointPivot().subtract(FluidCouplingGeometry.nozzleTip()),
            new Vec3(0, 0, 2), "Nozzle pivot offset");
        for (var a : FluidCouplingInterfaces.values()) for (var b : FluidCouplingInterfaces.values()) {
            check(a.accepts(b) == b.accepts(a), "Symmetric compatibility");
            check(a.accepts(b) == (a == FluidCouplingInterfaces.FREE && b == a
                || a.hardBoom() && b.hardBoom() && a != b
                || a.hose() && b.hose() && a != b), "Pair policy excludes cross-family and same-role pairs");
        }
        for (int yaw = -360; yaw <= 360; yaw += 5) {
            for (int tilt = 0; tilt <= 45; tilt += 5) {
                Vec3 axis = FluidCouplingGeometry.yawPitch(FluidCouplingGeometry.PITCH_PIVOT.add(0, 1, 0), tilt, yaw)
                    .subtract(FluidCouplingGeometry.PITCH_PIVOT);
                check(Math.abs(axis.length() - 1) < 1e-8, "Yaw preserves radius");
                check(Math.abs(FluidCouplingGeometry.tiltToward(axis) - tilt) < 1e-8, "Combined tilt tracking");
                if (tilt > 0) check(Math.abs(FluidCouplingGeometry.wrapYaw(
                    FluidCouplingGeometry.yawToward(axis, 0) - yaw)) < 1e-8, "Full heading tracking");
                Vec3 mouth = FluidCouplingGeometry.yawPitch(FluidCouplingGeometry.RECEPTACLE_MOUTH, tilt, yaw);
                close(mouth.subtract(FluidCouplingGeometry.receptacleContact(tilt, yaw)), axis.scale(7), "Yaw insertion depth");
                for (Direction facing : Direction.values()) close(
                    FluidCouplingGeometry.unorient(FluidCouplingGeometry.orient(axis, facing), facing), axis, "Yaw mount frame");
            }
        }
        check(FluidCouplingGeometry.wrapYaw(-179 - 179) == 2, "Short interpolation across wrap");
        check(FluidCouplingGeometry.yawToward(new Vec3(0, 1, 0), 123) == 123, "Stable heading at pole");
        check(FluidCouplingGeometry.wrapYaw(Double.NaN) == 0, "Invalid saved yaw");
        for (Direction facing : Direction.values()) {
            Vec3 mx = FluidCouplingGeometry.orient(new Vec3(1, 0, 0), facing);
            Vec3 my = FluidCouplingGeometry.orient(new Vec3(0, 1, 0), facing);
            var mount = com.mikoalopex.createpowertrain.content.kinetics.coupling.CouplingJointKinematics.frame(
                new org.joml.Vector3d(mx.x, mx.y, mx.z), new org.joml.Vector3d(my.x, my.y, my.z));
            for (int yaw = -180; yaw <= 180; yaw += 15) for (int pitch = -45; pitch <= 45; pitch += 15) {
                var expected = new org.joml.Quaterniond().rotationY(Math.toRadians(yaw)).rotateX(Math.toRadians(pitch));
                var worldMount = new org.joml.Quaterniond().rotationXYZ(0.3, -0.8, 1.1).mul(mount);
                var socketWorld = new org.joml.Quaterniond(worldMount).mul(expected);
                var nozzleX = socketWorld.transform(new org.joml.Vector3d(1, 0, 0));
                var nozzleNormal = socketWorld.transform(new org.joml.Vector3d(0, -1, 0));
                // Rotate the active structure while holding the nozzle fixed in world space.
                for (double turn : new double[] {0, 0.7, 2.4}) {
                    var movingMount = new org.joml.Quaterniond().rotationY(turn).mul(worldMount);
                    var actual = FluidCouplingGeometry.dockedRotation(
                        movingMount.transform(new org.joml.Vector3d(1, 0, 0)),
                        movingMount.transform(new org.joml.Vector3d(0, 1, 0)), nozzleX, nozzleNormal);
                    var actualWorld = new org.joml.Quaterniond(movingMount).mul(actual);
                    for (var axis : new org.joml.Vector3d[] {new org.joml.Vector3d(1, 0, 0),
                            new org.joml.Vector3d(0, 1, 0), new org.joml.Vector3d(0, 0, 1)})
                        check(actualWorld.transform(new org.joml.Vector3d(axis)).distance(
                            socketWorld.transform(new org.joml.Vector3d(axis))) < 1e-8, "Docked faces cannot rotate relative to nozzle");
                    Vec3 contact = FluidCouplingGeometry.receptacleContact(actual).subtract(FluidCouplingGeometry.PITCH_PIVOT);
                    var worldOffset = movingMount.transform(new org.joml.Vector3d(contact.x, contact.y, contact.z));
                    check(worldOffset.distance(new org.joml.Vector3d(nozzleNormal).mul(-2)) < 1e-8,
                        "Docked insertion point follows the complete pose");
                }
            }
        }
        for (int heading = -180; heading <= 180; heading += 15) {
            for (int angle = -45; angle <= 45; angle++) {
                Vec3 target = FluidCouplingGeometry.yawPitch(FluidCouplingGeometry.PITCH_PIVOT.add(0, 1, 0), angle, heading)
                    .subtract(FluidCouplingGeometry.PITCH_PIVOT);
                var aim = FluidCouplingGeometry.aimToward(target, heading);
                check(Math.abs(FluidCouplingGeometry.wrapYaw(aim.yaw() - heading)) < 1e-8,
                    "Crossing the center axis must not flip yaw");
                check(Math.abs(aim.pitch() - angle) < 1e-8, "Signed pitch spans minus to plus 45 degrees");
            }
        }
        var settings = new FluidCouplingSettings(FluidCouplingInterfaces.FREE, 4, 2, -88, 103, true);
        var receptacle = settings.withInterface(FluidCouplingInterfaces.RECEPTACLE);
        var nozzle = settings.withInterface(FluidCouplingInterfaces.BOOM_NOZZLE);
        for (Direction facing : Direction.values()) {
            var a = FluidCouplingJoint.endpoint(Level.OVERWORLD, null, BlockPos.ZERO, facing, receptacle, v -> v);
            var b = FluidCouplingJoint.endpoint(Level.OVERWORLD, java.util.UUID.randomUUID(), BlockPos.ZERO,
                facing.getOpposite(), nozzle, v -> v);
            var request = FluidCouplingJoint.request(java.util.UUID.randomUUID(), receptacle, nozzle, a, b);
            check(request.alignHingeTangents() && request.mode() == CouplingPhysics.FREE_YAW_LIMITED_PITCH
                && request.lowerDegrees() == -45 && request.upperDegrees() == 45, "Fixed hard-boom request");
            check(Math.abs(a.normal().dot(a.tangent())) < 1e-8, "Orthogonal physical frame");
            Vec3 expected = FluidCouplingGeometry.modelPoint(FluidCouplingGeometry.PITCH_PIVOT, facing);
            close(new Vec3(a.anchor().x(), a.anchor().y(), a.anchor().z()), expected, "Fixed constraint pivot");
        }
        for (var type : FluidCouplingInterfaces.values()) {
            var changed = settings.withInterface(type);
            check(FluidCouplingSettings.load(changed.save()).equals(changed), "Settings round trip");
            check(changed.withInterface(FluidCouplingInterfaces.FREE).equals(settings), "Retain free settings");
            if (type.hardBoom()) {
                var rule = changed.constraint();
                check(rule.mode() == CouplingPhysics.FREE_YAW_LIMITED_PITCH && rule.lower() == -45 && rule.upper() == 45
                    && !rule.flipRange() && rule.modelZero(), "Hard-boom constraint cannot be overridden");
            }
            if (type.hose()) {
                var rule = changed.constraint();
                check(rule.mode() == 2 && rule.lower() == 0 && rule.upper() == 0
                    && !rule.flipRange() && !rule.modelZero(), "Hose always uses fixed joint, ignoring free hinge settings");
            }
        }
        var drogue = settings.withInterface(FluidCouplingInterfaces.HOSE_DROGUE);
        var probe = settings.withInterface(FluidCouplingInterfaces.HOSE_PROBE);
        check(drogue.type().activation() == FluidCouplingInterface.Activation.ACTIVE, "Drogue owns pairing");
        check(probe.type().activation() == FluidCouplingInterface.Activation.PASSIVE, "Probe is passive");
        for (Direction facing : Direction.values()) {
            var a = FluidCouplingJoint.endpoint(Level.OVERWORLD, null, BlockPos.ZERO, facing, drogue, v -> v);
            var b = FluidCouplingJoint.endpoint(Level.OVERWORLD, java.util.UUID.randomUUID(), BlockPos.ZERO,
                facing.getOpposite(), probe, v -> v);
            var request = FluidCouplingJoint.request(java.util.UUID.randomUUID(), drogue, probe, a, b);
            check(request.mode() == 2 && !request.alignHingeTangents(), "Fixed hose joint request");
            check(a.normal().dot(b.normal()) < -0.999999, "Hose mates along facing, not hard-boom pitch axis");
            close(new Vec3(a.anchor().x(), a.anchor().y(), a.anchor().z()),
                FluidCouplingGeometry.modelPoint(FluidCouplingGeometry.hoseToMount(new Vec3(7, 8, 8)), facing), "Drogue socket pivot");
            close(new Vec3(b.anchor().x(), b.anchor().y(), b.anchor().z()),
                FluidCouplingGeometry.modelPoint(FluidCouplingGeometry.hoseToMount(new Vec3(-6, 8, 8)), facing.getOpposite()), "Probe insertion pivot");
        }
        float deployed = 0;
        for (int tick = 0; tick < HoseDeployment.DURATION_TICKS; tick++) {
            check(!HoseDeployment.ready(deployed), "No pairing during partial deployment");
            deployed = HoseDeployment.step(deployed, true);
        }
        check(HoseDeployment.ready(deployed), "Pairing allowed after deployment");
        for (int tick = 0; tick < HoseDeployment.DURATION_TICKS; tick++) deployed = HoseDeployment.step(deployed, false);
        check(deployed == 0, "Fully stowed after redstone removed");
        float halfway = 0;
        for (int tick = 0; tick < 10; tick++) halfway = HoseDeployment.step(halfway, true);
        float reversed = HoseDeployment.step(halfway, false);
        check(reversed > 0 && reversed < halfway && Math.abs(halfway - reversed - 0.05f) < 1e-6,
            "Mid-animation reversal is continuous");
        check(HoseDeployment.clamp(Float.NaN) == 0 && HoseDeployment.clamp(7) == 1, "Persisted deployment sanitization");
        check(HoseDrogueParts.PARTS.size() == 28, "All moving source elements represented");
        check(HoseDrogueParts.PARTS.stream().filter(p -> p.angle() != 0).count() == 12, "Twelve net panels rotate");
        check(HoseDrogueParts.PARTS.stream().filter(p -> p.angle() == 0).count() == 16, "Sixteen ring pieces translate only");
        var ring = FluidCouplingGeometry.ring(Vec3.ZERO, new Vec3(0, 2, 0),
            new Vec3(0, 1, 0), new Vec3(0, -1, 0));
        close(ring.firstFace().add(ring.secondFace()).scale(0.5), ring.center(), "Shared ring center");
        check(Math.abs(ring.firstFace().distanceTo(ring.secondFace()) - 2.0 / 16) < 1e-10, "Ring thickness");
        var degenerate = FluidCouplingGeometry.ring(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO);
        check(Double.isFinite(degenerate.axis().length()) && degenerate.axis().lengthSqr() > 0.99, "Finite ring frame");
        System.out.println("FluidCouplingGeometry: " + checks + " geometry and policy checks passed");
    }

    private static void close(Vec3 actual, Vec3 expected, String message) {
        check(actual.distanceTo(expected) < 1e-8, message);
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
