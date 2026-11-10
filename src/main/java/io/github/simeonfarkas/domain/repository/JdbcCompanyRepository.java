package io.github.simeonfarkas.domain.repository;

import io.github.simeonfarkas.domain.model.Company;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCompanyRepository implements CompanyRepository{

    private final DataSource dataSource;

    private static final String UPSERT_SQL = """
            INSERT INTO companies (ticker, name, isin, market_cap, sector, industry, currency, is_active, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (ticker) DO UPDATE SET
                name = EXCLUDED.name,
                isin = COALESCE(EXCLUDED.isin, companies.isin),
                market_cap = EXCLUDED.market_cap,
                sector = EXCLUDED.sector,
                industry = EXCLUDED.industry,
                currency = EXCLUDED.currency,
                is_active = EXCLUDED.is_active,
                updated_at = CURRENT_TIMESTAMP
            """;

    public JdbcCompanyRepository (DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void upsertBatch(List<Company> companies) {
        if (companies.isEmpty()) return;

        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
                int count = 0;
                for (Company company : companies) {
                    ps.setString(1, company.ticker());
                    ps.setString(2, company.name());
                    ps.setString(3, company.isin());
                    ps.setBigDecimal(4, company.marketCap());
                    ps.setString(5, company.sector());
                    ps.setString(6, company.industry());
                    ps.setString(7, company.currency());
                    ps.setBoolean(8, company.isActive());

                    ps.addBatch();
                    count++;

                    // Flush batch periodically (ex. every 500 lines), to save memory
                    if (count % 500 == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Couldn't execute batch upsert for company", e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public Optional<Company> findByTicker(String ticker) {
        String sql = "SELECT * FROM companies WHERE ticker = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticker);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCompany(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get company with ticker: " + ticker, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Company> findAllActive() {
        String sql = "SELECT * FROM companies WHERE is_active = true";
        List<Company> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRowToCompany(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get active company", e);
        }
        return result;
    }

    private Company mapRowToCompany(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new Company(
                rs.getLong("id"),
                rs.getString("ticker"),
                rs.getString("name"),
                rs.getString("isin"),
                rs.getBigDecimal("market_cap"),
                rs.getString("sector"),
                rs.getString("industry"),
                rs.getString("currency"),
                rs.getBoolean("is_active"),
                createdAt != null ? createdAt.toInstant() : null,
                updatedAt != null ? updatedAt.toInstant() : null
        );
    }
}

