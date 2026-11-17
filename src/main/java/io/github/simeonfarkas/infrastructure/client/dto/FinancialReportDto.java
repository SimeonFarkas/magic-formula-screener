package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinancialReportDto(
        @JsonProperty("date") LocalDate reportDate,
        @JsonProperty("operatingIncome") BigDecimal operatingIncome,
        @JsonProperty("totalCurrentAssets") BigDecimal totalCurrentAssets,
        @JsonProperty("totalCurrentLiabilities") BigDecimal totalCurrentLiabilities,
        @JsonProperty("propertyPlantEquipmentNet") BigDecimal netFixedAssets,
        @JsonProperty("cashAndShortTermInvestments") BigDecimal cashAndShortTermInvestments,
        @JsonProperty("totalDebt") BigDecimal totalDebt
) {
}
