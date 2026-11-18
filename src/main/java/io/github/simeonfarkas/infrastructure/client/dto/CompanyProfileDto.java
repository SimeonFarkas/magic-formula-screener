package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyProfileDto(
        @JsonProperty("primary_symbol") String primarySymbol,
        @JsonProperty("name") String name,
        @JsonProperty("isin") String isin,
        @JsonProperty("sector") String sector,
        @JsonProperty("industry") String industry,
        @JsonProperty("fundamental_currency") String currency
) {}