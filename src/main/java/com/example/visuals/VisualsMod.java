package com.example.visuals;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("visuals")
public class VisualsMod {
    public VisualsMod(FMLJavaModLoadingContext context) {
        // Мод только клиентский: на выделенном сервере клиентских классов нет.
        try {
            Class.forName("net.minecraft.client.Minecraft");
        } catch (ClassNotFoundException e) {
            return;
        }
        VisualsConfig.load();
        VisualsClient.init(context);
    }
}
