package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.database.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Objects;
import java.util.Optional;

public class Service extends CommandDataImpl implements ICustomCommand {
    private final ServiceInfoRepository serviceInfoRepository;
    private final AccountInfoRepository accountInfoRepository;
//    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Service.class);
    public Service(Database database) {
        super("service", "Generates a service");
        this.serviceInfoRepository = database.getServiceInfoRepository();
        this.accountInfoRepository = database.getAccountInfoRepository();
        addOptions(
            new OptionData(OptionType.STRING, "action", "Type of the service", true)
                    .addChoice("Create", "CREATE")
                    .addChoice("Remove", "REMOVE")
                    .addChoice("Get", "GET"),
            new OptionData(OptionType.STRING, "name", "Name of the service", true),
            new OptionData(OptionType.INTEGER, "cooldown", "Cooldown for the service command", false)
                    .setMinValue(0)
        );
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        var type = ServiceType.valueOf(Objects.requireNonNull(interaction.getOption("action")).getAsString());
        var name = Objects.requireNonNull(interaction.getOption("name")).getAsString();
        var cooldownOptional = interaction.getOption("cooldown");
        var cooldown = cooldownOptional == null ? 0 : cooldownOptional.getAsLong();
        final Optional<ServiceInfo> serviceInfoOptional = serviceInfoRepository.findById(name);
        switch (type) {
            case CREATE -> {
                interaction.deferReply().queue();
                InteractionHook hook = interaction.getHook();
                if (serviceInfoOptional.isEmpty()) {
                    ServiceInfo serviceInfo = new ServiceInfo();
                    serviceInfo.setName(name);
                    serviceInfo.setCooldownTime(cooldown);
                    serviceInfo.setStock(0);
                    serviceInfo.setAccountId(System.currentTimeMillis());
                    serviceInfoRepository.save(serviceInfo);
                    hook.editOriginal("Service created successfully!").queue();
                } else {
                    hook.editOriginal("Service already exists!").queue();
                }
            }
            case REMOVE -> {
                interaction.deferReply().queue();
                InteractionHook hook = interaction.getHook();
                if (serviceInfoOptional.isPresent()) {
                    ServiceInfo serviceInfo = serviceInfoOptional.get();
                    serviceInfoRepository.delete(serviceInfo);
                    accountInfoRepository.deleteByAccountId(serviceInfo.getAccountId());
                    hook.editOriginal("Service removed successfully!").queue();
                } else {
                    hook.editOriginal("Service doesn't exist!").queue();
                }
            }
            case GET -> {
                if (serviceInfoOptional.isEmpty()) {
                    interaction.reply("Service doesn't exist!").setEphemeral(true).queue();
                    return;
                }
                ServiceInfo info = serviceInfoOptional.get();
                long accountId = info.getAccountId();
                interaction.replyEmbeds(new EmbedBuilder()
                        .setTitle("Service Info")
                        .addField("Name", format(name), true)
                        .addField("Service Stock", format(String.valueOf(info.getStock())), true)
                        .addField("Service ID", format(String.valueOf(accountId)), false)
                        .setColor(0x2f3136)
                        .setTimestamp(interaction.getTimeCreated())
                        .build()).setEphemeral(true).queue();
            }
        }
    }

    static String format(String str) {
        return "```\n%s\n```".formatted(str);
    }

    private enum ServiceType {
        CREATE,
        REMOVE,
        GET
    }
}
