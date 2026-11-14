package com.mikoalopex.createpowertrain.content.items.configurator;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Independent entry point for Powertrain's rope configuration. */
public class MultifunctionConfiguratorItem extends Item {
    public MultifunctionConfiguratorItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("createpowertrain.configurator.creative_only"), true);
            return InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, owner) -> new MultifunctionConfiguratorMenu(id, inventory, hand),
                Component.translatable("item.createpowertrain.multifunction_configurator")),
                buffer -> MultifunctionConfiguratorMenu.writeOpeningData(buffer, hand));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
