CREATE INDEX IF NOT EXISTS idx_calls_tenant_id ON calls(tenant_id);
CREATE INDEX IF NOT EXISTS idx_calls_status ON calls(status);
CREATE INDEX IF NOT EXISTS idx_calls_agent_id ON calls(agent_id);
CREATE INDEX IF NOT EXISTS idx_calls_start_time ON calls(start_time);
CREATE INDEX IF NOT EXISTS idx_calls_queue_id ON calls(queue_id);
