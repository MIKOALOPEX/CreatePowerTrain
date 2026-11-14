package com.mikoalopex.createpowertrain.mixin.client;

import java.util.Collection;
import com.mikoalopex.createpowertrain.CreatePowertrain;
import com.mikoalopex.createpowertrain.client.CreativeSections;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTab.class)
public abstract class CreativeSectionTabMixin {
    @Shadow private Collection<ItemStack> displayItems;

    @Inject(method = "buildContents", at = @At("TAIL"))
    private void powertrain$sections(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        if ((Object) this == CreatePowertrain.CREATIVE_TAB.get())
            displayItems = CreativeSections.arrange(displayItems);
    }
}
