package com.example.visuals.mixin;

import com.example.visuals.VisualsClient;
import com.example.visuals.VisualsConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    /** Рисуем HUD и свой прицел поверх обычного интерфейса. */
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$render(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        VisualsClient.renderHud(graphics);
    }

    /** Прячем ванильный прицел, когда включён свой. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void visuals$hideCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (VisualsConfig.INSTANCE.customCrosshair) ci.cancel();
    }
}
