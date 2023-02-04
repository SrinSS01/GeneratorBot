package io.github.srinss01.generatorbot;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("bot")
@Getter
@Setter
public class Config {
    private String token;
    private List<String> subscriptionRoleIds;
    private String logChannelId;

    @Override
    public String toString() {
        return "bot:" + '\n' +
                "  " + "token: " + token + '\n' +
                "  " + "subscriptionIDs: \n" + ListToYaml(subscriptionRoleIds)
                + "  " + "logChannelId: " + logChannelId;
    }
    private static String ListToYaml(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append("    - ").append(s).append('\n');
        }
        return sb.toString();
    }
}
