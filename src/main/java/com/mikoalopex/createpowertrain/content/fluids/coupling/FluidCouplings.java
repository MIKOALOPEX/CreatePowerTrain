package com.mikoalopex.createpowertrain.content.fluids.coupling;

import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.mikoalopex.createpowertrain.content.kinetics.coupling.BallCouplingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreatePowertrain.MODID)
public final class FluidCouplings {
    @net.neoforged.bus.api.SubscribeEvent
    public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        FluidCouplingIndex.clear();
    }
    public static final DeferredBlock<FluidCouplingBlock> BLOCK = CreatePowertrain.BLOCKS.register("fluid_coupling",
        () -> new FluidCouplingBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion().dynamicShape()));
    public static final DeferredItem<BlockItem> ITEM = CreatePowertrain.ITEMS.register("fluid_coupling",
        () -> new com.mikoalopex.createpowertrain.compatibility.CompatibleBlockItem(BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidCouplingBlockEntity>> BLOCK_ENTITY =
        CreatePowertrain.BLOCK_ENTITY_TYPES.register("fluid_coupling",
            () -> BlockEntityType.Builder.of(FluidCouplings::create, BLOCK.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<BallCouplingMenu>> MENU = CreatePowertrain.MENU_TYPES.register(
        "fluid_coupling", () -> IMenuTypeExtension.create((id, inventory, buffer) -> new BallCouplingMenu(id, inventory, buffer, true)));
    public static void init() {}

    

    private static FluidCouplingBlockEntity create(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        com.mikoalopex.createpowertrain.compatibility.RequiredPhysics.initialize();
        return new com.mikoalopex.createpowertrain.integration.sable.SableFluidCouplingBlockEntity(pos, state);
    }
    private FluidCouplings() {}
}
