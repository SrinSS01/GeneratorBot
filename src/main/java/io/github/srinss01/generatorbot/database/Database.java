package io.github.srinss01.generatorbot.database;

import io.github.srinss01.generatorbot.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Getter
public class Database {
    private final ServiceInfoRepository serviceInfoRepository;
    private final Config config;
    public static final Map<String, List<String>> services = new HashMap<>();
}
