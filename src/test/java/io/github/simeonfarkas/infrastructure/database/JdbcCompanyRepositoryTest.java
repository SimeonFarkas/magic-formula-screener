package io.github.simeonfarkas.infrastructure.database;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.repository.JdbcCompanyRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class JdbcCompanyRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    private static DataSource dataSource;
    private JdbcCompanyRepository repository;

    @BeforeAll
    static void beforeAll() {
        //Runs Flyway-migration against container database
        DatabaseMigrator.migrate(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        dataSource = DataSourceFactory.create(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    @BeforeEach
    void setUp() {
        repository = new JdbcCompanyRepository(dataSource);
    }

    @Test
    void should_Upsert_And_Handle_Duplicates_Idempotently() {
       Company comp1 = Company.create("VOLV-B", "Volvo AB", "SE0000115446",
               new BigDecimal("500000000"), "Industrials", "Auto", "SEK");

       repository.upsertBatch(List.of(comp1));

       //Verify that the company is saved
        Optional<Company> fetched = repository.findByTicker("VOLV-B");
        assertTrue(fetched.isPresent());
        assertEquals("Volvo AB", fetched.get().name());

        //Upsert with updated name and market cap
        Company updatedComp1 = Company.create("VOLV-B", "Volvo Group AB", "SE0000115446",
                new BigDecimal("520000000"), "Industrials", "Auto", "SEK");

        repository.upsertBatch(List.of(updatedComp1));

        Optional<Company> reFetched = repository.findByTicker("VOLV-B");
        assertTrue(reFetched.isPresent());
        assertEquals("Volvo Group AB", reFetched.get().name());
        assertEquals(0, new BigDecimal("520000000").compareTo(reFetched.get().marketCap()));
    }
}
