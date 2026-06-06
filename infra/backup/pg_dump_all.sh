#!/bin/bash
set -euo pipefail

# ============================================================
# VCall - PostgreSQL backup script (pg_dump)
# Creates compressed SQL dumps of ALL microservice databases
# and uploads them to MinIO.
# ============================================================

BACKUP_DIR="${BACKUP_DIR:-/tmp/vcall-backups}"
PG_HOST="${PG_HOST:-localhost}"
PG_PORT="${PG_PORT:-5432}"
PG_USER="${PG_USER:-vcall}"
PG_PASSWORD="${PG_PASSWORD:-vcall@123}"
PG_DATABASES="${PG_DATABASES:-vcall_iam,vcall_agent,vcall_customer,vcall_crm,vcall_call,vcall_sip,vcall_pbx,vcall_recording,vcall_omnichannel,vcall_chat,vcall_email,vcall_sms,vcall_ticket,vcall_campaign,vcall_billing,vcall_cdr,vcall_reporting,vcall_notification,vcall_audit}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_PATH="${BACKUP_DIR}/${TIMESTAMP}"

# MinIO config
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin}"
MINIO_BUCKET="${MINIO_BUCKET:-vcall-db-backups}"

export PGPASSWORD="${PG_PASSWORD}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

cleanup() {
    rm -rf "${BACKUP_PATH}"
}
trap cleanup EXIT

mkdir -p "${BACKUP_PATH}"

# Install mc if not present
if ! command -v mc &>/dev/null; then
    log "MinIO client (mc) not found, downloading..."
    curl -sL https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/local/bin/mc
    chmod +x /usr/local/bin/mc
fi

log "Starting PostgreSQL backup at ${TIMESTAMP}"

IFS=',' read -ra DATABASES <<< "${PG_DATABASES}"
for DB in "${DATABASES[@]}"; do
    DB=$(echo "${DB}" | xargs)
    log "Dumping database: ${DB}"
    pg_dump \
        -h "${PG_HOST}" \
        -p "${PG_PORT}" \
        -U "${PG_USER}" \
        -d "${DB}" \
        --format=custom \
        --compress=9 \
        --verbose \
        --file="${BACKUP_PATH}/${DB}.dump" 2>&1 | tail -1
done

# Generate manifest
cat > "${BACKUP_PATH}/manifest.json" << EOF
{
  "timestamp": "${TIMESTAMP}",
  "host": "${PG_HOST}",
  "databases": [$(IFS=','; for DB in "${DATABASES[@]}"; do echo -n "\"${DB}\","; done | sed 's/,$//')],
  "retention_days": ${RETENTION_DAYS}
}
EOF

# Create compressed archive
ARCHIVE="${BACKUP_DIR}/vcall-pg-${TIMESTAMP}.tar.gz"
tar -czf "${ARCHIVE}" -C "${BACKUP_DIR}" "${TIMESTAMP}"

# Upload to MinIO
log "Uploading backup to MinIO: ${MINIO_BUCKET}"
mc alias set vcall-backup "${MINIO_ENDPOINT}" "${MINIO_ACCESS_KEY}" "${MINIO_SECRET_KEY}"
mc mb --ignore-existing "vcall-backup/${MINIO_BUCKET}"
mc cp "${ARCHIVE}" "vcall-backup/${MINIO_BUCKET}/postgresql/"
mc cp "${BACKUP_PATH}/manifest.json" "vcall-backup/${MINIO_BUCKET}/postgresql/${TIMESTAMP}-manifest.json"

log "Backup size: $(du -h "${ARCHIVE}" | cut -f1)"
log "Backup uploaded to MinIO successfully"

# Cleanup old backups locally
find "${BACKUP_DIR}" -name "vcall-pg-*.tar.gz" -mtime "+${RETENTION_DAYS}" -delete 2>/dev/null || true

# Cleanup old backups in MinIO (list and remove)
log "Cleaning up MinIO backups older than ${RETENTION_DAYS} days..."
mc ls "vcall-backup/${MINIO_BUCKET}/postgresql/" | while read -r line; do
    FILE_DATE=$(echo "${line}" | awk '{print $1}')
    FILE_NAME=$(echo "${line}" | awk '{print $NF}')
    if [[ -n "${FILE_DATE}" && "${FILE_NAME}" == *manifest.json ]]; then
        FILE_TS=$(date -d "${FILE_DATE}" +%s 2>/dev/null || echo 0)
        CUTOFF=$(date -d "-${RETENTION_DAYS} days" +%s)
        if [[ "${FILE_TS}" -lt "${CUTOFF}" && "${FILE_TS}" -gt 0 ]]; then
            log "Removing old backup: ${FILE_NAME}"
            mc rm "vcall-backup/${MINIO_BUCKET}/postgresql/${FILE_NAME}"
            # Also remove the corresponding archive
            ARCHIVE_NAME=$(echo "${FILE_NAME}" | sed 's/-manifest.json/.tar.gz/' | sed 's/^[0-9]\{8\}_[0-9]\{6\}-//')
            mc rm "vcall-backup/${MINIO_BUCKET}/postgresql/vcall-pg-${ARCHIVE_NAME}.tar.gz" 2>/dev/null || true
        fi
    fi
done

log "Backup completed successfully"
