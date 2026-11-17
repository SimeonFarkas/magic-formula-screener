package io.github.simeonfarkas.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.model.FinancialMetrics;
import io.github.simeonfarkas.infrastructure.client.dto.CompanyProfileDto;
import io.github.simeonfarkas.infrastructure.client.dto.FinancialReportDto;
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
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public StockDataApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
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
            String url = String.format("%s/profile/%s?apikey=%s", baseUrl, ticker, apiKey);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Failed to fetch profile for {} - HTTP {}", ticker, response.statusCode());
                return Optional.empty();
            }

            List<CompanyProfileDto> dtoList = objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

            if (dtoList.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(CompanyMapper.toDomain(dtoList.getFirst()));

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
            String url = String.format("%s/financials/%s?apikey=%s", baseUrl, company.ticker(), apiKey);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Failed to fetch financials for {} - HTTP {}", company.ticker(), response.statusCode());
                return Optional.empty();
            }

            List<FinancialReportDto> dtoList = objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

            if (dtoList.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(FinancialMetricsMapper.toDomain(dtoList.getFirst(), company));

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Error fetching financials for {}", company.ticker(), e);
            return Optional.empty();
        }
    }
}

