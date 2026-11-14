package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Registers physical hose endpoints independently of the required physics backend.
 */
public final class PhysicalHoses {
    public static final DeferredBlock<PhysicalHoseBlock> BLOCK = block("physical_fire_hose", 8);
    public static final DeferredBlock<CopycatPhysicalHoseBlock> COPYCAT = CreatePowertrain.BLOCKS.register("copycat_physical_hose",
        () -> new CopycatPhysicalHoseBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredItem<PhysicalHoseItem> COPYCAT_ITEM = CreatePowertrain.ITEMS.register("copycat_physical_hose",
        () -> new PhysicalHoseItem(COPYCAT.get(), new Item.Properties()));
    public static final DeferredItem<PhysicalHoseItem> ITEM = CreatePowertrain.ITEMS.register("physical_fire_hose",
        () -> new PhysicalHoseItem(BLOCK.get(), new Item.Properties()));
    // Preserve legacy size-specific IDs for saved worlds; expose only the adjustable item.
    public static final DeferredBlock<PhysicalHoseBlock> SMALL = block(4), MEDIUM = block(6), LARGE = block(8);
    public static final DeferredItem<PhysicalHoseItem> SMALL_ITEM = item(SMALL, 4), MEDIUM_ITEM = item(MEDIUM, 6), LARGE_ITEM = item(LARGE, 8);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PhysicalHoseBlockEntity>> BLOCK_ENTITY =
        CreatePowertrain.BLOCK_ENTITY_TYPES.register("physical_fire_hose", () -> BlockEntityType.Builder
            .of(PhysicalHoses::create, BLOCK.get(), COPYCAT.get(), SMALL.get(), MEDIUM.get(), LARGE.get()).build(null));

    private static DeferredBlock<PhysicalHoseBlock> block(int width) {
        return block("physical_fire_hose_" + width, width);
    }

    private static DeferredBlock<PhysicalHoseBlock> block(String name, int width) {
        return CreatePowertrain.BLOCKS.register(name, () -> new com.mikoalopex.createpowertrain.integration.simulated.SimulatedPhysicalHoseBlock(
            BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion(), width));
    }

    private static DeferredItem<PhysicalHoseItem> item(DeferredBlock<PhysicalHoseBlock> block, int width) {
        return CreatePowertrain.ITEMS.register("physical_fire_hose_" + width,
            () -> new PhysicalHoseItem(block.get(), new Item.Properties()));
    }

    public static void init() {}

    

    private static PhysicalHoseBlockEntity create(BlockPos pos, BlockState state) {
        com.mikoalopex.createpowertrain.compatibility.RequiredPhysics.initialize();
        return new com.mikoalopex.createpowertrain.integration.simulated.SimulatedPhysicalHoseBlockEntity(pos, state);
    }

    private PhysicalHoses() {}
}
