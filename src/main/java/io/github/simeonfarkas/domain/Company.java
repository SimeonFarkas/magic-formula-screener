package io.github.simeonfarkas.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Company(
        Long id,
        String ticker,
        String name,
        String sector,
        String industry,
        String currency,
        boolean isActive,
        String isin,
        BigDecimal marketCap,
        Instant createdAt,
        Instant updatedAt
) {
    public Company(String ticker, String name, String sector, String industry, String currency,
                   boolean isActive, String isin, BigDecimal marketCap) {
        this(null, ticker, name, sector, industry, currency, isActive, isin, marketCap, null, null);
    }
}
