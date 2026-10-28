package io.github.simeonfarkas;

import io.github.simeonfarkas.infrastructure.database.DatabaseMigrator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        String dbHost = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault("POSTGRES_PORT", "5432");
        String dbName = System.getenv().getOrDefault("POSTGRES_DB", "magic_formula_dev");
        String dbUser = System.getenv().getOrDefault("POSTGRES_USER", "postgres");
        String dbPassword = System.getenv().getOrDefault("POSTGRES_PASSWORD", "postgres");

        String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        System.out.println("Applying migrations to " + jdbcUrl + "...");
        DatabaseMigrator.migrate(jdbcUrl, dbUser, dbPassword);
        System.out.println("Migrations applied successfully.");
    }
}
