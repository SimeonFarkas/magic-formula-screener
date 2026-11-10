package io.github.simeonfarkas.domain.repository;

import io.github.simeonfarkas.domain.model.FinancialMetrics;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFinancialMetricsRepository implements FinancialMetricsRepository {

    private final DataSource dataSource;

    private static final String UPSERT_SQL = """
            INSERT INTO financial_metrics (company_id, report_date,ebit, enterprise_value, current_assets, current_liabilities, net_fixed_assets, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (company_id, report_date) DO UPDATE SET
            ebit = EXCLUDED.ebit,
            enterprise_value = EXCLUDED.enterprise_value,
            current_assets = EXCLUDED.current_assets,
            current_liabilities = EXCLUDED.current_liabilities,
            net_fixed_assets = EXCLUDED.net_fixed_assets,
            updated_at = CURRENT_TIMESTAMP
            """;

    public JdbcFinancialMetricsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void upsertBatch(List<FinancialMetrics> metricsList) {
        if (metricsList == null || metricsList.isEmpty()){
            return;
        }

        try (Connection conn = dataSource.getConnection()){
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {
                int count = 0;
                for (FinancialMetrics m : metricsList) {
                    ps.setLong(1, m.companyId());
                    ps.setDate(2, Date.valueOf(m.reportDate()));
                    setNullableBigDecimal(ps, 3, m.ebit());
                    setNullableBigDecimal(ps, 4, m.enterpriseValue());
                    setNullableBigDecimal(ps, 5, m.currentAssets());
                    setNullableBigDecimal(ps, 6, m.currentLiabilities());
                    setNullableBigDecimal(ps, 7, m.netFixedAssets());

                    ps.addBatch();
                    count++;

                    if (count % 500 == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to run batch upsert for financial data", e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error at batch-operation", e);
        }
    }

    @Override
    public Optional<FinancialMetrics> findByCompanyIdAndReportDate(long companyId, LocalDate reportDate) {
        String sql = "SELECT * FROM financial_metrics WHERE company_id = ? AND report_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            ps.setDate(2, Date.valueOf(reportDate));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToFinancialMetrics(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get report for company_id: " + companyId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FinancialMetrics> findLatestByCompanyId(long companyId) {
        String sql = """
                SELECT * FROM financial_metrics WHERE company_id = ? ORDER BY report_date DESC LIMIT 1
                """;
        try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    return Optional.of(mapRowToFinancialMetrics(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get latest report for company_id: " + companyId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<FinancialMetrics> findAllByCompanyId(long companyId) {
        String sql = "SELECT * FROM financial_metrics WHERE company_id = ? ORDER BY report_date ASC";
        List<FinancialMetrics> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, companyId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToFinancialMetrics(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get financial history for company_id: " + companyId, e);
        }
        return List.of();
    }

    private void setNullableBigDecimal(PreparedStatement ps, int paramIndex, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(paramIndex, Types.NUMERIC);
        } else {
            ps.setBigDecimal(paramIndex, value);
        }
    }

    private FinancialMetrics mapRowToFinancialMetrics(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        Date reportDate = rs.getDate("report_date");

        return new FinancialMetrics(
                rs.getLong("id"),
                rs.getLong("company_id"),
                reportDate != null ? reportDate.toLocalDate() : null,
                rs.getBigDecimal("ebit"),
                rs.getBigDecimal("enterprise_value"),
                rs.getBigDecimal("current_assets"),
                rs.getBigDecimal("current_liabilities"),
                rs.getBigDecimal("net_fixed_assets"),
                createdAt != null ? createdAt.toInstant() : null,
                updatedAt != null ? updatedAt.toInstant() : null
        );
    }
}
