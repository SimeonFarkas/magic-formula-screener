package io.github.simeonfarkas.domain.repository;

import io.github.simeonfarkas.domain.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository {
    void upsertBatch(List<Company> companies);
    Optional<Company> findByTicker(String ticker);
    List<Company> findAllActive();
}
