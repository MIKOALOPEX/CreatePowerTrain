package com.mikoalopex.createpowertrain.content.items.configurator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class MultifunctionConfiguratorScreen extends AbstractContainerScreen<MultifunctionConfiguratorMenu> {
    private ConfiguratorRopePanel panel;
    private Button previous, next;
    public MultifunctionConfiguratorScreen(MultifunctionConfiguratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 280; imageHeight = 201;
    }
    @Override protected void init() {
        super.init();
        if (panel == null) panel = new ConfiguratorRopePanel(font, menu.ropeSettings());
        panel.init(leftPos, topPos, widget -> addRenderableWidget(widget));
        addRenderableWidget(Button.builder(Component.translatable("createpowertrain.configurator.save"), b -> save())
            .bounds(leftPos + 28, topPos + 171, 68, 20).build()).active = minecraft.player.hasPermissions(2);
        addRenderableWidget(Button.builder(Component.translatable("rope_latch.settings.defaults"), b -> panel.defaults())
            .bounds(leftPos + 104, topPos + 171, 72, 20).build());
        previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1))
            .bounds(leftPos + 184, topPos + 171, 18, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1))
            .bounds(leftPos + 242, topPos + 171, 18, 20).build());
        refresh();
    }
    private void turn(int direction) { setFocused(null); panel.turnPage(direction); refresh(); }
    private void refresh() {
        panel.visible(true); previous.active = panel.page() > 0; next.active = panel.page() + 1 < panel.pageCount();
    }
    private void save() {
        var settings = panel.read();
        if (settings == null) minecraft.player.displayClientMessage(Component.translatable("rope_latch.settings.invalid"), true);
        else PacketDistributor.sendToServer(new ConfiguratorRopeSettingsPacket(menu.containerId, settings));
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFE0D8C8);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 23, 0xFFB9AD96);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 12, 9, 0xFF303030, false);
        panel.renderLabels(graphics);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }
}
