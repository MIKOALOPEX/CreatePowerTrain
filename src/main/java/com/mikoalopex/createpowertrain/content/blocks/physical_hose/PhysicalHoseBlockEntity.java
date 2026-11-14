package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import java.util.List;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.FireHoseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Stores physical hose endpoints and bracket bindings independently of the
 * required physics backend.
 */
public class PhysicalHoseBlockEntity extends FireHoseBlockEntity {
    public final HoseMaterial bodyMaterial = new HoseMaterial();
    public final HoseMaterial portMaterial = new HoseMaterial();
    private PhysicalHoseBlockEntity loadedLightPartner() {
        if (level == null || partnerPos == null || !level.hasChunkAt(partnerPos)) return null;
        return getPairedHose() instanceof PhysicalHoseBlockEntity other && other.isCopycat() ? other : null;
    }
    private void updateOwnMaterialLight() {
        if (!isCopycat() || isRemoved() || level == null
                || level instanceof net.createmod.catnip.levelWrappers.SchematicLevel
                || level.getChunkSource() == null || !level.hasChunkAt(worldPosition)) return;
        var other = loadedLightPartner();
        var body = isController() || partnerPos == null ? bodyMaterial : other == null ? null : other.bodyMaterial;
        CopycatHoseLight.update(level.getAuxLightManager(worldPosition), level, worldPosition, body, portMaterial);
    }
    private void updateMaterialLight() {
        if (!isCopycat()) return;
        updateOwnMaterialLight();
        var other = loadedLightPartner();
        if (other != null) other.updateOwnMaterialLight();
    }
    @Override public void notifyUpdate() {
        super.notifyUpdate();
        updateMaterialLight();
    }
    @Override public void onLoad() {
        super.onLoad();
        updateMaterialLight();
    }
    private BlockState removedBodyMaterial;
    public BlockState ropeBreakParticleState() {
        if (!isCopycat()) return getBlockState();
        if (bodyMaterial.filled()) return bodyMaterial.state();
        return removedBodyMaterial != null ? removedBodyMaterial : com.simibubi.create.AllBlocks.COPYCAT_BASE.getDefaultState();
    }
    public boolean isCopycat() { return getBlockState().getBlock() instanceof CopycatPhysicalHoseBlock; }
    /** Tube payment belongs to the connection, not to whichever endpoint survives it. */
    public void refundCopycatBody(BlockPos dropPos, boolean refund) {
        if (!isCopycat() || level == null || level.isClientSide) return;
        if (bodyMaterial.filled()) removedBodyMaterial = bodyMaterial.state();
        var payment = bodyMaterial.clear();
        notifyUpdate();
        if (refund) net.minecraft.world.level.block.Block.popResource(level, dropPos, payment);
    }
    public void releaseCopycatConnection(BlockPos dropPos, boolean refund) {
        if (!isCopycat() || level == null || level.isClientSide) return;
        var other = getPairedHose();
        refundCopycatBody(dropPos, refund);
        if (other instanceof PhysicalHoseBlockEntity hose && hose.isCopycat())
            hose.refundCopycatBody(dropPos, refund);
    }
    public PhysicalHoseBlockEntity bodyMaterialOwner() {
        if (!attached() || isController()) return this;
        return getPairedHose() instanceof PhysicalHoseBlockEntity other && other.isCopycat() && other.isController() ? other : null;
    }
    public void mergeBodyMaterial(PhysicalHoseBlockEntity other) {
        if (!isCopycat()) return;
        removedBodyMaterial = null;
        other.removedBodyMaterial = null;
        if (other.bodyMaterial.filled()) {
            if (!bodyMaterial.filled()) bodyMaterial.moveFrom(other.bodyMaterial);
            else net.minecraft.world.level.block.Block.popResource(level, other.worldPosition, other.bodyMaterial.clear());
        }
        notifyUpdate(); other.notifyUpdate();
    }
    public final java.util.List<PhysicalHoseAnchors.Binding> anchors = new java.util.ArrayList<>();
    public java.util.UUID bracketStrand;
    public double bracketFirstLength = 1;
    protected boolean anchorsDirty;
    public PhysicalHoseAnchors.Snapshot anchorSnapshot() { return null; }
    public boolean rebuildAnchors() { return false; }
    public void releaseAnchor(java.util.UUID node) {
        if (anchors.removeIf(binding -> binding.node().equals(node))) {
            anchorsDirty = !rebuildAnchors(); notifyUpdate();
        }
    }
    public void clearAnchors() {
        var old = java.util.List.copyOf(anchors);
        anchors.clear();
        for (var binding : old) {
            var bracket = binding.resolve(level);
            if (bracket != null) level.destroyBlock(binding.pos(), true);
        }
        notifyUpdate();
    }
    private CompoundTag dormantStrand = new CompoundTag();

    public PhysicalHoseBlockEntity(BlockPos pos, BlockState state) { super(PhysicalHoses.BLOCK_ENTITY.get(), pos, state); }
    public int width() { return getBlockState().getValue(PhysicalHoseBlock.SIZE).width; }
    public void cycleSize() {
        if (level == null || level.isClientSide) return;
        var other = getPairedHose();
        if (attached() && !(other instanceof PhysicalHoseBlockEntity)) return;
        var size = getBlockState().getValue(PhysicalHoseBlock.SIZE).next();
        setSize(size);
        if (other instanceof PhysicalHoseBlockEntity hose) hose.setSize(size);
    }

    public void setSize(PhysicalHoseBlock.Size size) {
        if (level == null || level.isClientSide) return;
        level.setBlock(worldPosition, getBlockState().setValue(PhysicalHoseBlock.SIZE, size), 3);
        notifyUpdate();
    }

    public record EndFrame(Vec3 point, Vec3 normal, Vec3 up) {}
    public EndFrame renderEnd(boolean start) { return null; }
    public boolean connectPhysical(PhysicalHoseBlockEntity target) { return false; }
    public void disconnectPhysical() {}
    public boolean attached() { return dormantStrand.hasUUID("HasRopeAttached"); }
    public List<Vec3> renderPoints(float partial) { return List.of(); }
    protected boolean hasPhysicsBackend() { return false; }
    @Override public boolean allowsHoseConnection() { return false; }
    @Override protected boolean transportEnabled() { return false; }

    @Override protected boolean prepareTransportTick() {
        if (level == null || level.isClientSide) return false;
        if (transportEnabled()) return true;
        updateDisconnectedEndpoint();
        return false;
    }

    @Override public void remove() {
        // Native holder destruction handles both ends; ordinary hose routing is not involved.
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean packet) {
        super.read(tag, registries, packet);
        bodyMaterial.read(tag.getCompound("CopycatBody"), registries);
        portMaterial.read(tag.getCompound("CopycatPort"), registries);
        bracketStrand = tag.hasUUID("BracketStrand") ? tag.getUUID("BracketStrand") : null;
        bracketFirstLength = tag.contains("BracketFirstLength") ? tag.getDouble("BracketFirstLength") : 1;
        anchors.clear();
        for (var entry : tag.getList("PhysicalAnchors", net.minecraft.nbt.Tag.TAG_COMPOUND))
            anchors.add(PhysicalHoseAnchors.Binding.read((CompoundTag) entry));
        if (!hasPhysicsBackend()) {
            dormantStrand = new CompoundTag();
            for (String key : new String[] {"OwnStrand", "HasRopeAttached", "Strand"})
                if (tag.contains(key)) dormantStrand.put(key, tag.get(key).copy());
        }
        if (packet) updateMaterialLight();
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean packet) {
        super.write(tag, registries, packet);
        if (isCopycat()) {
            tag.put("CopycatBody", bodyMaterial.write(registries));
            tag.put("CopycatPort", portMaterial.write(registries));
        }
        if (bracketStrand != null) tag.putUUID("BracketStrand", bracketStrand);
        tag.putDouble("BracketFirstLength", bracketFirstLength);
        var list = new net.minecraft.nbt.ListTag();
        for (var binding : anchors) list.add(binding.write(level));
        tag.put("PhysicalAnchors", list);
        if (!hasPhysicsBackend())
            for (String key : dormantStrand.getAllKeys())
                if (!packet || !key.equals("Strand")) tag.put(key, dormantStrand.get(key).copy());
    }
}
