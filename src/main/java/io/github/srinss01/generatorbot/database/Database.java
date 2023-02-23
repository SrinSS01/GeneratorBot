package io.github.srinss01.generatorbot.database;

import io.github.srinss01.generatorbot.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@Component
@AllArgsConstructor
@Getter
public class Database {
    private final ServiceInfoRepository serviceInfoRepository;
    private final Config config;
    public static final Map<String, Stack<String>> services = new HashMap<>();
}
