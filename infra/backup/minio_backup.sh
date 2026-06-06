#!/bin/bash
set -euo pipefail

# ============================================================
# VCall - MinIO backup script
# Backs up MinIO buckets to a secondary location or syncs
# between MinIO instances for disaster recovery.
# ============================================================

MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin}"
MINIO_BUCKETS="${MINIO_BUCKETS:-recordings,attachments,email-attachments,xr-assets,xr-videos,xr-models,xr-audio,xr-thumbnails,xr-scenes}"
BACKUP_DIR="${BACKUP_DIR:-/tmp/vcall-minio-backup}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Optional secondary MinIO (DR)
DR_ENDPOINT="${DR_MINIO_ENDPOINT:-}"
DR_ACCESS_KEY="${DR_MINIO_ACCESS_KEY:-}"
DR_SECRET_KEY="${DR_MINIO_SECRET_KEY:-}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

if ! command -v mc &>/dev/null; then
    log "MinIO client (mc) not found, downloading..."
    curl -sL https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/local/bin/mc
    chmod +x /usr/local/bin/mc
fi

log "Starting MinIO backup at ${TIMESTAMP}"

mc alias set primary "${MINIO_ENDPOINT}" "${MINIO_ACCESS_KEY}" "${MINIO_SECRET_KEY}"

# Backup to local directory
mkdir -p "${BACKUP_DIR}/${TIMESTAMP}"
IFS=',' read -ra BUCKETS <<< "${MINIO_BUCKETS}"
for BUCKET in "${BUCKETS[@]}"; do
    BUCKET=$(echo "${BUCKET}" | xargs)
    log "Backing up bucket: ${BUCKET}"
    mkdir -p "${BACKUP_DIR}/${TIMESTAMP}/${BUCKET}"
    mc cp --recursive "primary/${BUCKET}/" "${BACKUP_DIR}/${TIMESTAMP}/${BUCKET}/" 2>/dev/null || log "Warning: bucket ${BUCKET} may be empty or doesn't exist"
done

# Create archive
ARCHIVE="${BACKUP_DIR}/vcall-minio-${TIMESTAMP}.tar.gz"
tar -czf "${ARCHIVE}" -C "${BACKUP_DIR}" "${TIMESTAMP}"
log "MinIO backup archive: ${ARCHIVE} ($(du -h "${ARCHIVE}" | cut -f1))"

# Sync to DR site if configured
if [[ -n "${DR_ENDPOINT}" && -n "${DR_ACCESS_KEY}" && -n "${DR_SECRET_KEY}" ]]; then
    log "Syncing to DR MinIO: ${DR_ENDPOINT}"
    mc alias set dr "${DR_ENDPOINT}" "${DR_ACCESS_KEY}" "${DR_SECRET_KEY}"
    IFS=',' read -ra BUCKETS <<< "${MINIO_BUCKETS}"
    for BUCKET in "${BUCKETS[@]}"; do
        BUCKET=$(echo "${BUCKET}" | xargs)
        mc mb --ignore-existing "dr/${BUCKET}"
        log "Syncing bucket ${BUCKET} to DR..."
        mc mirror --overwrite "primary/${BUCKET}" "dr/${BUCKET}"
    done
    log "DR sync completed"
fi

log "MinIO backup completed successfully"
