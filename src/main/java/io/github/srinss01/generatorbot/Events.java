package io.github.srinss01.generatorbot;

import io.github.srinss01.generatorbot.commands.*;
import io.github.srinss01.generatorbot.database.Database;
import io.github.srinss01.generatorbot.database.ServiceInfoRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Objects;

@Component
public class Events extends ListenerAdapter {
    private final Database database;
    private final CommandsCollection commandsCollection = new CommandsCollection();

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Events.class);

    public Events(Database database, CooldownManager cooldownManager) {
        this.database = database;
        put(new Stop());
        ServiceInfoRepository serviceInfoRepository = database.getServiceInfoRepository();
        put(new Service(database));
        put(new SetCooldown(serviceInfoRepository));
        put(new Services(serviceInfoRepository));
        put(new Generate(database, cooldownManager));
        put(new Account(database));
    }

    private void put(ICustomCommand command) {
        commandsCollection.put(command.getName(), command);
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("{} is ready.", event.getJDA().getSelfUser().getName());
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event
            .getGuild()
            .updateCommands()
            .addCommands(commandsCollection.values())
            .queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Config config = database.getConfig();
        User user = event.getUser();
        String name = event.getName();
        Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getTextChannelById(config.getLogChannelId()))
                .sendMessageEmbeds(UserLogFormatter.format(user, event.getChannel().getAsMention(), event.getCommandId(), name))
                .queue();
        commandsCollection.get(name).execute(event);
    }

    private static class CommandsCollection extends HashMap<String, ICustomCommand> {}
    private static class UserLogFormatter {
        public static MessageEmbed format(User user, String channel, String commandId, String commandName) {
            return new EmbedBuilder()
                    .setTitle("Command Executed: </%s:%s>".formatted(commandName, commandId))
                    .addField("User", Objects.requireNonNull(_format(user.getAsTag())), true)
                    .addField("ID", _format(user.getId()), true)
                    .addField("Channel", channel, false)
                    .setFooter("Logged", user.getAvatarUrl())
                    .setTimestamp(OffsetDateTime.now())
                    .setColor(randomColor())
                    .build();
        }

        private static String _format(String str) {
            return "```\n%s\n```".formatted(str);
        }

        private static int randomColor() {
            return (int) (Math.random() * 0x1000000);
        }
    }
}
