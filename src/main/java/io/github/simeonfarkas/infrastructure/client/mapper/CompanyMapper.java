package io.github.simeonfarkas.infrastructure.client.mapper;

import io.github.simeonfarkas.domain.model.Company;
import io.github.simeonfarkas.infrastructure.client.dto.CompanyProfileDto;

public class CompanyMapper {

    private CompanyMapper() {}

    public static Company toDomain(CompanyProfileDto dto) {
        if (dto==null) {
            return null;
        }

        String cleanTicker = dto.symbol() !=null
                ? dto.symbol().replace(".ST", "").trim()
                : null;

        return Company.create(
                cleanTicker,
                dto.companyName(),
                dto.isin(),
                dto.marketCap(),
                dto.sector(),
                dto.industry(),
                dto.currency()
        );
    }
}
