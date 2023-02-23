package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.database.ServiceInfo;
import io.github.srinss01.generatorbot.database.ServiceInfoRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.List;

public class Services extends CommandDataImpl implements ICustomCommand {
    private final ServiceInfoRepository serviceInfoRepository;
    public Services(ServiceInfoRepository serviceInfoRepository) {
        super("services", "Lists all the services");
        this.serviceInfoRepository = serviceInfoRepository;
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        interaction.deferReply().queue();
        InteractionHook hook = interaction.getHook();
        List<ServiceInfo> serviceInfos = serviceInfoRepository.findAll();
        if (serviceInfos.isEmpty()) {
            hook.editOriginal("No services found.").queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Services")
                .setDescription("List of all the services")
                .setTimestamp(interaction.getTimeCreated())
                .setColor(0x2f3136);
        serviceInfos.forEach(serviceInfo -> builder.addField(serviceInfo.getName(), "```\nStock of: " + serviceInfo.getStock() + "\n```", false));
        hook.editOriginalEmbeds(builder.build()).queue();
    }
}
