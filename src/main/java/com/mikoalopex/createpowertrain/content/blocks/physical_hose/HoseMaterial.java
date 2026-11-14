package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import com.simibubi.create.AllBlocks;
import com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** One independently paid-for copycat surface. Never stores a reference to a player's stack. */
public final class HoseMaterial {
    private BlockState state;
    private ItemStack consumed = ItemStack.EMPTY;
    private ResourceLocation beltAppearance;

    // Belt fill has no block model. Its fallback state supplies black debris only.
    public BlockState state() { return isBelt() ? Blocks.BLACK_WOOL.defaultBlockState() : state == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : state; }
    public boolean filled() { return state != null || isBelt(); }
    public boolean isBelt() { return beltAppearance != null; }
    public int emission() { return state == null || isBelt() ? 0 : state.getLightEmission(); }
    public int emission(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return state == null || isBelt() ? 0 : state.getLightEmission(level, pos);
    }
    public ResourceLocation beltAppearance() { return beltAppearance; }
    /** Future dye interactions can select a registered hose appearance without replacing payment. */
    public boolean setBeltAppearance(ResourceLocation appearance) {
        var entry = FireHoseAppearances.all().get(appearance);
        if (!isBelt() || entry == null || !entry.rendersHose()) return false;
        beltAppearance = appearance;
        return true;
    }
    public void fill(BlockState material, ItemStack item) { clear(); state = material; consumed = item.copyWithCount(1); }
    public void fillBelt(ItemStack item) { clear(); beltAppearance = FireHoseAppearances.BLACK; consumed = item.copyWithCount(1); }
    public void moveFrom(HoseMaterial other) {
        if (other == this || filled()) throw new IllegalStateException("Material transfer requires a distinct empty destination");
        state = other.state; beltAppearance = other.beltAppearance; consumed = other.consumed;
        other.clear();
    }
    public ItemStack clear() { var result = consumed; state = null; beltAppearance = null; consumed = ItemStack.EMPTY; return result; }
    public void forgetPayment() { consumed = ItemStack.EMPTY; }
    public boolean cycle() {
        if (state == null) return false;
        // Copycats+ cycles visual properties, not the Cartesian product of redstone internals.
        // Lamps take priority so one click always changes their visible on/off state.
        if (state.hasProperty(BlockStateProperties.LIT)) state = state.cycle(BlockStateProperties.LIT);
        else if (state.hasProperty(com.simibubi.create.content.redstone.RoseQuartzLampBlock.POWERING))
            state = state.cycle(com.simibubi.create.content.redstone.RoseQuartzLampBlock.POWERING);
        else if (state.hasProperty(net.minecraft.world.level.block.TrapDoorBlock.HALF)
                && state.getOptionalValue(BlockStateProperties.OPEN).orElse(false))
            state = state.cycle(net.minecraft.world.level.block.TrapDoorBlock.HALF);
        else if (state.hasProperty(BlockStateProperties.FACING)) state = state.cycle(BlockStateProperties.FACING);
        else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
        else if (state.hasProperty(BlockStateProperties.AXIS)) state = state.cycle(BlockStateProperties.AXIS);
        else if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) state = state.cycle(BlockStateProperties.HORIZONTAL_AXIS);
        else if (state.hasProperty(BlockStateProperties.OPEN)) state = state.cycle(BlockStateProperties.OPEN);
        else return false;
        return true;
    }
    public void read(CompoundTag tag, HolderLookup.Provider registries) {
        beltAppearance = tag.contains("BeltAppearance") ? ResourceLocation.tryParse(tag.getString("BeltAppearance")) : null;
        state = tag.contains("Material") ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("Material")) : null;
        if (state != null && state.isAir()) state = null;
        if (isBelt()) state = null;
        consumed = !filled() ? ItemStack.EMPTY : ItemStack.parseOptional(registries, tag.getCompound("Item"));
    }
    public CompoundTag write(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        if (state != null) tag.put("Material", NbtUtils.writeBlockState(state));
        if (isBelt()) tag.putString("BeltAppearance", beltAppearance.toString());
        if (!consumed.isEmpty()) tag.put("Item", consumed.save(registries));
        return tag;
    }
}
