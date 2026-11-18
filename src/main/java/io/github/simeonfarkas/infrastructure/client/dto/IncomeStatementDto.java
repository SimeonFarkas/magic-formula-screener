package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IncomeStatementDto(
        @JsonProperty("period_end_date") LocalDate periodEndDate,
        @JsonProperty("ebit") BigDecimal ebit
) {}