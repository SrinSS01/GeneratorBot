package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.Main;
import io.github.srinss01.generatorbot.database.Database;
import io.github.srinss01.generatorbot.database.ServiceInfo;
import io.github.srinss01.generatorbot.database.ServiceInfoRepository;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Service extends CommandDataImpl implements ICustomCommand {
    private final ServiceInfoRepository repository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Service.class);
    public Service(ServiceInfoRepository repository) {
        super("service", "Generates a service");
        this.repository = repository;
        addOptions(
            new OptionData(OptionType.STRING, "action", "Type of the service", true)
                    .addChoice("Create", "CREATE")
                    .addChoice("Remove", "REMOVE")
                    .addChoice("Get", "GET")
                    .addChoice("Reload_from_disk", "RELOAD"),
            new OptionData(OptionType.STRING, "name", "Name of the service", true)
        );
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        var type = ServiceType.valueOf(Objects.requireNonNull(interaction.getOption("action")).getAsString());
        var name = Objects.requireNonNull(interaction.getOption("name")).getAsString();
        switch (type) {
            case CREATE -> {
                interaction.deferReply().queue();
                InteractionHook hook = interaction.getHook();
                val file = new File("services", name + "_Accounts.txt");
                try {
                    boolean newFile = file.createNewFile();
                    if (newFile) {
                        repository.save(new io.github.srinss01.generatorbot.database.ServiceInfo(name, file.getAbsolutePath()));
                        hook.editOriginal("Service created successfully!").queue();
                    } else {
                        hook.editOriginal("Service already exists!").queue();
                    }
                } catch (IOException e) {
                    logger.error("Error creating file for service: " + name, e);
                }
            }
            case REMOVE -> {
                interaction.deferReply().queue();
                InteractionHook hook = interaction.getHook();
                val serviceInfoOptional = repository.findById(name);
                if (serviceInfoOptional.isPresent()) {
                    ServiceInfo serviceInfo = serviceInfoOptional.get();
                    val file = new File(serviceInfo.getFile());
                    if (file.delete()) {
                        repository.delete(serviceInfo);
                        hook.editOriginal("Service removed successfully!").queue();
                    } else {
                        hook.editOriginal("Error removing service!").queue();
                    }
                } else {
                    hook.editOriginal("Service doesn't exist!").queue();
                }
            }
            case GET -> interaction.replyEmbeds(new EmbedBuilder()
                    .setTitle("Service Info")
                    .addField("Name", format(name), true)
                    .addField("Service Stock", format(String.valueOf(Database.services.get(name).size())), true)
                    .addField("File", format(repository.findById(name).map(ServiceInfo::getFile).orElse("Not found")), false)
                    .setColor(0x2f3136)
                    .build()).setEphemeral(true).queue();
            case RELOAD -> {
                Main.loadServices();
                interaction.reply("Services reloaded successfully!").setEphemeral(true).queue();
            }
        }
    }

    private static String format(String str) {
        return "```\n%s\n```".formatted(str);
    }

    private enum ServiceType {
        CREATE,
        REMOVE,
        GET,
        RELOAD
    }
}
