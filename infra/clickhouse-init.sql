CREATE DATABASE IF NOT EXISTS vcall_cdr;

CREATE TABLE IF NOT EXISTS vcall_cdr.cdr_records (
    call_id String,
    caller_number String,
    callee_number String,
    direction String,
    start_time DateTime,
    answer_time Nullable(DateTime),
    end_time Nullable(DateTime),
    duration_seconds Int32,
    status String,
    agent_id String,
    queue_id Int64,
    cost Decimal(18,6),
    tenant_id String,
    ingestion_time DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(start_time)
ORDER BY (start_time, tenant_id, call_id);

CREATE TABLE IF NOT EXISTS vcall_cdr.cdr_hourly_summary (
    hour DateTime,
    tenant_id String,
    total_calls Int64,
    answered_calls Int64,
    missed_calls Int64,
    total_duration Int64,
    total_cost Decimal(18,6)
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (hour, tenant_id);
