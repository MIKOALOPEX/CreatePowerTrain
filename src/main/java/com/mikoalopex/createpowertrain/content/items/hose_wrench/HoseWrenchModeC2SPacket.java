package com.mikoalopex.createpowertrain.content.items.hose_wrench;

import com.mikoalopex.createpowertrain.CreatePowertrain;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-selected mode, sent whenever the toolbar first appears or changes. */
public record HoseWrenchModeC2SPacket(int mode) implements CustomPacketPayload {
    public static final Type<HoseWrenchModeC2SPacket> TYPE =
        new Type<>(CreatePowertrain.path("hose_wrench_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HoseWrenchModeC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HoseWrenchModeC2SPacket::mode,
            HoseWrenchModeC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
