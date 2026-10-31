package com.example.visuals;

import com.example.visuals.mixin.OptionInstanceAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

/** Вся клиентская логика. Загружается только на клиенте. */
public class VisualsClient {
    public static final String[] PARTICLE_NAMES = {"Криты", "Искры чар", "Сердца", "Огонь", "Стержни края", "Тотем"};
    public static final ParticleOptions[] PARTICLES = {
            ParticleTypes.CRIT, ParticleTypes.ENCHANTED_HIT, ParticleTypes.HEART,
            ParticleTypes.FLAME, ParticleTypes.END_ROD, ParticleTypes.TOTEM_OF_UNDYING
    };
    public static final String[] COLOR_NAMES = {"Белый", "Зелёный", "Красный", "Голубой", "Жёлтый"};
    public static final int[] COLORS = {0xFFFFFFFF, 0xFF55FF55, 0xFFFF5555, 0xFF55FFFF, 0xFFFFFF55};

    private static KeyMapping zoomKey;
    private static KeyMapping menuKey;

    // FPS
    private static long lastFpsTime = System.currentTimeMillis();
    private static int frames = 0;
    private static int fps = 0;

    // Fullbright
    private static boolean fullbrightApplied = false;
    private static double savedGamma = 1.0;

    public static void init(FMLJavaModLoadingContext context) {
        zoomKey = new KeyMapping("key.visuals.zoom", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C, KeyMapping.Category.MISC);
        menuKey = new KeyMapping("key.visuals.menu", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC);

        RegisterKeyMappingsEvent.getBus(context.getModBusGroup()).addListener(VisualsClient::registerKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(VisualsClient::onClientTick);
        ViewportEvent.ComputeFov.BUS.addListener(VisualsClient::onComputeFov);
        AttackEntityEvent.BUS.addListener(VisualsClient::onAttack);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(zoomKey);
        event.register(menuKey);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Zoom.tick(mc.screen == null && zoomKey.isDown());
        while (menuKey.consumeClick()) {
            mc.setScreen(new ConfigScreen(mc.screen));
        }
        updateFullbright(mc);
    }

    private static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (Zoom.current != 1.0f) {
            event.setFOV((float) (event.getFOV() / Zoom.current));
        }
    }

    private static void onAttack(AttackEntityEvent event) {
        VisualsConfig cfg = VisualsConfig.INSTANCE;
        Player player = event.getEntity();
        Entity target = event.getTarget();
        Level level = player.level();
        if (!level.isClientSide() || !cfg.hitParticles || !(target instanceof LivingEntity)) return;

        ParticleOptions effect = PARTICLES[cfg.particleIndex % PARTICLES.length];
        RandomSource r = level.random;
        for (int i = 0; i < 12; i++) {
            level.addParticle(effect,
                    target.getX() + (r.nextDouble() - 0.5) * target.getBbWidth(),
                    target.getY(0.3 + r.nextDouble() * 0.6),
                    target.getZ() + (r.nextDouble() - 0.5) * target.getBbWidth(),
                    (r.nextDouble() - 0.5) * 0.4,
                    r.nextDouble() * 0.3,
                    (r.nextDouble() - 0.5) * 0.4);
        }
    }

    // ---------- Fullbright ----------

    @SuppressWarnings("unchecked")
    private static void setRawGamma(Minecraft mc, double value) {
        ((OptionInstanceAccessor<Double>) (Object) mc.options.gamma()).visuals$setRawValue(value);
    }

    private static void updateFullbright(Minecraft mc) {
        if (mc.options == null) return;
        if (VisualsConfig.INSTANCE.fullbright) {
            if (!fullbrightApplied) {
                double g = mc.options.gamma().get();
                savedGamma = g > 1.0 ? 1.0 : g;
                fullbrightApplied = true;
            }
            setRawGamma(mc, 16.0);
        } else {
            restoreGamma(mc);
        }
    }

    /** Возвращает настоящее значение яркости (вызывается и перед сохранением options.txt). */
    public static void restoreGamma(Minecraft mc) {
        if (fullbrightApplied && mc.options != null) {
            setRawGamma(mc, savedGamma);
            fullbrightApplied = false;
        }
    }

    // ---------- HUD и прицел (вызывается из GuiMixin) ----------

    public static void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        VisualsConfig cfg = VisualsConfig.INSTANCE;
        if (mc.player == null || mc.options.hideGui) return;

        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }

        if (cfg.hud) {
            int y = 4;
            if (cfg.showFps) {
                g.drawString(mc.font, "FPS: " + fps, 4, y, 0xFFFFFFFF);
                y += 11;
            }
            if (cfg.showCoords) {
                BlockPos p = mc.player.blockPosition();
                g.drawString(mc.font, "XYZ: " + p.getX() + " " + p.getY() + " " + p.getZ(), 4, y, 0xFFFFFFFF);
            }
        }

        if (cfg.customCrosshair && mc.options.getCameraType().isFirstPerson()) {
            int cx = mc.getWindow().getGuiScaledWidth() / 2;
            int cy = mc.getWindow().getGuiScaledHeight() / 2;
            int color = COLORS[cfg.crosshairColorIndex % COLORS.length];
            int gap = 2, len = 5;
            g.fill(cx - gap - len, cy, cx - gap, cy + 1, color);
            g.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, color);
            g.fill(cx, cy - gap - len, cx + 1, cy - gap, color);
            g.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, color);
        }
    }
}
