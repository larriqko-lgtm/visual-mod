package com.example.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {
    private static final float[] ZOOMS = {2f, 3f, 4f, 6f, 8f};
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Visuals"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean v) {
        return Component.literal(name + ": " + (v ? "ВКЛ" : "ВЫКЛ"));
    }

    private void toggle(int x, int y, String name, Supplier<Boolean> get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(onOff(name, get.get()), b -> {
            set.accept(!get.get());
            b.setMessage(onOff(name, get.get()));
        }).bounds(x, y, 200, 20).build());
    }

    @Override
    protected void init() {
        VisualsConfig c = VisualsConfig.INSTANCE;
        int lx = width / 2 - 205, rx = width / 2 + 5, y = 45, step = 24;

        toggle(lx, y, "Fullbright", () -> c.fullbright, v -> c.fullbright = v);
        toggle(lx, y + step, "HUD", () -> c.hud, v -> c.hud = v);
        toggle(lx, y + step * 2, "Показывать FPS", () -> c.showFps, v -> c.showFps = v);
        toggle(lx, y + step * 3, "Показывать координаты", () -> c.showCoords, v -> c.showCoords = v);
        toggle(lx, y + step * 4, "Свой прицел", () -> c.customCrosshair, v -> c.customCrosshair = v);
        addRenderableWidget(Button.builder(
                Component.literal("Цвет прицела: " + VisualsClient.COLOR_NAMES[c.crosshairColorIndex]), b -> {
                    c.crosshairColorIndex = (c.crosshairColorIndex + 1) % VisualsClient.COLORS.length;
                    b.setMessage(Component.literal("Цвет прицела: " + VisualsClient.COLOR_NAMES[c.crosshairColorIndex]));
                }).bounds(lx, y + step * 5, 200, 20).build());

        toggle(rx, y, "Частицы при ударе", () -> c.hitParticles, v -> c.hitParticles = v);
        addRenderableWidget(Button.builder(
                Component.literal("Тип частиц: " + VisualsClient.PARTICLE_NAMES[c.particleIndex]), b -> {
                    c.particleIndex = (c.particleIndex + 1) % VisualsClient.PARTICLES.length;
                    b.setMessage(Component.literal("Тип частиц: " + VisualsClient.PARTICLE_NAMES[c.particleIndex]));
                }).bounds(rx, y + step, 200, 20).build());
        toggle(rx, y + step * 2, "Подсветка предметов", () -> c.highlightItems, v -> c.highlightItems = v);
        toggle(rx, y + step * 3, "Подсветка существ", () -> c.highlightMobs, v -> c.highlightMobs = v);
        addRenderableWidget(Button.builder(Component.literal("Зум: x" + (int) c.zoomFactor), b -> {
            int idx = 0;
            for (int i = 0; i < ZOOMS.length; i++) if (ZOOMS[i] == c.zoomFactor) idx = i;
            c.zoomFactor = ZOOMS[(idx + 1) % ZOOMS.length];
            b.setMessage(Component.literal("Зум: x" + (int) c.zoomFactor));
        }).bounds(rx, y + step * 4, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(width / 2 - 100, y + step * 6 + 8, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, "Visuals — настройки", width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        VisualsConfig.save();
        minecraft.setScreen(parent);
    }
}
