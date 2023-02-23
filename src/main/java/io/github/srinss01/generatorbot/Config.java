package io.github.srinss01.generatorbot;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("bot")
@Getter
@Setter
public class Config {
    private String token;
    private String logChannelId;
    private String activationKey;
    static boolean authenticated = false;

    @Override
    public String toString() {
        return "bot:" + '\n' +
                "  " + "token: " + token + '\n' +
                "  " + "activationKey: " + activationKey + '\n' +
                "  " + "logChannelId: " + logChannelId;
    }
}
