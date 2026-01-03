package me.hypericats.fedmaps.client;

import me.hypericats.fedmaps.commands.FmCommand;
import me.hypericats.fedmaps.config.SettingHandler;
import me.hypericats.fedmaps.feature.Esp;
import me.hypericats.fedmaps.feature.FeatureHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

public class FedMapsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SettingHandler.init();
        FeatureHandler.init();

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            FeatureHandler.getByClass(Esp.class).onWorldLoad();
        });

        WorldRenderEvents.END_MAIN.register(context -> {
            FeatureHandler.getByClass(Esp.class).onRenderWorld(context.matrices(), context.consumers(), MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false));
        });


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            FmCommand.register(dispatcher);
        });
    }
}
