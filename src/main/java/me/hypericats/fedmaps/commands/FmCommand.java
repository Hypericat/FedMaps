package me.hypericats.fedmaps.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.hypericats.fedmaps.commands.arguments.SettingArgument;
import me.hypericats.fedmaps.config.Setting;
import me.hypericats.fedmaps.config.SettingHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class FmCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var fmLiteral = ClientCommandManager.literal("fm").executes(ctx -> {
            ctx.getSource().sendError(Text.of("Incomplete command!"));
            return 0;
        });

        for (Setting setting : SettingHandler.getSettings()) {
            fmLiteral.then(
                    ClientCommandManager.literal(setting.getName())
                            .then(ClientCommandManager.literal("set")
                                    .then(ClientCommandManager.argument("value", setting.getArgument())
                                            .executes(ctx -> {
                                                Object value = ctx.getArgument("value", Object.class);
                                                return set(ctx.getSource(), setting, value);
                                            })
                                    )
                            )
                            .then(ClientCommandManager.literal("get")
                                    .executes(ctx -> get(ctx.getSource(), setting))
                            )
            );
        }
        dispatcher.register(fmLiteral);
    }

    private static int set(FabricClientCommandSource source, Setting setting, Object value) {
        setting.setFromObject(value);
        source.sendFeedback(Text.of("Set " + setting.getName() + " : " + value));
        return 0;
    }

    private static int get(FabricClientCommandSource source, Setting setting) {
        source.sendFeedback(Text.of(setting.getName() + " : " + setting.getAsString()));
        return 0;
    }
}
