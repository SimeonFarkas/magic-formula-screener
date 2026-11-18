package io.github.simeonfarkas.infrastructure.client.mapper;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.infrastructure.client.dto.CompanyProfileDto;
import io.github.simeonfarkas.infrastructure.client.dto.ValuationMultipleDto;

import java.math.BigDecimal;

public class CompanyMapper {

    private CompanyMapper() {}

    public static Company toDomain(CompanyProfileDto profile) {
        return toDomain(profile, null, null);
    }

    public static Company toDomain(
            CompanyProfileDto profile,
            ValuationMultipleDto multiples,
            String requestedSymbol
    ) {
        if (profile == null) {
            return null;
        }

        String rawTicker = (requestedSymbol != null && !requestedSymbol.isBlank())
                ? requestedSymbol
                : profile.primarySymbol();

        String cleanTicker = rawTicker != null
                ? rawTicker.replace("STO:", "").trim()
                : null;

        BigDecimal marketCap = calculateMarketCap(multiples);

        return Company.create(
                cleanTicker,
                profile.name(),
                profile.isin(),
                marketCap,
                profile.sector(),
                profile.industry(),
                profile.currency()
        );
    }

    private static BigDecimal calculateMarketCap(ValuationMultipleDto multiples) {
        if (multiples == null || multiples.lastPrice() == null || multiples.sharesOutstanding() == null) {
            return null;
        }
        return multiples.lastPrice().multiply(multiples.sharesOutstanding());
    }
}