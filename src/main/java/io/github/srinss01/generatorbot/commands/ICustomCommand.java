package io.github.srinss01.generatorbot.commands;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICustomCommand extends CommandData {
    void execute(SlashCommandInteraction interaction);
}
