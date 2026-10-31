package com.example.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class VisualsConfig {
    public boolean fullbright = false;
    public boolean hud = true;
    public boolean showFps = true;
    public boolean showCoords = true;
    public boolean customCrosshair = true;
    public int crosshairColorIndex = 0;
    public boolean hitParticles = true;
    public int particleIndex = 0;
    public boolean highlightItems = false;
    public boolean highlightMobs = false;
    public float zoomFactor = 4.0f;

    public static VisualsConfig INSTANCE = new VisualsConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("visuals.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                VisualsConfig loaded = GSON.fromJson(Files.readString(p), VisualsConfig.class);
                if (loaded != null) INSTANCE = loaded;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
