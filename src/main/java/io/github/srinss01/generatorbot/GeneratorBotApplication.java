package io.github.srinss01.generatorbot;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;


@SpringBootApplication
public class GeneratorBotApplication {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GeneratorBotApplication.class);
    public static void main(String[] args) throws IOException {
        val config = new File("config");
        if (!config.exists()) {
            val mkdir = config.mkdir();
            if (!mkdir) {
                LOGGER.error("Failed to create config directory");
                return;
            }
            LOGGER.info("Created config directory");
        }
        val properties = new File("config/application.yml");
        if (!properties.exists()) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter bot token: ");
            String token = scanner.nextLine();
            System.out.print("Enter log channel ID: ");
            String logChannelID = scanner.nextLine();
            Config _config = new Config();
            _config.setToken(token);
            _config.setLogChannelId(logChannelID);
            Files.writeString(properties.toPath(), _config.toString());
            LOGGER.info("Created application.yml file, please restart the bot");
        }
        val services = new File("services");
        if (!services.exists()) {
            val mkdir = services.mkdir();
            if (!mkdir) {
                LOGGER.error("Failed to create services directory");
                return;
            }
            LOGGER.info("Created services directory");
        }
        Main.loadServices();
        SpringApplication.run(GeneratorBotApplication.class, args);
    }
}
