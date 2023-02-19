package io.github.srinss01.generatorbot;

import io.github.srinss01.generatorbot.database.Database;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Component
@AllArgsConstructor
public class Main implements CommandLineRunner {
    private final Database database;
    private final Events events;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
    @Override
    public void run(String... args) {
        String token = database.getConfig().getToken();
        if (token == null || token.isEmpty()) {
            return;
        }
        logger.info("Starting bot with token: {}", token);
        try {
            JDABuilder
                    .createDefault(token)
                    .enableIntents(
                            GUILD_MEMBERS,
                            GUILD_EMOJIS_AND_STICKERS,
                            GUILD_VOICE_STATES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(events)
                    .disableCache(
                            CacheFlag.EMOJI,
                            CacheFlag.STICKER,
                            CacheFlag.VOICE_STATE
                    ).build();
        } catch (InvalidTokenException e) {
            if (GraphicsEnvironment.isHeadless()) {
                throw e;
            } else JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + token, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void loadServices() {
        try (Stream<Path> services = Files.list(Path.of("services"))) {
            services.forEach(path -> {
                try(var linesStream = Files.lines(path)) {
                    List<String> lines = linesStream.filter(it -> !it.isBlank()).toList();
                    Path fileName = path.getFileName();
                    Database.services.put(fileName.toString().replaceAll("_Accounts\\.txt", ""), new ArrayList<>(lines));
                } catch (IOException e) {
                    logger.error("Error loading services", e);
                }
            });
        } catch (IOException e) {
            logger.error("Error loading services", e);
        } finally {
            logger.info("Loaded services: {}", Database.services.keySet());
        }
    }
}
