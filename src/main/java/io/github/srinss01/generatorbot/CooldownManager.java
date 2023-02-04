package io.github.srinss01.generatorbot;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CooldownManager {
    private final Config config;
    private static final Map<Long, Long> COOLDOWN_MANAGER = new HashMap<>();

    public boolean isOnCooldown(long userId) {
        Long aLong = COOLDOWN_MANAGER.get(userId);
        if (aLong == null) {
            return false;
        }
        return (System.currentTimeMillis() / 1000) - aLong < config.getCooldownTime();
    }

    public void setCooldown(long userId) {
        COOLDOWN_MANAGER.put(userId, System.currentTimeMillis() / 1000);
    }

    public String getCooldown(Long userId) {
        Long aLong = COOLDOWN_MANAGER.get(userId);
        if (aLong == null) {
            return "0";
        }
        long timeLeft = config.getCooldownTime() - ((System.currentTimeMillis() / 1000) - aLong);
        return formatDuration(timeLeft);
    }

    private static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(hours).append(" hours, ");
        }
        if (minutes > 0) {
            result.append(minutes).append(" minutes, ");
        }
        if (remainingSeconds > 0) {
            result.append(remainingSeconds).append(" seconds");
        }
        return result.toString().trim();
    }
}