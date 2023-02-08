package io.github.srinss01.generatorbot;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CooldownManager {
    private static final Map<Long, Map<String, Long>> COOLDOWN_MANAGER = new HashMap<>();

    public boolean isOnCooldown(long userId, String service, Long cooldownTime) {
        Map<String, Long> userMap = COOLDOWN_MANAGER.get(userId);
        if (userMap == null) {
            return false;
        }
        Long aLong = userMap.get(service);
        if (aLong == null) {
            return false;
        }
        return (System.currentTimeMillis() / 1000) - aLong < cooldownTime;
    }

    public void setCooldown(long userId, String service) {
        Map<String, Long> userMap = COOLDOWN_MANAGER.get(userId);
        if (userMap == null) {
            userMap = new HashMap<>();
            userMap.put(service, System.currentTimeMillis() / 1000);
            COOLDOWN_MANAGER.put(userId, userMap);
        } else {
            userMap.put(service, System.currentTimeMillis() / 1000);
        }
    }

    public String getTimeLeft(Long userId, String service, Long cooldownTime) {
        Map<String, Long> userMap = COOLDOWN_MANAGER.get(userId);
        if (userMap == null) {
            return "0";
        }
        Long aLong = userMap.get(service);
        if (aLong == null) {
            return "0";
        }
        long timeLeft = cooldownTime - ((System.currentTimeMillis() / 1000) - aLong);
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
