package io.github.simeonfarkas.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BalanceSheetDto(
        @JsonProperty("period_end_date") LocalDate periodEndDate,
        @JsonProperty("bs_cur_asset_report") BigDecimal totalCurrentAssets,
        @JsonProperty("bs_cur_liab") BigDecimal totalCurrentLiabilities,
        @JsonProperty("bs_net_fix_asset") BigDecimal netFixedAssets,
        @JsonProperty("bs_c_and_ce_and_sti_detailed") BigDecimal cashAndShortTermInvestments,
        @JsonProperty("bs_st_borrow") BigDecimal shortTermDebt,
        @JsonProperty("bs_lt_borrow") BigDecimal longTermDebt
) {}