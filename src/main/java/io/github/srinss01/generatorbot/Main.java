package io.github.srinss01.generatorbot;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.srinss01.generatorbot.auth.ActivationStatus;
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

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Component
@AllArgsConstructor
public class Main implements CommandLineRunner {
    private final Database database;
    private final Events events;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
    static boolean headless = GraphicsEnvironment.isHeadless();
    @Override
    public void run(String... args) throws UnirestException {
        Config config = database.getConfig();
        if (!Config.authenticated && !ActivationStatus.check(config.getActivationKey())) {
            if (!headless) {
                JOptionPane.showMessageDialog(null, "Invalid activation key", "Error", JOptionPane.ERROR_MESSAGE);
            }
            logger.error("Invalid activation key");
            System.exit(1);
        }
        String token = config.getToken();
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
            if (headless) {
                throw e;
            } else JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + token, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
