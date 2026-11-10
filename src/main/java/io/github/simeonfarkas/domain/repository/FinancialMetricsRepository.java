package io.github.simeonfarkas.domain.repository;

import io.github.simeonfarkas.domain.model.FinancialMetrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialMetricsRepository {
    void upsertBatch(List<FinancialMetrics> metricsList);
    Optional<FinancialMetrics> findByCompanyIdAndReportDate(long companyId, LocalDate reportDate);
    Optional<FinancialMetrics> findLatestByCompanyId(long companyId);
    List<FinancialMetrics> findAllByCompanyId(long companyId);
}
