package me.hypericats.fedmaps.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.hypericats.fedmaps.config.Setting;
import me.hypericats.fedmaps.config.SettingHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.concurrent.CompletableFuture;

public class SettingArgument implements ArgumentType<Setting> {
//    private static final Collection<String> EXAMPLES = (Collection<String>) Stream.of(GameMode.SURVIVAL, GameMode.CREATIVE)
//            .map(GameMode::getId)
//            .collect(Collectors.toList());
    private static final DynamicCommandExceptionType INVALID_FEATURE = new DynamicCommandExceptionType(
            gameMode -> Text.of("Unknown setting: %s")
    );

    public Setting parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        Setting feature = SettingHandler.fromName(string, Setting.class);
        if (feature == null) {
            throw INVALID_FEATURE.createWithContext(stringReader, string);
        } else {
            return feature;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource
                ? CommandSource.suggestMatching(SettingHandler.getNames(), builder)
                : Suggestions.empty();
    }

    public static SettingArgument setting() {
        return new SettingArgument();
    }

    public static Setting getSetting(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, Setting.class);
    }
}
