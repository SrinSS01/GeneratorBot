package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class ReloadServices extends CommandDataImpl implements ICustomCommand {
    public ReloadServices() {
        super("reload_services", "Reloads the services from disk");
        setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    @Override
    public void execute(SlashCommandInteraction interaction) {
        Main.loadServices();
        interaction.reply("Services reloaded successfully!").setEphemeral(true).queue();
    }
}
