package com.mikoalopex.createpowertrain.content.kinetics.rocker;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.simibubi.create.AllItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid=CreatePowertrain.MODID)
public record RockerUsePacket() implements CustomPacketPayload {
    public static final Type<RockerUsePacket> TYPE = new Type<>(CreatePowertrain.path("rocker_use"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RockerUsePacket> CODEC = StreamCodec.unit(new RockerUsePacket());
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(CreatePowertrain.MODID).playToServer(TYPE,CODEC,(packet,context)->context.enqueueWork(()->{
            var player=context.player();
            if(player.isSpectator() || player.getMainHandItem().is(AllItems.WRENCH.get())) return;
            var hit=RockerTargets.pick(player);
            if(hit != null && !hit.be().dial(hit.local())) hit.be().use(player);
        }));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
