package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class Services extends CommandDataImpl implements ICustomCommand {
    public Services() {
        super("services", "Lists all the services");
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        interaction.deferReply().queue();
        InteractionHook hook = interaction.getHook();
        if (Database.services.isEmpty()) {
            hook.editOriginal("No services found.").queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Services")
                .setDescription("List of all the services")
                .setTimestamp(interaction.getTimeCreated())
                .setColor(0x2f3136);
        Database.services.forEach((service, lines) -> builder.addField(service, "```\nStock of: " + lines.size() + "\n```", false));
        hook.editOriginalEmbeds(builder.build()).queue();
    }
}
