package me.hypericats.fedmaps.mixin;

import me.hypericats.fedmaps.feature.Esp;
import me.hypericats.fedmaps.feature.FeatureHandler;
import me.hypericats.fedmaps.screens.SSIDScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("RETURN"), method = "getSession", cancellable = true)
    private void onGetSSID(CallbackInfoReturnable<Session> cir) {
        if (SSIDScreen.session != null)
            cir.setReturnValue(SSIDScreen.session);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTickStart(CallbackInfo ci) {
        // Tick Start
        FeatureHandler.getByClass(Esp.class).onClientTick();
    }
}
