package com.mikoalopex.createpowertrain.content.blocks.rope_latch;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Factories for the required Sable rope physics implementation. */
public final class RopeLatchCompatibility {
    private RopeLatchCompatibility() {}


    public static Block createBlock(BlockBehaviour.Properties properties) {
        return Loaded.block(properties);
    }

    public static BlockEntity createEntity(BlockPos pos, BlockState state) {
        return Loaded.entity(pos, state);
    }

    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        Client.register(event);
    }

    private static final class Client {
        @SuppressWarnings({"unchecked", "rawtypes"})
        static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer((BlockEntityType)
                CreatePowertrain.ROPE_LATCH_BE.get(), RopeLatchRenderer::new);
        }
    }

    private static final class Loaded {
        static Block block(BlockBehaviour.Properties properties) { return new RopeLatchBlock(properties); }
        static BlockEntity entity(BlockPos pos, BlockState state) {
            return new RopeLatchBlockEntity(CreatePowertrain.ROPE_LATCH_BE.get(), pos, state);
        }
    }

}
