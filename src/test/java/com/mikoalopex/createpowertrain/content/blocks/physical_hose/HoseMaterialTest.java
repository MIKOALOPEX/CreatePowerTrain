package com.mikoalopex.createpowertrain.content.blocks.physical_hose;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

/** Registry-only serialization and item accounting checks; no client, server or save is opened. */
public final class HoseMaterialTest {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        Bootstrap.bootStrap();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var lamp = new HoseMaterial();
        lamp.fill(Blocks.REDSTONE_LAMP.defaultBlockState(), new ItemStack(Items.REDSTONE_LAMP, 16));
        check(lamp.emission() == 0, "Unlit lamp must not glow");
        for (int i = 0; i < 7; i++) {
            check(lamp.cycle(), "Lamp state must cycle");
            check(lamp.state().getValue(BlockStateProperties.LIT) == (i % 2 == 0), "Lamp on/off must alternate");
            check(lamp.emission() == (i % 2 == 0 ? 15 : 0), "Glow follows lamp state");
        }
        var restoredLamp = new HoseMaterial();
        restoredLamp.read(lamp.write(registries), registries);
        check(restoredLamp.state().getValue(BlockStateProperties.LIT), "Lit texture state survives reload");
        check(restoredLamp.emission() == 15, "Emission survives reload");
        check(restoredLamp.clear().getCount() == 1, "Repeated state changes still refund one item");
        var furnace = new HoseMaterial();
        var initial = Blocks.FURNACE.defaultBlockState();
        furnace.fill(initial, new ItemStack(Items.FURNACE));
        var visited = new java.util.HashSet<net.minecraft.world.level.block.state.BlockState>();
        for (int i = 0; i < 2; i++) { visited.add(furnace.state()); furnace.cycle(); }
        check(visited.size() == 2 && furnace.state() == initial, "Lit state toggles directly without rotating furnace");
        check(furnace.clear().getCount() == 1, "Multi-property cycling consumes only initial payment");
        var body = new HoseMaterial();
        var port = new HoseMaterial();
        var stack = new ItemStack(Items.OAK_LOG, 32);
        body.fill(Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z), stack);
        port.fill(Blocks.STONE.defaultBlockState(), new ItemStack(Items.STONE, 16));
        stack.shrink(5);
        check(body.cycle(), "Logs can rotate");
        var savedBody = body.write(registries);
        var savedPort = port.write(registries);
        var reloaded = new HoseMaterial();
        reloaded.read(savedBody, registries);
        check(reloaded.state() == body.state(), "Material orientation survives serialization");
        var payment = reloaded.clear();
        check(payment.is(Items.OAK_LOG) && payment.getCount() == 1, "Exactly one paid item is recovered");
        check(stack.getCount() == 27, "Stored payment does not alias the player's stack");
        check(reloaded.clear().isEmpty(), "Repeated removal cannot duplicate items");
        var disconnected = reloaded.write(registries);
        reloaded.read(disconnected, registries);
        check(!reloaded.filled() && reloaded.clear().isEmpty(), "Disconnect survives reload without resurrecting material or payment");
        reloaded.read(savedPort, registries);
        check(reloaded.state().is(Blocks.STONE) && body.state().is(Blocks.OAK_LOG), "Body and port remain independent");
        check(!reloaded.cycle(), "Stone has no orientation");
        reloaded.forgetPayment();
        check(reloaded.filled() && reloaded.clear().isEmpty(), "Creative destruction does not return a material");
        reloaded.read(new net.minecraft.nbt.CompoundTag(), registries);
        check(!reloaded.filled(), "Older endpoints load with empty copycat slots");
        checkBelt(registries);
        checkRoseQuartzLamp();
        checkNativeLighting();
        System.out.println("Copycat hose material persistence and accounting passed");
    }
    private static void checkRoseQuartzLamp() {
        // Use Create's exact three properties without registering a new block in the frozen vanilla test registry.
        var definition = new net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState>(Blocks.STONE)
            .add(com.simibubi.create.content.redstone.RoseQuartzLampBlock.POWERED,
                com.simibubi.create.content.redstone.RoseQuartzLampBlock.POWERING,
                com.simibubi.create.content.redstone.RoseQuartzLampBlock.ACTIVATE)
            .create(net.minecraft.world.level.block.Block::defaultBlockState, net.minecraft.world.level.block.state.BlockState::new);
        for (var initial : definition.getPossibleStates()) {
            var slot = new HoseMaterial();
            slot.fill(initial, new ItemStack(Items.STONE));
            check(slot.cycle(), "Rose quartz lamp must cycle");
            check(slot.state() == initial.cycle(com.simibubi.create.content.redstone.RoseQuartzLampBlock.POWERING),
                "One click toggles POWERING without cycling POWERED or ACTIVATE");
            slot.cycle();
            check(slot.state() == initial && slot.clear().getCount() == 1, "Two clicks restore lamp without extra payment");
        }
    }
    private static void checkNativeLighting() {
        var body = new HoseMaterial();
        var port = new HoseMaterial();
        var pos = net.minecraft.core.BlockPos.ZERO;
        var world = net.minecraft.world.level.EmptyBlockGetter.INSTANCE;
        class LightManager implements net.neoforged.neoforge.common.world.AuxiliaryLightManager {
            int light;
            public void setLightAt(net.minecraft.core.BlockPos at, int value) { check(at.equals(pos), "Light uses endpoint position"); light = value; }
            public int getLightAt(net.minecraft.core.BlockPos at) { return light; }
        }
        var manager = new LightManager();
        body.fill(Blocks.REDSTONE_LAMP.defaultBlockState(), new ItemStack(Items.REDSTONE_LAMP));
        port.fill(Blocks.REDSTONE_TORCH.defaultBlockState(), new ItemStack(Items.REDSTONE_TORCH));
        CopycatHoseLight.update(manager, world, pos, body, port);
        check(manager.light == 7, "Independent port lights the endpoint while tube is dark");
        body.cycle();
        CopycatHoseLight.update(manager, world, pos, body, port);
        check(manager.light == 15, "Lit tube becomes a native light source");
        body.cycle();
        CopycatHoseLight.update(manager, world, pos, body, port);
        check(manager.light == 7, "Extinguishing tube preserves port light");
        port.clear();
        CopycatHoseLight.update(manager, world, pos, body, port);
        check(manager.light == 0, "Removing last light clears native source");
        body.fillBelt(new ItemStack(Items.DRIED_KELP));
        CopycatHoseLight.update(manager, world, pos, body, port);
        check(manager.light == 0, "Belt does not create a light source");
        CopycatHoseLight.update(manager, world, pos, null, port);
        check(manager.light == 0, "Missing tube owner is not a light source");
    }
    private static void checkBelt(net.minecraft.core.HolderLookup.Provider registries) {
        // Registry-only bootstrap has no Create items; a vanilla token exercises the same payment codec.
        var input = new ItemStack(Items.DRIED_KELP, 16);
        var belt = new HoseMaterial();
        belt.fillBelt(input);
        check(belt.filled() && belt.isBelt() && belt.emission() == 0, "Belt is a non-emissive filled slot");
        check(!belt.cycle() && input.getCount() == 16, "Belt cannot cycle block states or mutate payment stack");
        check(belt.beltAppearance().equals(com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances.BLACK), "Belt defaults to black");
        var color = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("test", "belt_color");
        com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances.register(color, null, color, color);
        check(belt.setBeltAppearance(color), "Future color hook selects a registered texture");
        check(!belt.setBeltAppearance(com.mikoalopex.createpowertrain.api.fire_hose.FireHoseAppearances.TRANSPARENT), "Belt appearance must have a body texture");
        var restored = new HoseMaterial();
        restored.read(belt.write(registries), registries);
        check(restored.isBelt() && restored.beltAppearance().equals(color), "Belt type and color hook survive reload");
        var owner = new HoseMaterial();
        owner.moveFrom(restored);
        check(owner.isBelt() && owner.beltAppearance().equals(color), "Connection transfer preserves belt appearance");
        check(!restored.filled() && restored.clear().isEmpty(), "Previous owner has no duplicate payment");
        var refund = owner.clear();
        check(refund.is(Items.DRIED_KELP) && refund.getCount() == 1, "Belt returns one original item");
        owner.read(owner.write(registries), registries);
        check(!owner.isBelt() && !owner.filled() && owner.clear().isEmpty(), "Cleared belt cannot resurrect on reload");
        belt.forgetPayment();
        check(belt.isBelt() && belt.clear().isEmpty(), "Creative clearing keeps no belt payment");
        belt.fillBelt(input);
        belt.fill(Blocks.STONE.defaultBlockState(), new ItemStack(Items.STONE));
        check(!belt.isBelt() && belt.state().is(Blocks.STONE), "Replacing material clears belt metadata");
    }
    private static void check(boolean result, String message) { if (!result) throw new AssertionError(message); }
}
