package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.database.ServiceInfoRepository;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Objects;

public class SetCooldown extends CommandDataImpl implements ICustomCommand {
    private final ServiceInfoRepository repository;
    public SetCooldown(ServiceInfoRepository repository) {
        super("set_cooldown", "Sets the cooldown for a service");
        addOption(OptionType.STRING, "service", "Name of the service", true);
        addOption(OptionType.INTEGER, "cooldown", "Cooldown for the service command", true);
        this.repository = repository;
    }
    @Override
    public void execute(SlashCommandInteraction interaction) {
        interaction.deferReply(true).queue();
        String service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
        var cooldown = Objects.requireNonNull(interaction.getOption("cooldown")).getAsLong();
        InteractionHook hook = interaction.getHook();
        repository.findById(service).ifPresentOrElse(serviceInfo -> {
            serviceInfo.setCooldownTime(cooldown);
            repository.save(serviceInfo);
            hook.editOriginal("Cooldown for service `" + service + "` is set to " + serviceInfo.getCooldownTime() + " seconds.").queue();
        }, () -> hook.editOriginal("Service `" + service + "` does not exist.").queue());
    }
}
