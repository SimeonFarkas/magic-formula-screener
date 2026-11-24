package io.github.simeonfarkas.service;

public record IngestionSummary(
        int total,
        int succeeded,
        int skipped,
        int failed
) {
}
