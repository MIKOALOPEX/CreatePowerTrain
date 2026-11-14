package com.mikoalopex.createpowertrain.content.blocks.fire_hose;

import java.util.UUID;

import com.mikoalopex.createpowertrain.Config;
import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.mikoalopex.createpowertrain.integration.sable.SableStructureCompat;
import com.simibubi.create.AllBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Manages selection on Create bracket items and places ordinary hose guides.
 * Physical segment placement is delegated to PhysicalHoseBracketInteraction.
 */
@EventBusSubscriber(modid = CreatePowertrain.MODID)
public final class HoseBracketInteraction {
    public static final String SELECTION = "createpowertrain:HoseBracketSelection";

    private HoseBracketInteraction() {}

    public static boolean isBracket(ItemStack stack) {
        return AllBlocks.METAL_BRACKET.isIn(stack) || AllBlocks.WOODEN_BRACKET.isIn(stack);
    }

    public static CompoundTag selection(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(SELECTION);
    }

    public static void selection(ItemStack stack, CompoundTag selection) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (selection.isEmpty()) tag.remove(SELECTION);
            else tag.put(SELECTION, selection);
        });
    }

    /** Binds selection to the player's current hand and inventory slot. */
    public static void selection(Player player, InteractionHand hand, CompoundTag selection) {
        selection.putUUID("SelectingPlayer", player.getUUID());
        selection.putInt("SelectedSlot", hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40);
        selection(player.getItemInHand(hand), selection);
    }

    /** Clears server-side selections after a bracket leaves its selected hand or slot. */
    public static void clearInactiveSelections(Player player) {
        if (player.level().isClientSide) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (!isBracket(stack)) continue;
            var selected = selection(stack);
            if (selected.isEmpty()) continue;
            if ((slot != inventory.selected && slot != 40) || !selected.contains("SelectedSlot")
                || selected.getInt("SelectedSlot") != slot || !selected.hasUUID("SelectingPlayer")
                || !player.getUUID().equals(selected.getUUID("SelectingPlayer")))
                selection(stack, new CompoundTag());
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Pre event) {
        clearInactiveSelections(event.getEntity());
    }

    @SubscribeEvent
    public static void toss(ItemTossEvent event) {
        if (event.getPlayer().level().isClientSide) return;
        var dropped = event.getEntity().getItem();
        if (!isBracket(dropped)) return;
        var selected = selection(dropped);
        if (selected.isEmpty()) return;
        // Dropping one item also abandons the selection on the remaining stack.
        var inventory = event.getPlayer().getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (isBracket(stack) && selected.equals(selection(stack)))
                selection(stack, new CompoundTag());
        }
        selection(dropped, new CompoundTag());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void use(PlayerInteractEvent.RightClickBlock event) {
        if (!isBracket(event.getItemStack()) || !event.getEntity().isShiftKeyDown()) return;
        clearInactiveSelections(event.getEntity());
        com.mikoalopex.createpowertrain.content.blocks.physical_hose.PhysicalHoseBracketInteraction.use(event);
    }

    private static void message(PlayerInteractEvent.RightClickBlock event, String key) {
        event.getEntity().displayClientMessage(Component.translatable("createpowertrain.hose_bracket." + key), true);
    }
}
