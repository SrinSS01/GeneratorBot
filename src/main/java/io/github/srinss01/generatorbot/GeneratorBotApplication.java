package io.github.srinss01.generatorbot;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.srinss01.generatorbot.auth.ActivationStatus;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;


@SpringBootApplication
public class GeneratorBotApplication {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GeneratorBotApplication.class);
    private static final Scanner scanner = new Scanner(System.in);

    static boolean headless = GraphicsEnvironment.isHeadless();

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        ActivationStatus.init();
        val config = new File("config");
        if (!config.exists()) {
            val mkdir = config.mkdir();
            if (!mkdir) {
                if (!headless) {
                    JOptionPane.showMessageDialog(null, "Failed to create config directory", "Error", JOptionPane.ERROR_MESSAGE);
                }
                LOGGER.error("Failed to create config directory");
                System.exit(1);
            }
            LOGGER.info("Created config directory");
        }
        val properties = new File("config/application.yml");
        try {
            if (!properties.exists()) {
                // get environment variables
                var activationKey = Objects.requireNonNullElse(validateEnv("ACTIVATION_KEY"), ask("Enter activation key: ", "Activation Key"));
                if (!ActivationStatus.check(activationKey)) {
                    if (!headless) {
                        JOptionPane.showMessageDialog(null, "Invalid activation key", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    LOGGER.error("Invalid activation key");
                    System.exit(1);
                }
                var token = Objects.requireNonNullElse(validateEnv("TOKEN"), ask("Enter bot token: ", "Bot Token"));
                var logChannelId = Objects.requireNonNullElse(validateEnv("LOG_CHANNEL_ID"), ask("Enter log channel ID: ", "Log Channel ID"));
                Config _config = new Config();
                _config.setToken(token);
                _config.setLogChannelId(logChannelId);
                Files.writeString(properties.toPath(), _config.toString());
                LOGGER.info("Created application.yml file, please restart the bot");
            }
        } catch (UnirestException e) {
            if (!headless) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            LOGGER.error("Error while checking activation key", e);
            System.exit(1);
        } catch (IOException e) {
            if (!headless) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            LOGGER.error("Error while creating application.yml file", e);
            System.exit(1);
        }
        val services = new File("services");
        if (!services.exists()) {
            val mkdir = services.mkdir();
            if (!mkdir) {
                if (!headless) {
                    JOptionPane.showMessageDialog(null, "Failed to create services directory", "Error", JOptionPane.ERROR_MESSAGE);
                }
                LOGGER.error("Failed to create services directory");
                System.exit(1);
            }
            LOGGER.info("Created services directory");
        }
        Main.loadServices();
    }

    public static void main(String[] args) {
        SpringApplication.run(GeneratorBotApplication.class, args);
    }

    private static String validateEnv(String keyName) {
        LOGGER.info("searching for env({})...", keyName);
        var keyVal = System.getenv(keyName);
        if (keyVal == null) {
            LOGGER.info("{} not found, skipping...", keyName);
        } else {
            LOGGER.info("{} found, continuing...", keyName);
            return keyVal;
        }
        return null;
    }

    private static String ask(String message, String title) {
        if (headless) {
            System.out.print(message);
            return scanner.nextLine();
        } else {
            return JOptionPane.showInputDialog(
                    null,
                    message,
                    title,
                    JOptionPane.PLAIN_MESSAGE
            );
        }
    }
}
