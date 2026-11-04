#!/usr/bin/env bash
export POSTGRES_USER="postgres"

export POSTGRES_PASSWORD="postgres"

#docker exec -i mfs-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < src/main/resources/db/migration/V1__create_companies_table.sql

#docker exec -i mfs-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < src/main/resources/db/migration/V2__create_company_and_metric_tables.sql

docker exec -it mfs-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"