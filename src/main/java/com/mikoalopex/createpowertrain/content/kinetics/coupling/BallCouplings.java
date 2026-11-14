package com.mikoalopex.createpowertrain.content.kinetics.coupling;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class BallCouplings {
    public static final DeferredBlock<BallCouplingBlock> BASE = CreatePowertrain.BLOCKS.register("ball_coupling_base",
        () -> new BallCouplingBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion().dynamicShape(), false));
    public static final DeferredBlock<BallCouplingBlock> TOP = CreatePowertrain.BLOCKS.register("ball_coupling_top",
        () -> new BallCouplingBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion().dynamicShape(), true));
    public static final DeferredItem<StressCouplingItem> ITEM = CreatePowertrain.ITEMS.register("stress_coupling",
        () -> new StressCouplingItem(BASE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BallCouplingBlockEntity>> BLOCK_ENTITY =
        CreatePowertrain.BLOCK_ENTITY_TYPES.register("ball_coupling",
            () -> BlockEntityType.Builder.of(BallCouplingBlockEntity::create, BASE.get(), TOP.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<BallCouplingMenu>> MENU =
        CreatePowertrain.MENU_TYPES.register("ball_coupling", () -> IMenuTypeExtension.create(BallCouplingMenu::new));

    public static void init() {}
    private BallCouplings() {}
}
