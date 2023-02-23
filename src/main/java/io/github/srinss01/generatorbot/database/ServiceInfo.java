package io.github.srinss01.generatorbot.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_info_db")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfo {
    @Id
    String name;

    long accountId;
    long cooldownTime;
    int stock;
}
