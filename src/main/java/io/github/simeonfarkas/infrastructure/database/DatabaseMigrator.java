package io.github.simeonfarkas.infrastructure.database;

import org.flywaydb.core.Flyway;

public class DatabaseMigrator {
    private DatabaseMigrator() {}

    public static void migrate(String jdbcUrl, String user, String password) {
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
    }
}
