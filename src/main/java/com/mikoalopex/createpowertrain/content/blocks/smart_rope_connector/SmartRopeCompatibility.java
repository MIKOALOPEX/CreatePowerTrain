package com.mikoalopex.createpowertrain.content.blocks.smart_rope_connector;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Factories and client hooks for the required native rope implementation. */
public final class SmartRopeCompatibility {
    private SmartRopeCompatibility() {}


    public static void clientTick() {
        SmartRopeSelection.tick();
    }

    public static boolean scroll(double delta) {
        return SmartRopeSelection.scroll(delta);
    }

    public static boolean use() {
        return SmartRopeSelection.use();
    }

    public static boolean selecting() {
        return SmartRopeSelection.hasTarget();
    }

    public static void cut(net.minecraft.server.level.ServerPlayer player, SmartRopeCutPacket packet) {
        Loaded.cut(player, packet);
    }

    public static Block createBlock(BlockBehaviour.Properties properties) {
        return Loaded.createBlock(properties);
    }

    public static BlockEntity createEntity(BlockPos pos, BlockState state) {
        return Loaded.createEntity(pos, state);
    }

    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        Client.register(event);
    }

    private static final class Loaded {
        static void cut(net.minecraft.server.level.ServerPlayer player, SmartRopeCutPacket packet) {
            var level = player.serverLevel();
            if (!level.dimension().location().equals(packet.dimension()) || player.isSpectator()
                || !player.getAbilities().mayBuild || !level.isLoaded(packet.pos())
                || !level.mayInteract(player, packet.pos())
                || !player.getItemInHand(packet.hand()).is(dev.simulated_team.simulated.index.SimTags.Items.DESTROYS_ROPE))
                return;
            if (!(level.getBlockEntity(packet.pos()) instanceof SmartRopeConnectorBlockEntity connector)) return;
            var center = com.mikoalopex.createpowertrain.integration.sable.SableStructureCompat
                .transformPositionToWorld(connector, packet.pos().getCenter());
            double range = player.blockInteractionRange() + 1.0;
            if (player.getEyePosition().distanceToSqr(center) > range * range) return;
            connector.cutConnection(packet.rope(), player);
        }

        static Block createBlock(BlockBehaviour.Properties properties) {
            return new SmartRopeConnectorBlock(properties);
        }

        static BlockEntity createEntity(BlockPos pos, BlockState state) {
            return new SmartRopeConnectorBlockEntity(CreatePowertrain.SMART_ROPE_CONNECTOR_BE.get(), pos, state);
        }
    }

    private static final class Client {
        @SuppressWarnings({"unchecked", "rawtypes"})
        static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer((BlockEntityType)
                CreatePowertrain.SMART_ROPE_CONNECTOR_BE.get(), context -> new SmartRopeConnectorRenderer(context));
        }
    }
}
