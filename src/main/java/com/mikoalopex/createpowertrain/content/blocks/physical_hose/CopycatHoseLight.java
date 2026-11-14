package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/** Copycats+ uses this native light path for mutable, multipart material data. */
final class CopycatHoseLight {
    private CopycatHoseLight() {}

    static void update(AuxiliaryLightManager manager, BlockGetter level, BlockPos pos,
                       HoseMaterial body, HoseMaterial port) {
        if (manager == null) return;
        int bodyLight = body == null ? 0 : body.emission(level, pos);
        manager.setLightAt(pos, Math.clamp(Math.max(bodyLight, port.emission(level, pos)), 0, 15));
    }
}
