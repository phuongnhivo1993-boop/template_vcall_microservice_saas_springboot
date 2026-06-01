#!/bin/bash
sleep 10
mc alias set vcall-minio http://localhost:9000 minioadmin minioadmin
mc mb vcall-minio/recordings
mc mb vcall-minio/attachments
mc mb vcall-minio/email-attachments
mc policy set download vcall-minio/recordings
echo "MinIO buckets created successfully"
