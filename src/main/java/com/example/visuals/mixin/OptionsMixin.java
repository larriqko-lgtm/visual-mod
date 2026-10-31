package com.example.visuals.mixin;

import com.example.visuals.VisualsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    /** Перед сохранением options.txt возвращаем настоящую яркость, чтобы в файл не попало 16.0. */
    @Inject(method = "save", at = @At("HEAD"))
    private void visuals$beforeSave(CallbackInfo ci) {
        VisualsClient.restoreGamma(Minecraft.getInstance());
    }
}
