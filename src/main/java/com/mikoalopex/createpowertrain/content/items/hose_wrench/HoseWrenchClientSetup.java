package com.mikoalopex.createpowertrain.content.items.hose_wrench;

import com.mikoalopex.createpowertrain.CreatePowertrain;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Registers the client mode receiver and the hose-wrench HUD layer. */
@EventBusSubscriber(modid = CreatePowertrain.MODID, value = Dist.CLIENT)
public final class HoseWrenchClientSetup {
    private HoseWrenchClientSetup() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        HoseWrenchNetwork.setClientModeSink(HoseWrenchClientState::acceptServerMode);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            CreatePowertrain.path("hose_wrench"),
            HoseWrenchOverlay.INSTANCE);
    }
}
