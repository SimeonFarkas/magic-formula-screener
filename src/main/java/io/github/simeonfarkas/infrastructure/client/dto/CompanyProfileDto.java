package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyProfileDto(
        @JsonProperty("symbol") String symbol,
        @JsonProperty("companyName") String companyName,
        @JsonProperty("isin") String isin,
        @JsonProperty("mktCap") BigDecimal marketCap,
        @JsonProperty("sector") String sector,
        @JsonProperty("industry") String industry,
        @JsonProperty("currency") String currency
) {}
