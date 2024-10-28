package me.dimaguy.fabricplaceholderapiParser;

import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricplaceholderapiParser implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("papi")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            return 1;
                        })
                        .then(literal("parse")
                                        .then(argument("player", StringArgumentType.word())
                                                        .suggests((context, builder) -> {
                                                            var playerManager = context.getSource().getServer().getPlayerManager();
                                                            ArrayList<String> playerNames = new ArrayList<>(List.of(playerManager.getPlayerNames()));
                                                            playerNames.add("me");
                                                            return CommandSource.suggestMatching(
                                                                    playerNames,
                                                                    builder);
                                                        }).then(argument("testedstring", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            PlaceholderContext pctx;
                                                            String playerName = StringArgumentType.getString(context, "player");
                                                            if (playerName.equals("me")) {
                                                                pctx = PlaceholderContext.of(context.getSource());
                                                            } else {
                                                                pctx = PlaceholderContext.of(context.getSource().getServer().getPlayerManager().getPlayer(playerName));
                                                            }
                                                            String testedstring = StringArgumentType.getString(context, "testedstring");
                                                            Text result = Placeholders.parseText(Text.literal(testedstring), pctx);
                                                            context.getSource().sendFeedback(() -> result, false);
                                                            return 1;
                                                        }))
                                        )
                                //TODO: Implement entity argument parsing
                        )
                        .then(literal("list")
                                .executes(context -> {
                                    var result = Placeholders.getPlaceholders().asMultimap().keys().stream()
                                            .map(Identifier::toString)
                                            .sorted()
                                            .reduce((a, b) -> a+"\n"+b)
                                            .orElse("No placeholders found");
                                    context.getSource().sendFeedback(() -> Text.of(result), false);
                                    return 1;
                                })
                                .then(argument("startswith", StringArgumentType.word())
                                        .executes(context -> {
                                            var result = Placeholders.getPlaceholders().asMultimap().keys().stream()
                                                    .map(Identifier::toString)
                                                    .filter(string -> string.startsWith(StringArgumentType.getString(context, "startswith")))
                                                    .sorted()
                                                    .reduce((a, b) -> a+"\n"+b)
                                                    .orElse("No placeholders found");
                                            context.getSource().sendFeedback(() -> Text.of(result), false);
                                            return 1;
                                        })
                                )
                        )
        ));
    }
}
