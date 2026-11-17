package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import com.mikoalopex.createpowertrain.integration.simulated.SimulatedPhysicalHoseBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class CopycatPhysicalHoseBlock extends SimulatedPhysicalHoseBlock {
    public CopycatPhysicalHoseBlock(Properties properties) { super(properties, 8); }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public boolean hasDynamicLightEmission(BlockState state) { return true; }
    @Override public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        // The light engine can query off-thread: read the thread-safe manager, never a block entity.
        var manager = level.getAuxLightManager(pos);
        return manager == null ? 0 : manager.getLightAt(pos);
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return PhysicalHoses.COPYCAT_ITEM.get().getDefaultInstance(); }

    // Fill the shared tube first, then this endpoint's port, regardless of hit position.
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) return ItemInteractionResult.FAIL;
        if (stack.getItem() instanceof PhysicalHoseItem || stack.is(Items.SHEARS))
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        boolean belt = stack.getItem() instanceof com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
        BlockState material = belt ? null : ((CopycatBlock) AllBlocks.COPYCAT_PANEL.get()).getAcceptedBlockState(level, pos, stack, hit.getDirection());
        if ((!belt && material == null) || !(level.getBlockEntity(pos) instanceof PhysicalHoseBlockEntity hose))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var owner = hose.bodyMaterialOwner();
        if (owner == null) return ItemInteractionResult.FAIL;
        if (belt) {
            // A belt fills only the shared tube body, never a port.
            if (!owner.bodyMaterial.filled()) fill(owner, owner.bodyMaterial, null, stack, player, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
        if (!owner.bodyMaterial.filled()) {
            fill(owner, owner.bodyMaterial, material, stack, player, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
        if (!hose.portMaterial.filled()) {
            fill(hose, hose.portMaterial, material, stack, player, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
        // Both slots are paid for: cycle the matching material, tube first.
        if (matches(owner.bodyMaterial, material)) {
            if (!level.isClientSide && owner.bodyMaterial.cycle()) owner.notifyUpdate();
            return ItemInteractionResult.SUCCESS;
        }
        if (matches(hose.portMaterial, material) && !level.isClientSide && hose.portMaterial.cycle())
            hose.notifyUpdate();
        return ItemInteractionResult.SUCCESS;
    }

    private static boolean matches(HoseMaterial slot, BlockState material) {
        return !slot.isBelt() && slot.filled() && slot.state().is(material.getBlock());
    }

    private static void fill(PhysicalHoseBlockEntity target, HoseMaterial slot, BlockState material,
            ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) return;
        if (material == null) slot.fillBelt(stack);
        else slot.fill(material, stack);
        if (!player.isCreative()) stack.shrink(1);
        target.notifyUpdate();
        level.playSound(null, pos, slot.state().getSoundType().getPlaceSound(),
            net.minecraft.sounds.SoundSource.BLOCKS, 1, 0.8f);
    }

    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PhysicalHoseBlockEntity hose) {
            // Capture the pair before native rope destruction clears either endpoint's link.
            hose.releaseCopycatConnection(pos, !player.isCreative());
            if (player.isCreative()) hose.portMaterial.forgetPayment();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide && !moving && level.getBlockEntity(pos) instanceof PhysicalHoseBlockEntity hose) {
            hose.releaseCopycatConnection(pos, true);
            popResource(level, pos, hose.portMaterial.clear());
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
