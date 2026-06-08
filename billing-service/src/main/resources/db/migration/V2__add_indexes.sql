CREATE INDEX IF NOT EXISTS idx_invoices_tenant_id ON invoices(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_usage_records_tenant_id ON usage_records(tenant_id);
