package me.hypericats.fedmaps.mixin;

import me.hypericats.fedmaps.screens.SSIDScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "init")
    private void onInit(CallbackInfo ci) {
        ButtonWidget yes = ButtonWidget.builder(Text.of("SSID"), button -> MinecraftClient.getInstance().setScreen(SSIDScreen.getInstance())).width(100).position(20, 8).build();
        this.addDrawableChild(yes);
    }
}
