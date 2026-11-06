package io.github.simeonfarkas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Company(
        Long id,
        String ticker,
        String name,
        String isin,
        BigDecimal marketCap,
        String sector,
        String industry,
        String currency,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
    //Constructor for new companies before the database generates id & timestamps
    public static Company create(String ticker, String name, String isin, BigDecimal marketCap,
                                 String sector, String industry, String currency) {
        return new Company(null, ticker, name, isin, marketCap, sector, industry, currency, true, null, null);
    }
}
