package com.example.visuals;

public class Zoom {
    /** Текущий коэффициент приближения (1.0 = без зума), плавно меняется каждый тик. */
    public static float current = 1.0f;

    public static void tick(boolean pressed) {
        float target = pressed ? VisualsConfig.INSTANCE.zoomFactor : 1.0f;
        current += (target - current) * 0.35f;
        if (Math.abs(target - current) < 0.001f) current = target;
    }
}
