package io.github.simeonfarkas.service;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.domain.model.FinancialMetrics;
import io.github.simeonfarkas.domain.repository.CompanyRepository;
import io.github.simeonfarkas.domain.repository.FinancialMetricsRepository;
import io.github.simeonfarkas.infrastructure.client.StockDataApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    private final StockDataApiClient apiClient;
    private final CompanyRepository companyRepository;
    private final FinancialMetricsRepository metricsRepository;

    public DataIngestionService(StockDataApiClient apiClient,
                                CompanyRepository companyRepository,
                                FinancialMetricsRepository metricsRepository) {
        this.apiClient = apiClient;
        this.companyRepository = companyRepository;
        this.metricsRepository = metricsRepository;
    }

    /**
     * Coordinate getting, validating and storing a list och tickers.
     * Each company is handled isolated so that errors for one company doesn't interrupt the whole batch.
     */
    public IngestionSummary ingestTickers(List<String> tickers){
        int succeeded = 0;
        int skipped = 0;
        int failed = 0;

        log.info("Starting data ingestion for {} tickers", tickers.size());

        for (String rawTicker : tickers) {
            if (rawTicker == null || rawTicker.isBlank()) {
                skipped++;
                continue;
            }

            try {
                boolean wasIngested = processSingleTicker(rawTicker.trim());
                if (wasIngested) {
                    succeeded++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed to ingest ticker '{}'. Continuing with remaingin batch.", rawTicker, e);
            }
        }

        IngestionSummary summary = new IngestionSummary(tickers.size(), succeeded, skipped, failed);
        log.info("Ingestion finished: {}", summary);
        return summary;
    }

    private boolean processSingleTicker(String ticker) {
        // 1. Get and validate company profile
        Optional<Company> fetchedCompanyOpt = apiClient.fetchCompanyProfile(ticker);
        if (fetchedCompanyOpt.isEmpty()) {
            log.warn("Skipping '{}': no company profile returned from API", ticker);
            return false;
        }

        Company fetchedCompany = fetchedCompanyOpt.get();
        if (!isValidCompany(fetchedCompany)) {
            log.warn("Skipping '{}': failed company validation (inactive or missing required fields", ticker);
            return false;
        }

        // 2. Save company and get it back with database generated ID
        companyRepository.upsertBatch(List.of(fetchedCompany));
        Company persistedComapany = companyRepository.findByTicker(fetchedCompany.ticker())
                .orElseThrow(() -> new IllegalStateException(
                        "Company was upserted but could not be retrieved by ticker: " + fetchedCompany.ticker()));

        // 3. Get and validate financial metrics (requires persistedCompany.id())
        Optional<FinancialMetrics> metricsOpt = apiClient.fetchLatestFinancials((persistedComapany));
        if (metricsOpt.isEmpty()) {
            log.warn("Skipping financials for '{}': no financial data returned from API", persistedComapany.ticker());
            return false;
        }

        FinancialMetrics metrics = metricsOpt.get();
        if (!isValidMetrics(metrics)) {
            log.warn("Skipping financials for '{}': missing critical Magic Formula metrics (EBIT, EV, or capital components",
                    persistedComapany.ticker());
            return false;
        }

        // 4. Save metrics in its own isolated transaction
        metricsRepository.upsertBatch(List.of(metrics));
        log.info("Successfully ingested compnay '{}' (reportDate={}",
                persistedComapany.ticker(), metrics.reportDate());
        return true;
    }

    /**
     * Validation rules for comapnies:
     * - Must be active (isActive = true)
     * - Must have a ticker, name and currency
     */
    boolean isValidCompany(Company company) {
        if (company == null || !company.isActive()) {
            return false;
        }
        return company.ticker() != null && !company.ticker().isBlank()
                && company.name() != null && !company.name().isBlank()
                && company.currency() != null && !company.currency().isBlank();
    }

    /**
     * Validation rules for Magic Formula data
     * - Must have report date, EBIT and EV (for Earnings Yield)
     * - Must have Current Assets, Current Liabilities and New Fixed Assets (for Return on Capital)
     */
    boolean isValidMetrics(FinancialMetrics metrics) {
        if (metrics == null || metrics.companyId() == null || metrics.reportDate() == null) {
            return false;
        }
        return metrics.ebit() != null
                && metrics.enterpriseValue() != null
                && metrics.currentAssets() != null
                && metrics.currentLiabilities() != null
                && metrics.netFixedAssets() != null;
    }
}
