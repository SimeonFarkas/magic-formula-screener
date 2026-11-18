package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValuationMultipleDto(
        @JsonProperty("period_end_date") LocalDate periodEndDate,
        @JsonProperty("pr_last") BigDecimal lastPrice,
        @JsonProperty("bs_sh_out") BigDecimal sharesOutstanding,
        @JsonProperty("enterprise_value") BigDecimal enterpriseValue
) {}