package io.github.simeonfarkas.infrastructure.client.mapper;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.model.FinancialMetrics;
import io.github.simeonfarkas.infrastructure.client.dto.FinancialReportDto;

import java.math.BigDecimal;
import java.util.Objects;

public class FinancialMetricsMapper {

    private FinancialMetricsMapper() {}

    public static FinancialMetrics toDomain(FinancialReportDto dto, Company company) {
        if (dto == null || company == null) {
            return null;
        }

        BigDecimal enterpriseValue = calculateEnterpriseValue(
                company.marketCap(),
                dto.totalDebt(),
                dto.cashAndShortTermInvestments()
        );

        return FinancialMetrics.create(
                company.id(),
                dto.reportDate(),
                dto.operatingIncome(),
                enterpriseValue,
                dto.totalCurrentAssets(),
                dto.totalCurrentLiabilities(),
                dto.netFixedAssets()
        );
    }

    private static BigDecimal calculateEnterpriseValue(
            BigDecimal marketCap,
            BigDecimal totalDebt,
            BigDecimal cashAndShortTermInvestments
    ) {
        if (marketCap == null) {
            return null;
        }

        BigDecimal safeDebt = Objects.requireNonNullElse(totalDebt, BigDecimal.ZERO);
        BigDecimal safeCash = Objects.requireNonNullElse(cashAndShortTermInvestments, BigDecimal.ZERO);

        return marketCap.add(safeDebt).subtract(safeCash);
    }
}
