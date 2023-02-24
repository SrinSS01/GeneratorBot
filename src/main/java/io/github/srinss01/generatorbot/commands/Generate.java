package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.CooldownManager;
import io.github.srinss01.generatorbot.database.AccountInfo;
import io.github.srinss01.generatorbot.database.AccountInfoRepository;
import io.github.srinss01.generatorbot.database.Database;
import io.github.srinss01.generatorbot.database.ServiceInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Objects;
import java.util.Optional;

public class Generate extends CommandDataImpl implements ICustomCommand {
    private final String logChannelId;
    private final Database database;
    private final CooldownManager cooldownManager;
    public Generate(Database database, CooldownManager cooldownManager) {
        super("generate", "Generates an account for a certain service");
        this.logChannelId = database.getConfig().getLogChannelId();
        this.cooldownManager = cooldownManager;
        this.database = database;
        addOption(OptionType.STRING, "service", "Name of the service", true);
    }
    @Override
    public void execute(SlashCommandInteraction interaction) {
        User user = interaction.getUser();
        String service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
        long userIdLong = user.getIdLong();
        interaction.deferReply().queue();
        InteractionHook hook = interaction.getHook();
        Optional<ServiceInfo> optionalServiceInfo = database.getServiceInfoRepository().findById(service);
        TextChannel logChannel = Objects.requireNonNull(Objects.requireNonNull(interaction.getGuild()).getTextChannelById(logChannelId));
        if (optionalServiceInfo.isEmpty()) {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setDescription("Service `" + service + "` does not exist.")
                    .setColor(0xf04747).build()
            ).queue();
            return;
        }
        ServiceInfo serviceInfo = optionalServiceInfo.get();
        Long cooldownTime = serviceInfo.getCooldownTime();
        if (cooldownManager.isOnCooldown(userIdLong, service, cooldownTime)) {
            hook.editOriginal("⏱️ You are on cooldown. Please wait another " + cooldownManager.getTimeLeft(userIdLong, service, cooldownTime))
                    .queue(message -> logChannel.sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setDescription("Command execution failed for " + user.getAsMention() + " due to cooldown on `" + service + "`")
                                    .setColor(0xf0a732)
                                    .build()
                    ).queue());
            return;
        }
        var accountId = serviceInfo.getAccountId();
        AccountInfoRepository accountInfoRepository = database.getAccountInfoRepository();
        long stock = serviceInfo.getStock();
        if (stock == 0) {
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setDescription("Service `" + service + "` is out of stock.")
                    .setColor(0xf04747).build();
            hook.editOriginalEmbeds(messageEmbed).queue();
            logChannel.sendMessageEmbeds(messageEmbed).queue();
            return;
        }
        Optional<AccountInfo> account = accountInfoRepository.findFirst(accountId);
        if (account.isEmpty()) {
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setDescription("Error while fetching account. Please try again later.")
                    .setColor(0xf04747).build();
            hook.editOriginalEmbeds(messageEmbed).queue();
            logChannel.sendMessageEmbeds(messageEmbed).queue();
            return;
        }
        AccountInfo accountInfo = account.get();
        accountInfoRepository.delete(accountInfo);
        serviceInfo.setStock(serviceInfo.getStock() - 1);
        database.getServiceInfoRepository().save(serviceInfo);
        user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle("Your " + service + " account.")
                        .setDescription("```\n" + accountInfo.getDetails() + "\n```")
                        .setColor(0x2f3136).build()
        )).onSuccess(message -> {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setAuthor("Account Generated", null, user.getAvatarUrl())
                    .setDescription("Your generated " + service + " account has been sent to your DM's!")
                    .setColor(0x43b581)
                    .build()).queue();
            cooldownManager.setCooldown(userIdLong, service);
            logChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setAuthor("`" + service + "` Account Generated", null, user.getAvatarUrl())
                            .setDescription("Sent `" + service + "` account to " + user.getAsMention())
                            .addField("Account", "```\n" + accountInfo.getDetails() + "\n```", false)
                            .setColor(0x2b2d31)
                            .build()
            ).queue();
        }).onErrorFlatMap(error -> {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setDescription("Could not send `" + service + "` account to " + user.getAsMention())
                    .addField("Error", "```\n" + error.getMessage() + "\n```", false)
                    .setColor(0xf04747).build()
            ).queue();
            return logChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription("Could not send `" + service + "` account to " + user.getAsMention())
                            .addField("Error", "```\n" + error.getMessage() + "\n```", false)
                            .setColor(0xf04747).build()
            );
        }).queue();
    }
}
