package com.mikoalopex.createpowertrain.content.blocks.rope_connector_master;

import java.util.ArrayList;
import java.util.List;
import com.mikoalopex.createpowertrain.api.rope.ManagedRopeAdapter;
import net.minecraft.world.item.ItemStack;

/** Registers the required native rope adapter and additional local adapters. */
public final class RopeMasterCompatibility {
    private static final List<ManagedRopeAdapter> ADAPTERS = new ArrayList<>();
    private static boolean initialized;

    private RopeMasterCompatibility() {}

    public static void validateNativeAdapter() { if (!NativeRopeAdapter.supported()) throw new IllegalStateException("Required native rope adapter API unavailable"); }

    public static RopeMasterBlock createBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        return Loaded.block(properties);
    }

    public static synchronized void register(ManagedRopeAdapter adapter) {
        if (ADAPTERS.stream().anyMatch(existing -> existing.id().equals(adapter.id())))
            throw new IllegalArgumentException("Duplicate managed rope adapter: " + adapter.id());
        ADAPTERS.add(adapter);
    }

    public static synchronized List<ManagedRopeAdapter> adapters() {
        if (!initialized) {
            register(Loaded.adapter());
            initialized = true;
        }
        return List.copyOf(ADAPTERS);
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && adapters().stream().anyMatch(adapter -> adapter.accepts(stack));
    }

    private static final class Loaded {
        static RopeMasterBlock block(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
            return new SableRopeMasterBlock(properties);
        }
        static ManagedRopeAdapter adapter() { if (!NativeRopeAdapter.supported()) throw new IllegalStateException("Required native rope adapter API unavailable"); return new NativeRopeAdapter(); }
    }
}
