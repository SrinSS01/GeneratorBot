package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.CooldownManager;
import io.github.srinss01.generatorbot.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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
        long userIdLong = user.getIdLong();
        if (cooldownManager.isOnCooldown(userIdLong)) {
            interaction.reply("⏱️ You are on cooldown. Please wait another " + cooldownManager.getCooldown(userIdLong)).setEphemeral(true).queue();
            return;
        }
        interaction.deferReply(true).queue();
        TextChannel logChannel = Objects.requireNonNull(Objects.requireNonNull(interaction.getGuild()).getTextChannelById(logChannelId));
        InteractionHook hook = interaction.getHook();
        String service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
        List<String> strings = Database.services.get(service);
        if (strings == null) {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Service `" + service + "` does not exist.")
                    .setColor(0xf04747).build()
            ).queue();
            return;
        }
        int size = strings.size();
        if (size == 0) {
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Service `" + service + "` is out of stock.")
                    .setColor(0xf04747).build();
            hook.editOriginalEmbeds(messageEmbed).queue();
            logChannel.sendMessageEmbeds(messageEmbed).queue();
            return;
        }
        int index = (int) (Math.random() * size);
        String account = strings.get(index);
        user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle("Your " + service + " account.")
                        .setDescription("```\n" + account + "\n```")
                        .setColor(0x2f3136).build()
        )).onSuccess(message -> {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setTitle("Account Generated")
                    .setDescription("Your generated " + service + " account has been sent to your DM's!")
                    .setColor(0x43b581)
                    .build()).queue();
            strings.remove(index);
            cooldownManager.setCooldown(userIdLong);
            logChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("`" + service + "` Account Generated")
                            .setDescription("Sent `" + service + "` account to " + user.getAsMention())
                            .addField("Account", "```\n" + account + "\n```", false)
                            .setColor(0x2f3136)
                            .build()
            ).queue();
            database.getServiceInfoRepository().findById(service).ifPresent(serviceInfo -> {
                try {
                    Files.writeString(Path.of(serviceInfo.getFile()), String.join("\n", strings));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).onErrorFlatMap(error -> {
            hook.editOriginalEmbeds(new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Could not send `" + service + "` account to " + user.getAsMention())
                    .addField("Error", "```\n" + error.getMessage() + "\n```", false)
                    .setColor(0xf04747).build()
            ).queue();
            return logChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Error")
                            .setDescription("Could not send `" + service + "` account to " + user.getAsMention())
                            .addField("Error", "```\n" + error.getMessage() + "\n```", false)
                            .setColor(0xf04747).build()
            );
        }).queue();
    }
}
