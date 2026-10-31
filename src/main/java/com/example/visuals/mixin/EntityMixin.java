package com.example.visuals.mixin;

import com.example.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** Клиентская подсветка (контур свечения) — сервер об этом не знает. */
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void visuals$glow(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) return;

        VisualsConfig cfg = VisualsConfig.INSTANCE;
        if (cfg.highlightItems && self instanceof ItemEntity) {
            cir.setReturnValue(true);
        } else if (cfg.highlightMobs && self instanceof LivingEntity
                && self != Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
