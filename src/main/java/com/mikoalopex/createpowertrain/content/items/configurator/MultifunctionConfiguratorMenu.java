package com.mikoalopex.createpowertrain.content.items.configurator;

import com.mikoalopex.createpowertrain.Config;
import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.mikoalopex.createpowertrain.content.blocks.rope_latch.RopeSlideSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MultifunctionConfiguratorMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private RopeSlideSettings settings = Config.ropeLatchSlideSettings;
    public MultifunctionConfiguratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readEnum(InteractionHand.class));
        settings = new RopeSlideSettings(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readVarInt(), buffer.readVarInt(), buffer.readDouble(), buffer.readDouble());
    }
    public MultifunctionConfiguratorMenu(int id, Inventory inventory, InteractionHand hand) {
        super(CreatePowertrain.MULTIFUNCTION_CONFIGURATOR_MENU.get(), id);
        this.hand = hand;
    }
    public RopeSlideSettings ropeSettings() { return settings; }
    public static void writeOpeningData(RegistryFriendlyByteBuf buffer, InteractionHand hand) {
        buffer.writeEnum(hand);
        var s = Config.ropeLatchSlideSettings;
        buffer.writeDouble(s.friction()); buffer.writeDouble(s.geometricFriction()); buffer.writeDouble(s.responseGain());
        buffer.writeDouble(s.maxSpeed()); buffer.writeDouble(s.gravityAcceleration()); buffer.writeDouble(s.gravityRetention());
        buffer.writeDouble(s.tautRatio()); buffer.writeVarInt(s.scanIntervalTicks()); buffer.writeVarInt(s.searchDurationSeconds());
        buffer.writeDouble(s.swingDamping()); buffer.writeDouble(s.upperSwingDamping());
    }
    @Override public boolean stillValid(Player player) {
        return player.getItemInHand(hand).is(CreatePowertrain.MULTIFUNCTION_CONFIGURATOR_ITEM.get());
    }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
