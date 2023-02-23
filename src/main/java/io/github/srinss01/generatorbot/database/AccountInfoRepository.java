package io.github.srinss01.generatorbot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Integer> {
}