package io.github.simeonfarkas.infrastructure.client.mapper;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.model.FinancialMetrics;
import io.github.simeonfarkas.infrastructure.client.dto.BalanceSheetDto;
import io.github.simeonfarkas.infrastructure.client.dto.IncomeStatementDto;
import io.github.simeonfarkas.infrastructure.client.dto.ValuationMultipleDto;

import java.math.BigDecimal;
import java.util.Objects;

public class FinancialMetricsMapper {

    private FinancialMetricsMapper() {}

    public static FinancialMetrics toDomain(
            IncomeStatementDto income,
            BalanceSheetDto balance,
            ValuationMultipleDto multiples,
            Company company
    ) {
        if (income == null || balance == null || company == null) {
            return null;
        }

        BigDecimal enterpriseValue = (multiples != null && multiples.enterpriseValue() != null)
                ? multiples.enterpriseValue()
                : calculateEnterpriseValue(company.marketCap(), balance);

        return FinancialMetrics.create(
                company.id(),
                income.periodEndDate(),
                income.ebit(),
                enterpriseValue,
                balance.totalCurrentAssets(),
                balance.totalCurrentLiabilities(),
                balance.netFixedAssets()
        );
    }

    private static BigDecimal calculateEnterpriseValue(BigDecimal marketCap, BalanceSheetDto balance) {
        if (marketCap == null) {
            return null;
        }

        BigDecimal shortDebt = Objects.requireNonNullElse(balance.shortTermDebt(), BigDecimal.ZERO);
        BigDecimal longDebt = Objects.requireNonNullElse(balance.longTermDebt(), BigDecimal.ZERO);
        BigDecimal safeCash = Objects.requireNonNullElse(balance.cashAndShortTermInvestments(), BigDecimal.ZERO);

        return marketCap.add(shortDebt).add(longDebt).subtract(safeCash);
    }
}