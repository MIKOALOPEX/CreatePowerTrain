package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import com.mikoalopex.createpowertrain.integration.simulated.SimulatedPhysicalHoseBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CopycatPhysicalHoseBlock extends SimulatedPhysicalHoseBlock {
    public CopycatPhysicalHoseBlock(Properties properties) { super(properties, 8); }

    // The centre of the outward face addresses the tube; the rim and side faces address the port.

    private static boolean body(BlockState state, BlockPos pos, Vec3 hit, Direction face) {
        return PhysicalHoseGeometry.targetsCopycatBody(state.getValue(FACING), state.getValue(SIZE).width, hit.subtract(Vec3.atLowerCornerOf(pos)), face);
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public boolean hasDynamicLightEmission(BlockState state) { return true; }
    @Override public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        // The light engine can query off-thread: read the thread-safe manager, never a block entity.
        var manager = level.getAuxLightManager(pos);
        return manager == null ? 0 : manager.getLightAt(pos);
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return PhysicalHoses.COPYCAT_ITEM.get().getDefaultInstance(); }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) return ItemInteractionResult.FAIL;
        if (stack.getItem() instanceof PhysicalHoseItem || stack.is(net.minecraft.world.item.Items.SHEARS))
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        boolean body = body(state, pos, hit.getLocation(), hit.getDirection());
        boolean belt = stack.getItem() instanceof com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
        // Consume the interaction on a port so the belt item cannot start its own placement there.
        if (belt && !body) return ItemInteractionResult.FAIL;
        BlockState material = belt ? null : ((CopycatBlock) AllBlocks.COPYCAT_PANEL.get()).getAcceptedBlockState(level, pos, stack, hit.getDirection());
        if ((!belt && material == null) || !(level.getBlockEntity(pos) instanceof PhysicalHoseBlockEntity hose))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var owner = body ? hose.bodyMaterialOwner() : hose;
        if (owner == null) return ItemInteractionResult.FAIL;
        var slot = body ? owner.bodyMaterial : owner.portMaterial;
        if (slot.filled()) {
            if (belt || slot.isBelt()) return ItemInteractionResult.SUCCESS;
            if (!slot.state().is(material.getBlock())) return ItemInteractionResult.SUCCESS;
            if (!level.isClientSide && slot.cycle()) owner.notifyUpdate();
            return ItemInteractionResult.SUCCESS;
        }
        if (!level.isClientSide) {
            if (belt) slot.fillBelt(stack);
            else slot.fill(material, stack);
            if (!player.isCreative()) stack.shrink(1);
            owner.notifyUpdate();
            level.playSound(null, pos, slot.state().getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 0.8f);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.mayBuild()) return InteractionResult.FAIL;
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof PhysicalHoseBlockEntity hose)) return InteractionResult.PASS;
        boolean body = body(state, context.getClickedPos(), context.getClickLocation(), context.getClickedFace());
        var owner = body ? hose.bodyMaterialOwner() : hose;
        if (owner == null) return InteractionResult.FAIL;
        var slot = body ? owner.bodyMaterial : owner.portMaterial;
        if (!slot.filled()) return super.onWrenched(state, context);
        if (!context.getLevel().isClientSide) {
            var refund = slot.clear();
            if (!player.isCreative()) player.getInventory().placeItemBackInInventory(refund);
            owner.notifyUpdate();
        }
        return InteractionResult.SUCCESS;
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
