package io.github.simeonfarkas.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.model.FinancialMetrics;
import io.github.simeonfarkas.infrastructure.client.dto.BalanceSheetDto;
import io.github.simeonfarkas.infrastructure.client.dto.CompanyProfileDto;
import io.github.simeonfarkas.infrastructure.client.dto.IncomeStatementDto;
import io.github.simeonfarkas.infrastructure.client.dto.RoicListResponse;
import io.github.simeonfarkas.infrastructure.client.dto.ValuationMultipleDto;
import io.github.simeonfarkas.infrastructure.client.mapper.CompanyMapper;
import io.github.simeonfarkas.infrastructure.client.mapper.FinancialMetricsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class StockDataApiClient {

    private static final Logger log = LoggerFactory.getLogger(StockDataApiClient.class);
    // ROIC.AI gratisplan tillåter max 5 anrop/minut (1 anrop var 12:e sekund)
    private static final long MIN_REQUEST_INTERVAL_MS = 12_500L;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private long lastRequestTimeMs = 0L;

    public StockDataApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public Optional<Company> fetchCompanyProfile(String ticker) {
        try {
            String symbol = toRoicSymbol(ticker);
            String url = String.format("%s/company/profile/%s", baseUrl, symbol);

            Optional<CompanyProfileDto> profile = getJson(url, new TypeReference<>() {});
            if (profile.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(CompanyMapper.toDomain(profile.get(), null, symbol));

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Error fetching profile for {}", ticker, e);
            return Optional.empty();
        }
    }

    public Optional<FinancialMetrics> fetchLatestFinancials(Company company) {
        try {
            String symbol = toRoicSymbol(company.ticker());

            String incomeUrl = String.format(
                    "%s/fundamental/income-statement/%s?period_type=annual&limit=1", baseUrl, symbol);
            Optional<RoicListResponse<IncomeStatementDto>> incomeResp = getJson(incomeUrl, new TypeReference<>() {});
            if (incomeResp.isEmpty() || incomeResp.get().data() == null || incomeResp.get().data().isEmpty()) {
                return Optional.empty();
            }

            String balanceUrl = String.format(
                    "%s/fundamental/balance-sheet/%s?period_type=annual&limit=1", baseUrl, symbol);
            Optional<RoicListResponse<BalanceSheetDto>> balanceResp = getJson(balanceUrl, new TypeReference<>() {});
            if (balanceResp.isEmpty() || balanceResp.get().data() == null || balanceResp.get().data().isEmpty()) {
                return Optional.empty();
            }

            String multiplesUrl = String.format(
                    "%s/fundamental/multiples/%s?period_type=annual&limit=1", baseUrl, symbol);
            Optional<RoicListResponse<ValuationMultipleDto>> multiplesResp = getJson(multiplesUrl, new TypeReference<>() {});
            ValuationMultipleDto multiples = multiplesResp
                    .map(RoicListResponse::data)
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .orElse(null);

            return Optional.ofNullable(FinancialMetricsMapper.toDomain(
                    incomeResp.get().data().getFirst(),
                    balanceResp.get().data().getFirst(),
                    multiples,
                    company
            ));

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Error fetching financials for {}", company.ticker(), e);
            return Optional.empty();
        }
    }

    private <T> Optional<T> getJson(String url, TypeReference<T> responseType) throws Exception {
        waitForRateLimit();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("ROIC.AI request failed ({}) - HTTP {} - Body: {}", url, response.statusCode(), response.body());
            return Optional.empty();
        }

        return Optional.ofNullable(objectMapper.readValue(response.body(), responseType));
    }

    private synchronized void waitForRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTimeMs;
        if (lastRequestTimeMs > 0 && elapsed < MIN_REQUEST_INTERVAL_MS) {
            Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
        }
        lastRequestTimeMs = System.currentTimeMillis();
    }

    private String toRoicSymbol(String ticker) {
        String trimmed = ticker.trim().toUpperCase();
        return trimmed.contains(":") ? trimmed : "STO:" + trimmed;
    }
}

