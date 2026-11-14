package com.mikoalopex.createpowertrain.compatibility;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
/** Ordinary block item retained under its existing internal name. */
public final class CompatibleBlockItem extends BlockItem {
    public CompatibleBlockItem(Block block, Properties properties) { super(block, properties); }
}
