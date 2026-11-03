-- 1. Extend the existing companies table
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS isin VARCHAR(12),
    ADD COLUMN IF NOT EXISTS market_cap NUMERIC(20, 2);

-- 2. Create the financial_metrics table
CREATE TABLE IF NOT EXISTS financial_metrics (
   id BIGSERIAL PRIMARY KEY,
   company_id BIGINT NOT NULL,
   report_date DATE NOT NULL,
   ebit NUMERIC(20, 2),
   enterprise_value NUMERIC(20, 2),
   current_assets NUMERIC(20, 2),
   current_liabilities NUMERIC(20, 2),
   net_fixed_assets NUMERIC(20, 2),
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_financial_metrics_company_id
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
        ON DELETE CASCADE,

   CONSTRAINT uq_financial_metrics_company_report_date
        UNIQUE (company_id, report_date)
);

-- 3. Indexes for joins and lookup performance
CREATE INDEX IF NOT EXISTS idx_financial_metrics_company_id
    ON financial_metrics (company_id);

CREATE INDEX IF NOT EXISTS idx_financial_metrics_report_date
    ON financial_metrics (report_date);