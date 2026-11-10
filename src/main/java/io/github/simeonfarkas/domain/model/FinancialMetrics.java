package io.github.simeonfarkas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FinancialMetrics(
        Long id,
        Long companyId,
        LocalDate reportDate,
        BigDecimal ebit,
        BigDecimal enterpriseValue,
        BigDecimal currentAssets,
        BigDecimal currentLiabilities,
        BigDecimal netFixedAssets,
        Instant createdAt,
        Instant updatedAt
) {
    public static FinancialMetrics create(Long companyId, LocalDate reportDate, BigDecimal ebit,
                                          BigDecimal enterpriseValue, BigDecimal currentAssets,
                                          BigDecimal currentLiabilities, BigDecimal netFixedAssets) {
        return new FinancialMetrics(null, companyId, reportDate, ebit, enterpriseValue,
                currentAssets, currentLiabilities, netFixedAssets, null, null);
    }
}
