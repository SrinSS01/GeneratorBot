package io.github.srinss01.generatorbot.commands;

import io.github.srinss01.generatorbot.database.AccountInfo;
import io.github.srinss01.generatorbot.database.AccountInfoRepository;
import io.github.srinss01.generatorbot.database.Database;
import io.github.srinss01.generatorbot.database.ServiceInfoRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Objects;
import java.util.Scanner;

public class Account extends CommandDataImpl implements ICustomCommand {
    private final ServiceInfoRepository serviceInfoRepository;
    private final AccountInfoRepository accountInfoRepository;
    public Account(Database database) {
        super("account", "Generates an account");
        addSubcommandGroups(
                new SubcommandGroupData("create", "Creates an account").addSubcommands(
                        new SubcommandData("from-text", "Creates an account from the given details").addOptions(
                                new OptionData(OptionType.STRING, "service", "The name of the service", true),
                                new OptionData(OptionType.STRING, "details", "The details of the account", true)
                        ),
                        new SubcommandData("from-file", "Creates list of accounts from a file").addOptions(
                                new OptionData(OptionType.STRING, "service", "The name of the service", true),
                                new OptionData(OptionType.ATTACHMENT, "file", "The file containing the details", true)
                        )
                )
        );
        addSubcommands(
                new SubcommandData("get", "Gets the list of all account of the service").addOptions(
                    new OptionData(OptionType.STRING, "service", "The name of the service", true)
                ),
                new SubcommandData("remove", "Removes an account").addOptions(
                    new OptionData(OptionType.INTEGER, "id", "The id of the account", true),
                    new OptionData(OptionType.STRING, "service", "The name of the service", true)
                ),
                new SubcommandData("remove-all", "Removes all the accounts of the service").addOptions(
                    new OptionData(OptionType.STRING, "service", "The name of the service", true)
                )
        );
        this.serviceInfoRepository = database.getServiceInfoRepository();
        this.accountInfoRepository = database.getAccountInfoRepository();
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        interaction.deferReply().queue();
        String command = interaction.getFullCommandName();
        switch (command) {
            case "account get" -> {
                var service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
                var serviceInfoOptional = serviceInfoRepository.findById(service);
                if (serviceInfoOptional.isEmpty()) {
                    interaction.getHook().editOriginal("Service not found!").queue();
                    return;
                }
                var serviceInfo = serviceInfoOptional.get();
                var accountInfos = accountInfoRepository.findByAccountId(serviceInfo.getAccountId());
                if (accountInfos.isEmpty()) {
                    interaction.getHook().editOriginal("No accounts found!").queue();
                    return;
                }
                var builder = new EmbedBuilder()
                        .setTitle("Accounts")
                        .setDescription("List of all the accounts")
                        .setColor(0x2b2d31);
                // create a table of accounts
                StringBuilder table = new StringBuilder();
                table.append("```");
                table.append("Id\tDetails").append("\n\n");
                accountInfos.forEach(accountInfo -> table.append(accountInfo.getId()).append("\t").append(accountInfo.getDetails()).append('\n'));
                table.append("```");
                builder.setDescription(table.toString());
                interaction.getHook().editOriginalEmbeds(builder.build()).queue();
            }
            case "account remove" -> {
                var service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
                var id = Objects.requireNonNull(interaction.getOption("id")).getAsInt();
                var serviceInfoOptional = serviceInfoRepository.findById(service);
                if (serviceInfoOptional.isEmpty()) {
                    interaction.getHook().editOriginal("Service not found!").queue();
                    return;
                }
                var serviceInfo = serviceInfoOptional.get();
                var accountId = serviceInfo.getAccountId();
                int rowsAffected = accountInfoRepository.deleteByIdAndAccountId(id, accountId);
                if (rowsAffected == 0) {
                    interaction.getHook().editOriginal("Zero accounts removed!").queue();
                    return;
                }
                int stock = serviceInfo.getStock();
                interaction.getHook().editOriginalEmbeds(
                        new EmbedBuilder()
                                .setAuthor("Account removed successfully!", null, interaction.getUser().getAvatarUrl())
                                .addField("Service", Service.format(service), false)
                                .addField("Account Id", Service.format(String.valueOf(id)), true)
                                .addField("Remaining Stock", Service.format(String.valueOf(stock - 1)), true)
                                .setColor(0x43b581)
                                .build()
                ).queue();
                serviceInfo.setStock(stock - 1);
                serviceInfoRepository.save(serviceInfo);
            }
            case "account remove-all" -> {
                var service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
                var serviceInfoOptional = serviceInfoRepository.findById(service);
                if (serviceInfoOptional.isEmpty()) {
                    interaction.getHook().editOriginal("Service not found!").queue();
                    return;
                }
                var serviceInfo = serviceInfoOptional.get();
                var accountId = serviceInfo.getAccountId();
                int rowsAffected = accountInfoRepository.deleteByAccountId(accountId);
                if (rowsAffected == 0) {
                    interaction.getHook().editOriginal("Zero accounts removed!").queue();
                    return;
                }
                interaction.getHook().editOriginalEmbeds(
                        new EmbedBuilder()
                                .setAuthor("Accounts removed successfully!", null, interaction.getUser().getAvatarUrl())
                                .addField("Service", Service.format(service), false)
                                .addField("Accounts Removed", Service.format(String.valueOf(rowsAffected)), true)
                                .addField("Remaining Stock", Service.format(String.valueOf(0)), true)
                                .setColor(0x43b581)
                                .build()
                ).queue();
                serviceInfo.setStock(0);
                serviceInfoRepository.save(serviceInfo);
            }
            case "account create from-text" -> {
                var service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
                var details = Objects.requireNonNull(interaction.getOption("details")).getAsString();
                var serviceInfoOptional = serviceInfoRepository.findById(service);
                if (serviceInfoOptional.isEmpty()) {
                    interaction.getHook().editOriginal("Service not found!").queue();
                    return;
                }
                var serviceInfo = serviceInfoOptional.get();
                var accountInfo = new AccountInfo();
                accountInfo.setAccountId(serviceInfo.getAccountId());
                accountInfo.setDetails(details);
                accountInfoRepository.save(accountInfo);
                interaction.getHook().editOriginalEmbeds(
                        new EmbedBuilder()
                                .setTitle("Account created")
                                .setDescription("Account created successfully!")
                                .addField("Service", Service.format(service), true)
                                .addField("Account Id", Service.format(String.valueOf(accountInfo.getId())), true)
                                .addField("Details", Service.format(details), false)
                                .setColor(0x43b581)
                                .build()
                ).queue();
                serviceInfo.setStock(serviceInfo.getStock() + 1);
                serviceInfoRepository.save(serviceInfo);
            }
            case "account create from-file" -> {
                var service = Objects.requireNonNull(interaction.getOption("service")).getAsString();
                var attachment = Objects.requireNonNull(interaction.getOption("file")).getAsAttachment();
                var serviceInfoOptional = serviceInfoRepository.findById(service);
                if (serviceInfoOptional.isEmpty()) {
                    interaction.getHook().editOriginal("Service not found!").queue();
                    return;
                }
                var serviceInfo = serviceInfoOptional.get();

                String fileExtension = attachment.getFileExtension();
                if (fileExtension == null || !fileExtension.equals("txt")) {
                    interaction.getHook().editOriginal("Only .txt files are supported!").queue();
                    return;
                }
                interaction.getHook().editOriginal("Processing...").queue();
                TextChannel textChannel = interaction.getChannel().asTextChannel();
                attachment.getProxy().download().thenAccept(stream -> {
                    try (stream) {
                        Scanner scanner = new Scanner(stream);
                        int counter = 0;
                        while (scanner.hasNext()) {
                            String accountDetails = scanner.nextLine();
                            AccountInfo accountInfo = new AccountInfo();
                            accountInfo.setAccountId(serviceInfo.getAccountId());
                            accountInfo.setDetails(accountDetails);
                            accountInfoRepository.save(accountInfo);
                            textChannel.sendMessageEmbeds(
                                    new EmbedBuilder()
                                            .setTitle("Account created")
                                            .setDescription("Account created successfully!")
                                            .addField("Service", Service.format(service), true)
                                            .addField("Account Id", Service.format(String.valueOf(accountInfo.getId())), true)
                                            .addField("Details", Service.format(accountDetails), false)
                                            .setColor(0x43b581)
                                            .build()
                            ).queue();
                            counter++;
                        }
                        serviceInfo.setStock(serviceInfo.getStock() + counter);
                        serviceInfoRepository.save(serviceInfo);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
    }
}
