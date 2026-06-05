# XR SaaS Enterprise Platform — Business Flow Specifications (XRVista)

## Document Overview

| Field | Value |
|-------|-------|
| **Document** | XR_04_BUSINESS_FLOW.md |
| **Platform** | XRVista — Enterprise XR SaaS Platform |
| **Scope** | All end-to-end business flows across the platform |
| **Version** | 1.0 |
| **Last Updated** | 2026-06-05 |

### Service Reference

| Service | Port | Domain |
|---------|------|--------|
| `iam-service` | 8101 | Authentication, RBAC, user management |
| `tenant-service` | 8120 | Multi-tenant management, subscriptions, feature flags |
| `asset-service` | 8121 | Upload, store, transcode 360 video / 3D / audio |
| `scene-service` | 8122 | VR/AR/XR scene CRUD, builder state, publishing |
| `video-service` | 8123 | 360 video processing, monoscopic / stereoscopic |
| `streaming-service` | 8124 | HLS/DASH manifest, CDN origin, adaptive bitrate |
| `xr-service` | 8125 | WebXR sessions, device management, XR rendering |
| `ai-service` | 8126 | AI scene generation, hotspot gen, narration, translation |
| `analytics-service` | 8127 | Session tracking, gaze, heatmap, device stats |
| `billing-service` | 8128 | Subscription plans, invoicing, usage metering |
| `collaboration-service` | 8129 | Multi-user VR, voice chat, avatar, screen share |
| `digital-twin-service` | 8130 | Digital twin sync, IoT integration, BIM |
| `notification-service` | 8118 | Push, email, Web Push notifications |

### Kafka Topic Reference

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `asset.uploaded` | asset-service | video-service, ai-service | `AssetUploadedEvent` |
| `asset.processed` | video-service | scene-service, analytics-service | `AssetProcessedEvent` |
| `video.transcoded` | video-service | streaming-service, analytics-service | `VideoTranscodedEvent` |
| `scene.published` | scene-service | cdn-service, analytics-service | `ScenePublishedEvent` |
| `scene.saved` | scene-service | analytics-service | `SceneSavedEvent` |
| `user.joined` | collaboration-service | analytics-service | `UserJoinedEvent` |
| `analytics.event` | analytics-service | notification-service | `AnalyticsEvent` |
| `xr.session.start` | xr-service | analytics-service | `XRSessionStartEvent` |
| `ai.generation.complete` | ai-service | scene-service | `AIGenerationCompleteEvent` |
| `billing.usage.metered` | billing-service | tenant-service | `UsageMeteredEvent` |
| `iot.data.received` | digital-twin-service | scene-service | `IoTDataReceivedEvent` |
| `training.completed` | scene-service | analytics-service, notification-service | `TrainingCompletedEvent` |
| `tenant.created` | tenant-service | iam-service, notification-service | `TenantCreatedEvent` |

---

## Flow 1: Tenant Onboarding

### Overview

A Super Admin provisions a new tenant, the Tenant Admin configures the workspace and invites team members with role-specific permissions.

### Actors

| Actor | Service Role |
|-------|-------------|
| Super Admin | Platform-level administrator |
| Tenant Admin | Tenant-level administrator |
| Content Creator | Asset and scene author |
| 3D Designer | 3D model author |
| VR Designer | VR scene builder |
| AR Designer | AR experience builder |

### Preconditions

- Super Admin is authenticated with `SUPER_ADMIN` role
- Tenant Admin email is valid and not already registered
- Email service is operational (SMTP configured)
- IAM service is running on port 8101

### Swimlane Diagram

```
+------------------+  +------------------+  +------------------+  +------------------+  +------------------+
|   Super Admin    |  |  Tenant Service  |  |   IAM Service    |  | Billing Service  |  |Notif Service     |
|                  |  |     (8120)       |  |     (8101)       |  |     (8128)       |  |     (8118)       |
+--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
         |                     |                     |                     |                     |
         | 1. POST /tenants    |                     |                     |                     |
         | (name,domain,plan)  |                     |                     |                     |
         |-------------------->|                     |                     |                     |
         |                     | 2. Validate uniqueness                     |                     |
         |                     |-------------------->|                     |                     |
         |                     |                     |                     |                     |
         |                     | 3. Create tenant record                    |                     |
         |                     |-------------------->|                     |                     |
         |                     |                     |                     |                     |
         |                     | 4. Create tenant admin user                |                     |
         |                     |-------------------->|                     |                     |
         |                     |                     |                     |                     |
         |                     | 5. Initialize FREE plan                   |                     |
         |                     |---------------------------------------------------------------->|
         |                     |                     |                     |                     |
         | 6. Return tenant + admin credentials      |                     |                     |
         |<--------------------|                     |                     |                     |
         |                     |                     |                     |                     |
         | 7. Tenant Admin logs in                   |                     |                     |
         |--------------------------------------------------------->|                     |
         |                     |                     |                     |                     |
         | 8. Select subscription plan               |                     |                     |
         |----------------------------------------------------------------->|
         |                     |                     |                     |                     |
         | 9. POST /tenants/{id}/invite              |                     |                     |
         | (email, role)        |                     |                     |                     |
         |-------------------->|                     |                     |                     |
         |                     | 10. Create pending invitation              |                     |
         |                     |-------------------->|                     |                     |
         |                     |                     | 11. Send invite email                    |
         |                     |                     |------------------------------------------------>|
         |                     |                     |                     |                     |
         | 12. Team member accepts invite            |                     |                     |
         |--------------------------------------------------------->|                     |
         |                     |                     |                     |                     |
         |                     | 13. Create user with role                  |                     |
         |                     |-------------------->|                     |                     |
         |                     |                     |                     |                     |
         | 14. POST /auth/login (new user)           |                     |                     |
         |--------------------------------------------------------->|                     |
         |                     |                     | 15. Return JWT tokens                   |
         | 16. Dashboard rendered                    |                     |                     |
         |<---------------------------------------------------------|                     |
         |                     |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Super Admin | Creates tenant account | tenant-service | `POST /api/v1/tenants` |
| 2 | tenant-service | Validates domain + name uniqueness | tenant-service | Internal validation |
| 3 | tenant-service | Creates tenant record (status=ACTIVE) | tenant-service | DB insert |
| 4 | tenant-service | Creates tenant admin user | iam-service | `POST /api/v1/users` |
| 5 | billing-service | Initializes FREE plan with default limits | billing-service | DB insert |
| 6 | tenant-service | Returns tenant + admin credentials | tenant-service | HTTP 201 response |
| 7 | Tenant Admin | Logs in with credentials | iam-service | `POST /api/v1/auth/login` |
| 8 | Tenant Admin | Selects subscription plan | billing-service | `POST /api/v1/billing/subscriptions` |
| 9 | Tenant Admin | Invites team members | tenant-service | `POST /api/v1/tenants/{id}/invitations` |
| 10 | tenant-service | Creates pending invitation record | tenant-service | DB insert |
| 11 | notification-service | Sends invitation email | notification-service | Email dispatch |
| 12 | Team Member | Accepts invitation link | iam-service | `GET /api/v1/invitations/{token}/accept` |
| 13 | iam-service | Creates user with assigned role | iam-service | DB insert |
| 14 | New User | Logs in | iam-service | `POST /api/v1/auth/login` |
| 15 | iam-service | Returns JWT + refresh tokens | iam-service | HTTP 200 response |
| 16 | New User | Dashboard rendered with role-specific view | frontend | SPA routing |

### Postconditions

- Tenant record exists in `tenants` table with plan configured
- Tenant Admin user exists with `TENANT_ADMIN` role
- Team members exist with assigned roles (`CONTENT_CREATOR`, `3D_DESIGNER`, `VR_DESIGNER`, `AR_DESIGNER`)
- Subscription record created in billing-service
- Invitation emails sent and delivered

### Error Handling

| Error | Handling |
|-------|----------|
| Domain already exists | HTTP 409 Conflict — "Domain already registered" |
| Invalid email format | HTTP 400 — validation error on email field |
| Email delivery fails | Invitation record set to `PENDING_RETRY`; retry queue |
| Plan not available | HTTP 400 — "Plan not available for this region" |
| IAM service down | tenant-service returns 503; invitation queued for retry |
| Payment fails during plan selection | Subscription stays in `PENDING` state; retry logic |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `tenant.created` | Tenant created | `{ tenantId, plan, adminEmail, createdAt }` |
| `billing.usage.metered` | Plan initialized | `{ tenantId, plan, storageLimit, bandwidthLimit }` |

---

## Flow 2: Asset Upload & Processing Pipeline

### Overview

A Content Creator uploads a 360 video which flows through validation, storage, transcoding, streaming manifest generation, and CDN caching.

### Actors

| Actor | Service Role |
|-------|-------------|
| Content Creator | Asset author uploading 360 video |
| Asset Service | Upload validation and storage |
| Video Service | Transcoding pipeline |
| Streaming Service | HLS/DASH manifest generation |
| CDN (CloudFront) | Edge caching and delivery |

### Preconditions

- User is authenticated with `CONTENT_CREATOR` role
- MinIO storage is accessible
- FFmpeg binary available in video-service container
- Kafka broker is running
- CDN origin configured

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
|   Content    |  |  Asset Service   |  |  Video Service   |  |Stream Service    |  |       CDN        |
|   Creator    |  |     (8121)       |  |     (8123)       |  |     (8124)       |  |   CloudFront     |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. POST /assets/upload (VIDEO_360)      |                     |                     |
       |------------------>|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 2. Validate:         |                     |                     |
       |                   |  - format (mp4,webm) |                     |                     |
       |                   |  - size <= 10GB      |                     |                     |
       |                   |  - resolution >= 4K  |                     |                     |
       |                   |  - codec (h264/h265) |                     |                     |
       |                   |                     |                     |                     |
       |                   | 3. Store original in MinIO                |                     |
       |                   |  (bucket: assets/{tenantId}/)             |                     |
       |                   |                     |                     |                     |
       |                   | 4. Create Asset record (status=PROCESSING)|                     |
       |                   |                     |                     |                     |
       | 5. Return upload confirmation (assetId, status=PROCESSING)  |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 6. Publish asset.uploaded event (Kafka)   |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     |                     |                     |
       |                   |                     | 7. Pull original from MinIO               |
       |                   |                     |                     |                     |
       |                   |                     | 8. Transcode to:    |                     |
       |                   |                     |  - 4K (3840x2160)   |                     |
       |                   |                     |  - 6K (6144x3072)   |                     |
       |                   |                     |  - 8K (7680x3840)   |                     |
       |                   |                     |  (H.264 + H.265)   |                     |
       |                   |                     |                     |                     |
       |                   |                     | 9. Segment into 6s chunks               |
       |                   |                     |                     |                     |
       |                   |                     | 10. Store processed in MinIO             |
       |                   |                     |                     |                     |
       |                   |                     | 11. Publish video.transcoded event       |
       |                   |                     |-------------------->|                     |
       |                   |                     |                     |                     |
       |                   |                     |                     | 12. Generate HLS manifest (.m3u8) |
       |                   |                     |                     | 13. Generate DASH manifest (.mpd) |
       |                   |                     |                     |                     |
       |                   |                     |                     | 14. Store manifests in MinIO      |
       |                   |                     |                     |                     |
       |                   |                     |                     | 15. Push to CDN origin            |
       |                   |                     |                     |------------------>|
       |                   |                     |                     |                     | 16. Cache segments + manifests
       |                   |                     |                     |                     |
       |                   | 17. Update asset status = READY           |                     |
       |                   |<--------------------|                     |                     |
       |                   |                     |                     |                     |
       | 18. WebSocket: asset ready              |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Content Creator | Uploads 360 video file | asset-service | `POST /api/v1/assets/upload` (multipart) |
| 2 | asset-service | Validates format, size, resolution, codec | asset-service | Internal validation |
| 3 | asset-service | Stores original in MinIO | asset-service | MinIO `putObject()` |
| 4 | asset-service | Creates Asset record with status=PROCESSING | asset-service | DB insert |
| 5 | asset-service | Returns upload confirmation | asset-service | HTTP 201 — `{ assetId, status }` |
| 6 | asset-service | Publishes `asset.uploaded` event | Kafka | `KafkaTemplate.send()` |
| 7 | video-service | Pulls original file from MinIO | video-service | MinIO `getObject()` |
| 8 | video-service | Transcodes to 4K/6K/8K (H.264 + H.265) | video-service | FFmpeg subprocess |
| 9 | video-service | Segments into 6-second chunks | video-service | FFmpeg segment muxer |
| 10 | video-service | Stores processed files in MinIO | video-service | MinIO `putObject()` |
| 11 | video-service | Publishes `video.transcoded` event | Kafka | `KafkaTemplate.send()` |
| 12 | streaming-service | Generates HLS manifest (.m3u8) | streaming-service | Manifest builder |
| 13 | streaming-service | Generates DASH manifest (.mpd) | streaming-service | Manifest builder |
| 14 | streaming-service | Stores manifests in MinIO | streaming-service | MinIO `putObject()` |
| 15 | streaming-service | Pushes to CDN origin | streaming-service | CloudFront invalidation |
| 16 | CDN | Caches processed segments at edge | CloudFront | Automatic edge cache |
| 17 | video-service | Updates asset status to READY | asset-service | `PUT /api/v1/assets/{id}/status` |
| 18 | asset-service | Sends WebSocket notification to user | asset-service | WebSocket push |

### Postconditions

- Original video stored in MinIO at `assets/{tenantId}/{assetId}/original`
- Transcoded videos stored at `processed/{assetId}/4K/`, `6K/`, `8K/`
- HLS and DASH manifests generated and stored
- CDN edge locations populated with cached segments
- Asset record status = `READY`
- User receives real-time notification that processing is complete

### Error Handling

| Error | Handling |
|-------|----------|
| Invalid file format | HTTP 400 — "Unsupported format. Accepted: mp4, webm, mov" |
| File exceeds size limit | HTTP 413 — "File exceeds maximum size of 10GB" |
| Resolution below 4K | HTTP 400 — "Minimum resolution 3840x2160 required for 360 video" |
| FFmpeg crash during transcode | Asset status set to `FAILED`; error event published; retry up to 3 times |
| MinIO unreachable | HTTP 503 — "Storage temporarily unavailable"; upload retried |
| CDN invalidation fails | Warning logged; content eventually propagated by TTL |
| Kafka consumer lag | video-service scales horizontally; monitoring alerts at lag > 1000 |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `asset.uploaded` | Asset uploaded | `{ assetId, tenantId, type, originalUrl, size, format }` |
| `video.transcoded` | Transcoding complete | `{ assetId, resolutions: ["4K","6K","8K"], hlsUrl, dashUrl }` |
| `asset.processed` | Full pipeline done | `{ assetId, status: "READY", cdnUrls: {...} }` |

---

## Flow 3: 3D Model Processing

### Overview

A 3D Designer uploads a 3D model file which undergoes compression, texture optimization, LOD generation, and thumbnail rendering.

### Actors

| Actor | Service Role |
|-------|-------------|
| 3D Designer | 3D model author |
| Asset Service | Upload and orchestration |
| Processing Pipeline | Draco, KTX2, LOD, thumbnail workers |

### Preconditions

- User is authenticated with `3D_DESIGNER` role
- Draco encoder binary available
- KTX2 encoder (basis-universal) available
- MinIO storage accessible
- Processing workers running

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
| 3D Designer  |  |  Asset Service   |  |  Draco Worker    |  |  KTX2 Worker     |  | LOD + Thumbnail  |
|              |  |     (8121)       |  |                  |  |                  |  |    Generator     |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. POST /assets/upload (MODEL_3D)       |                     |                     |
       |------------------>|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 2. Validate:         |                     |                     |
       |                   |  - format (glb,gltfb,obj)                |                     |
       |                   |  - size <= 500MB     |                     |                     |
       |                   |  - polygon count     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 3. Store original in MinIO                |                     |
       |                   |                     |                     |                     |
       |                   | 4. Create Asset record (status=PROCESSING)|                     |
       |                   |                     |                     |                     |
       | 5. Return upload confirmation            |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 6. Publish asset.uploaded event (Kafka)   |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     |                     |                     |
       |                   |                     | 7. Apply Draco mesh compression           |
       |                   |                     |  (reduces geometry 80-90%)                |
       |                   |                     |                     |                     |
       |                   |                     | 8. Convert textures to KTX2/Basis        |
       |                   |                     |-------------------->|                     |
       |                   |                     |                     |                     |
       |                   |                     |                     | 9. Generate LOD levels: |
       |                   |                     |                     |  - LOD0 (full)    |
       |                   |                     |                     |  - LOD1 (75%)     |
       |                   |                     |                     |  - LOD2 (50%)     |
       |                   |                     |                     |  - LOD3 (25%)     |
       |                   |                     |                     |-------------------->|
       |                   |                     |                     |                     |
       |                   |                     |                     | 10. Generate thumbnails: |
       |                   |                     |                     |  - 256x256        |
       |                   |                     |                     |  - 512x512        |
       |                   |                     |                     |  - 1024x1024      |
       |                   |                     |                     |                     |
       |                   | 11. Store all processed variants in MinIO |
       |                   |<----------------------------------------------------------------|
       |                   |                     |                     |                     |
       |                   | 12. Update asset metadata + status=READY  |                     |
       |                   |                     |                     |                     |
       | 13. WebSocket: model ready               |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | Detail |
|------|-------|--------|---------|--------|
| 1 | 3D Designer | Uploads 3D model file | asset-service | `POST /api/v1/assets/upload` |
| 2 | asset-service | Validates format, size, polygon count | asset-service | Accept: glb, gltf, fbx, obj |
| 3 | asset-service | Stores original in MinIO | asset-service | `assets/{tenantId}/{assetId}/original` |
| 4 | asset-service | Creates Asset record (status=PROCESSING) | asset-service | DB insert |
| 5 | asset-service | Returns upload confirmation | asset-service | HTTP 201 |
| 6 | asset-service | Publishes `asset.uploaded` event | Kafka | Topic: `asset.uploaded` |
| 7 | Draco Worker | Applies Draco mesh compression | processing | Reduces geometry 80-90% |
| 8 | KTX2 Worker | Converts textures to KTX2/Basis format | processing | GPU-optimized texture format |
| 9 | LOD Worker | Generates LOD levels (0-3) | processing | Progressive mesh simplification |
| 10 | Thumbnail Worker | Renders model thumbnails | processing | 256/512/1024px PNG renders |
| 11 | Workers | Store all variants in MinIO | asset-service | MinIO `putObject()` per variant |
| 12 | asset-service | Updates metadata + status=READY | asset-service | DB update |
| 13 | asset-service | Sends WebSocket notification | asset-service | Real-time push to client |

### Postconditions

- Original model stored at `assets/{tenantId}/{assetId}/original`
- Draco-compressed version at `processed/{assetId}/draco/`
- KTX2 textures at `processed/{assetId}/ktx2/`
- LOD variants at `processed/{assetId}/lod0/` through `lod3/`
- Thumbnails at `processed/{assetId}/thumbnails/`
- Asset status = `READY`

### Error Handling

| Error | Handling |
|-------|----------|
| Unsupported format | HTTP 400 — "Accepted formats: GLB, GLTF, FBX, OBJ" |
| Draco compression fails | Fallback to original geometry; warning logged |
| KTX2 conversion fails | Fallback to original textures; warning logged |
| Polygon count exceeds limit | HTTP 400 — "Model exceeds maximum polygon count" |
| Processing worker crashes | Job retried up to 3 times; asset status set to `FAILED` |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `asset.uploaded` | Asset uploaded | `{ assetId, tenantId, type: "MODEL_3D", format, polygonCount }` |
| `asset.processed` | Processing complete | `{ assetId, variants: ["draco","ktx2","lod","thumbnail"], status: "READY" }` |

---

## Flow 4: VR Scene Creation (No-Code Builder)

### Overview

A VR Designer uses the drag-and-drop Scene Builder to assemble a complete VR scene with 360 backgrounds, 3D models, hotspots, audio, and lighting. The scene graph is persisted in MongoDB.

### Actors

| Actor | Service Role |
|-------|-------------|
| VR Designer | Scene author using the no-code builder |
| Scene Service | Scene CRUD and graph management |
| Asset Service | Asset library queries |
| MongoDB | Scene graph document store |
| CDN | Published asset delivery |

### Preconditions

- User is authenticated with `VR_DESIGNER` role
- At least one 360 background asset exists (status=READY)
- MongoDB is running and accessible
- Scene Builder frontend loaded (Three.js + React Flow)
- Asset library populated with 3D models

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
| VR Designer  |  |  Scene Service   |  |  Asset Service   |  |     MongoDB      |  |       CDN        |
|              |  |     (8122)       |  |     (8121)       |  |                  |  |                  |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. POST /scenes (type=VR_TOUR)          |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 2. Create scene record (status=DRAFT)     |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       |                   | 3. Create empty scene graph in MongoDB    |                     |
       |                   |  { nodes: [], edges: [] }                 |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 4. Return sceneId |                     |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       | 5. Open Scene Builder (drag & drop UI)  |                     |                     |
       |                   |                     |                     |                     |
       | 6. GET /assets?type=IMAGE_360           |                     |                     |
       |--------------------------------------->|                     |                     |
       | 7. Return 360 image list               |                     |                     |
       |<---------------------------------------|                     |                     |
       |                   |                     |                     |                     |
       | 8. Select 360 background               |                     |                     |
       |  POST /scenes/{id}/nodes               |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 9. Add background node to graph           |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 10. Add hotspots (info, nav, media, action)                  |                     |
       |  POST /scenes/{id}/nodes (multiple)      |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 11. Add hotspot nodes to graph            |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 12. GET /assets?type=MODEL_3D           |                     |                     |
       |--------------------------------------->|                     |                     |
       | 13. Return 3D model list               |                     |                     |
       |<---------------------------------------|                     |                     |
       |                   |                     |                     |                     |
       | 14. Place 3D models in scene           |                     |                     |
       |  POST /scenes/{id}/nodes               |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 15. Add model nodes with transforms       |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 16. Add spatial audio                  |                     |                     |
       |  POST /scenes/{id}/nodes               |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 17. Add audio node with position          |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 18. Configure lighting                 |                     |                     |
       |  PATCH /scenes/{id}/lighting           |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 19. Update scene lighting config          |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 20. Auto-save (every 30s)              |                     |                     |
       |  PATCH /scenes/{id}/graph              |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 21. Upsert scene graph in MongoDB         |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       | 22. Preview scene in WebXR             |                     |                     |
       |  GET /scenes/{id}/preview              |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 23. Load full scene graph                 |                     |
       |                   |----------------------------------------->|                     |
       |                   | 24. Return scene + asset URLs             |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       | 25. Publish scene                     |                     |                     |
       |  POST /scenes/{id}/publish             |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 26. Validate completeness                 |                     |
       |                   |  (background, >= 1 hotspot)               |                     |
       |                   |                     |                     |                     |
       |                   | 27. Set status=PUBLISHED                  |                     |
       |                   |  Generate public URL                      |                     |
       |                   |----------------------------------------->|                     |
       |                   |                     |                     |                     |
       |                   | 28. Publish scene.published event         |                     |
       |                   |------------------------------------------------------------>|
       |                   |                     |                     |                     |
       | 29. Return public URL                  |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | VR Designer | Creates new scene | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates scene record (status=DRAFT) | scene-service | DB insert |
| 3 | scene-service | Creates empty scene graph in MongoDB | MongoDB | `{ nodes: [], edges: [] }` |
| 4 | scene-service | Returns sceneId | scene-service | HTTP 201 |
| 5 | VR Designer | Opens Scene Builder UI | frontend | React Flow canvas |
| 6 | VR Designer | Browses 360 images from asset library | asset-service | `GET /api/v1/assets?type=IMAGE_360` |
| 7 | asset-service | Returns filtered asset list | asset-service | HTTP 200 |
| 8 | VR Designer | Selects 360 background | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 9 | scene-service | Adds background node to graph | MongoDB | Graph upsert |
| 10 | VR Designer | Places hotspots (info, navigation, media, action) | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 11 | scene-service | Adds hotspot nodes with metadata | MongoDB | Graph upsert |
| 12 | VR Designer | Browses 3D models from asset library | asset-service | `GET /api/v1/assets?type=MODEL_3D` |
| 13 | asset-service | Returns 3D model list | asset-service | HTTP 200 |
| 14 | VR Designer | Places 3D models in scene | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 15 | scene-service | Adds model nodes with transform data | MongoDB | Graph upsert |
| 16 | VR Designer | Adds spatial audio source | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 17 | scene-service | Adds audio node with position/range | MongoDB | Graph upsert |
| 18 | VR Designer | Configures scene lighting | scene-service | `PATCH /api/v1/scenes/{id}/lighting` |
| 19 | scene-service | Updates lighting config | MongoDB | Document update |
| 20 | VR Designer | Auto-save triggered (every 30 seconds) | scene-service | `PATCH /api/v1/scenes/{id}/graph` |
| 21 | scene-service | Upserts full scene graph in MongoDB | MongoDB | Document replace |
| 22 | VR Designer | Clicks preview | scene-service | `GET /api/v1/scenes/{id}/preview` |
| 23-24 | scene-service | Loads and returns full scene graph | scene-service | HTTP 200 |
| 25 | VR Designer | Clicks publish | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 26 | scene-service | Validates completeness (background + >= 1 hotspot) | scene-service | Validation rules |
| 27 | scene-service | Sets status=PUBLISHED, generates public URL | scene-service | DB update |
| 28 | scene-service | Publishes `scene.published` event | Kafka | CDN cache invalidation |
| 29 | scene-service | Returns public URL | scene-service | HTTP 200 |

### Postconditions

- Scene record exists with status=PUBLISHED
- Scene graph stored in MongoDB with all nodes (backgrounds, hotspots, models, audio)
- Lighting configuration saved
- Public URL generated (e.g., `https://xrvista.com/scene/{sceneId}`)
- CDN cached with published scene assets
- Scene accessible to end users via public URL

### Error Handling

| Error | Handling |
|-------|----------|
| No background selected | HTTP 400 — "Scene must have at least one 360 background" |
| No hotspots placed | HTTP 400 — "Scene must have at least one hotspot" |
| MongoDB unavailable | Scene auto-saves to local state; retry on reconnect |
| Asset not found | HTTP 404 — "Asset not found or not accessible" |
| Scene graph too large | HTTP 413 — "Scene graph exceeds maximum size of 10MB" |
| Concurrent edit conflict | Version field checked; last-write-wins with conflict notification |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.saved` | Scene auto-saved | `{ sceneId, tenantId, nodeCount, lastModified }` |
| `scene.published` | Scene published | `{ sceneId, tenantId, type, publicUrl, publishedAt }` |

---

## Flow 5: 360 Video Experience

### Overview

A Content Creator creates a 360 video project, uploads video, configures player settings, adds hotspots on the timeline, and publishes for end-user viewing across devices.

### Actors

| Actor | Service Role |
|-------|-------------|
| Content Creator | 360 video project author |
| Scene Service | Project and hotspot management |
| Video Service | Video processing |
| Streaming Service | HLS/DASH delivery |
| End User | Viewer on Web/Mobile/VR |

### Preconditions

- User is authenticated with `CONTENT_CREATOR` role
- 360 video file ready for upload
- Video processing pipeline operational
- Streaming service configured

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
|   Content    |  |  Scene Service   |  |  Video Service   |  |Stream Service    |  |    End User      |
|   Creator    |  |     (8122)       |  |     (8123)       |  |     (8124)       |  |                  |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. POST /scenes (type=VIDEO_360)        |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 2. Create project record                  |                     |
       |                   |-------------------->|                     |                     |
       | 3. Return sceneId |                     |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       | 4. Select mono/stereo mode             |                     |                     |
       |  PATCH /scenes/{id}/video-config       |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 5. Save video config  |                     |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     |                     |                     |
       | 6. Upload 360 video                   |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 7. Store + publish asset.uploaded        |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     | 8. Process video    |                     |
       |                   |                     |  (transcode, segment)                      |
       |                   |                     |                     |                     |
       |                   |                     | 9. video.transcoded event                 |
       |                   |                     |-------------------->|                     |
       |                   |                     |                     | 10. Generate manifests |
       |                   |                     |                     |                     |
       | 11. Configure player settings         |                     |                     |
       |  PATCH /scenes/{id}/player            |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 12. Save player config (quality, autoplay, loop)               |
       |                   |                     |                     |                     |
       | 13. Add hotspots on timeline          |                     |                     |
       |  POST /scenes/{id}/hotspots           |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 14. Save hotspot data with timestamps     |                     |
       |                   |                     |                     |                     |
       | 15. Publish                          |                     |                     |
       |  POST /scenes/{id}/publish            |                     |                     |
       |------------------>|                     |                     |                     |
       |                   | 16. Validate + set status=PUBLISHED      |                     |
       |                   |  Generate embed URL                      |                     |
       |                   |                     |                     |                     |
       | 17. Return embed URL + QR code        |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   |                     |                     | 18. End User opens URL
       |                   |                     |                     |                     |
       |                   |                     |                     | 19. Device detection |
       |                   |                     |                     |  (Web/Mobile/VR/Cardboard)
       |                   |                     |                     |                     |
       |                   |                     |                     | 20. Stream video with hotspots
       |                   |                     |                     |------------------------------------------------>
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Content Creator | Creates 360 video project | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates scene record (type=VIDEO_360) | scene-service | DB insert |
| 3 | scene-service | Returns sceneId | scene-service | HTTP 201 |
| 4 | Content Creator | Selects monoscopic or stereoscopic mode | scene-service | `PATCH /api/v1/scenes/{id}/video-config` |
| 5 | scene-service | Saves video configuration | scene-service | DB update |
| 6 | Content Creator | Uploads 360 video file | asset-service | `POST /api/v1/assets/upload` |
| 7 | asset-service | Stores original + publishes `asset.uploaded` | Kafka | Topic: `asset.uploaded` |
| 8 | video-service | Processes video (transcode + segment) | video-service | FFmpeg pipeline |
| 9 | video-service | Publishes `video.transcoded` event | Kafka | Topic: `video.transcoded` |
| 10 | streaming-service | Generates HLS/DASH manifests | streaming-service | Manifest builder |
| 11 | Content Creator | Configures player settings | scene-service | `PATCH /api/v1/scenes/{id}/player` |
| 12 | scene-service | Saves quality, autoplay, loop, volume settings | scene-service | DB update |
| 13 | Content Creator | Adds hotspots on video timeline | scene-service | `POST /api/v1/scenes/{id}/hotspots` |
| 14 | scene-service | Saves hotspots with timestamp + position data | scene-service | DB insert |
| 15 | Content Creator | Publishes the video experience | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 16 | scene-service | Validates + generates embed URL + QR code | scene-service | DB update |
| 17 | scene-service | Returns embed URL + QR code | scene-service | HTTP 200 |
| 18-19 | End User | Opens URL; device detection runs | xr-service | User-Agent parsing |
| 20 | End User | Streams video with hotspot overlays | streaming-service | HLS/DASH stream |

### Postconditions

- 360 video project published with public URL
- Player settings configured (quality presets, autoplay, loop)
- Hotspots attached to video timeline with timestamps
- Embed code and QR code generated
- End users can view on Web, Mobile, VR Headset, or Cardboard
- Device-appropriate rendering path selected

### Error Handling

| Error | Handling |
|-------|----------|
| Video format unsupported | HTTP 400 — "Accepted: mp4, webm, mov for 360 video" |
| Stereoscopic metadata missing | HTTP 400 — "Stereoscopic video requires SBS or TB metadata" |
| Hotspot timestamp out of range | HTTP 400 — "Hotspot timestamp must be within video duration" |
| Video processing timeout | Asset status set to `FAILED`; retry with exponential backoff |
| Streaming manifest generation fails | Retry; fallback to direct MP4 streaming |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `asset.uploaded` | Asset uploaded | `{ assetId, type: "VIDEO_360", stereoMode, duration }` |
| `video.transcoded` | Transcoded | `{ assetId, resolutions, streamUrls }` |
| `scene.published` | Published | `{ sceneId, type: "VIDEO_360", embedUrl, publicUrl }` |

---

## Flow 6: Virtual Tour Creation

### Overview

A VR Designer builds a multi-room virtual tour with floor structure, 360 room photos, navigation paths, hotspots, and floor plans.

### Actors

| Actor | Service Role |
|-------|-------------|
| VR Designer | Tour author |
| Scene Service | Tour graph management |
| Asset Service | Asset library queries |
| GIS Service | Indoor navigation / floor plans |

### Preconditions

- User is authenticated with `VR_DESIGNER` role
- 360 photos available in asset library
- Scene Service operational
- GIS Service operational for floor plan features

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+
| VR Designer  |  |  Scene Service   |  |  Asset Service   |  |   GIS Service    |
|              |  |     (8122)       |  |     (8121)       |  |     (8132)       |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |
       | 1. POST /scenes (type=VR_TOUR)          |                     |
       |------------------>|                     |                     |
       | 2. Create tour record                   |                     |
       |  Return tourId   |                     |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 3. Define building structure            |                     |
       |  POST /tours/{id}/structure             |                     |
       |  { floors: [{ rooms: [...] }] }         |                     |
       |------------------>|                     |                     |
       | 4. Save building graph                  |                     |
       |                   |                     |                     |
       | 5. For each room:                       |                     |
       |  5a. Upload 360 photo                   |                     |
       |--------------------------------------->|                     |
       |  5b. Asset stored, URL returned         |                     |
       |<---------------------------------------|                     |
       |  5c. Assign photo to room               |                     |
       |  PATCH /tours/{id}/rooms/{rid}          |                     |
       |------------------>|                     |                     |
       |  5d. Room linked to 360 asset           |                     |
       |                   |                     |                     |
       | 6. Define navigation paths              |                     |
       |  POST /tours/{id}/navigation            |                     |
       |  { from: roomA, to: roomB, type: "door" }                     |
       |------------------>|                     |                     |
       | 7. Save navigation graph                |                     |
       |                   |                     |                     |
       | 8. Add hotspots per room                |                     |
       |  POST /tours/{id}/rooms/{rid}/hotspots  |                     |
       |------------------>|                     |                     |
       | 9. Save hotspot data                    |                     |
       |                   |                     |                     |
       | 10. Configure tour flow                 |                     |
       |  PATCH /tours/{id}/config               |                     |
       |  (linear / free exploration)            |                     |
       |------------------>|                     |                     |
       | 11. Save tour config                    |                     |
       |                   |                     |                     |
       | 12. Upload floor plan                   |                     |
       |  POST /tours/{id}/floor-plan            |                     |
       |------------------>|                     |                     |
       | 13. Process floor plan + generate indoor nav                  |
       |                   |--------------------------------------->|
       |                   |                     |                     | 14. Parse floor plan
       |                   |                     |                     |  Generate nav mesh
       |                   | 15. Floor plan + nav stored              |
       |<--------------------------------------------------------------------|
       |                   |                     |                     |
       | 16. Preview full tour                   |                     |
       |  GET /tours/{id}/preview                |                     |
       |------------------>|                     |                     |
       | 17. Load full tour graph + room assets + navigation           |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 18. Publish tour                        |                     |
       |  POST /tours/{id}/publish               |                     |
       |------------------>|                     |                     |
       | 19. Validate + publish + generate public URL                  |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 20. End User navigates through rooms                          |
       |  (click navigation arrows / use floor plan)                   |
       |                   |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | VR Designer | Creates new Virtual Tour | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates tour record, returns tourId | scene-service | DB insert |
| 3 | VR Designer | Defines building structure (floors > rooms) | scene-service | `POST /api/v1/tours/{id}/structure` |
| 4 | scene-service | Saves building graph | scene-service | DB update |
| 5a-c | VR Designer | Uploads 360 photos for each room | asset-service | `POST /api/v1/assets/upload` |
| 5d | VR Designer | Assigns photo to room | scene-service | `PATCH /api/v1/tours/{id}/rooms/{rid}` |
| 6 | VR Designer | Defines navigation paths between rooms | scene-service | `POST /api/v1/tours/{id}/navigation` |
| 7 | scene-service | Saves navigation graph (room-to-room connections) | scene-service | DB update |
| 8 | VR Designer | Adds hotspots in each room | scene-service | `POST /api/v1/tours/{id}/rooms/{rid}/hotspots` |
| 9 | scene-service | Saves hotspot data per room | scene-service | DB insert |
| 10 | VR Designer | Configures tour flow (linear / free exploration) | scene-service | `PATCH /api/v1/tours/{id}/config` |
| 11 | scene-service | Saves tour configuration | scene-service | DB update |
| 12 | VR Designer | Uploads floor plan image | scene-service | `POST /api/v1/tours/{id}/floor-plan` |
| 13-14 | GIS Service | Parses floor plan, generates indoor navigation mesh | gis-service | PostGIS spatial processing |
| 15 | scene-service | Stores floor plan + nav data | scene-service | DB + MongoDB update |
| 16 | VR Designer | Previews full tour | scene-service | `GET /api/v1/tours/{id}/preview` |
| 17 | scene-service | Loads full tour graph + assets + navigation | scene-service | HTTP 200 |
| 18 | VR Designer | Publishes tour | scene-service | `POST /api/v1/tours/{id}/publish` |
| 19 | scene-service | Validates completeness + publishes | scene-service | DB update + Kafka event |
| 20 | End User | Navigates through rooms using arrows or floor plan | xr-service | WebXR / browser rendering |

### Postconditions

- Virtual Tour published with unique public URL
- All rooms linked to 360 photos
- Navigation paths defined between rooms
- Hotspots attached to rooms
- Floor plan with indoor navigation available
- Tour flow configured (linear or free exploration)

### Error Handling

| Error | Handling |
|-------|----------|
| No rooms defined | HTTP 400 — "Tour must have at least one room" |
| Room missing 360 photo | HTTP 400 — "Each room must have a 360 photo assigned" |
| No navigation paths | HTTP 400 — "Tour must have navigation paths between rooms" |
| Floor plan upload fails | Warning; tour published without floor plan |
| GIS service unavailable | Floor plan features disabled; core tour still publishable |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.published` | Tour published | `{ sceneId, type: "VR_TOUR", roomCount, publicUrl }` |

---

## Flow 7: Showroom Builder

### Overview

A VR Designer builds a product showroom with 3D product models, interactive hotspots, CTA buttons, and a product catalog for e-commerce integration.

### Actors

| Actor | Service Role |
|-------|-------------|
| VR Designer | Showroom author |
| Scene Service | Showroom graph management |
| Asset Service | 3D model library |
| End User | Showroom visitor |

### Preconditions

- User is authenticated with `VR_DESIGNER` role
- 3D product models available in asset library
- Scene Service operational

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+
| VR Designer  |  |  Scene Service   |  |  Asset Service   |  |    End User      |
|              |  |     (8122)       |  |     (8121)       |  |                  |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |
       | 1. POST /scenes (type=SHOWROOM)         |                     |
       |------------------>|                     |                     |
       | 2. Create showroom record                |                     |
       |  Return showroomId|                     |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 3. Select template or blank             |                     |
       |  PATCH /scenes/{id}/template            |                     |
       |------------------>|                     |                     |
       | 4. Load template scene graph            |                     |
       |  (or empty graph)  |                     |                     |
       |                   |                     |                     |
       | 5. GET /assets?type=MODEL_3D            |                     |
       |--------------------------------------->|                     |
       | 6. Return 3D product models             |                     |
       |<---------------------------------------|                     |
       |                   |                     |                     |
       | 7. Add products to showroom             |                     |
       |  POST /scenes/{id}/nodes                |                     |
       |------------------>|                     |                     |
       | 8. Add model nodes with transforms      |                     |
       |                   |                     |                     |
       | 9. Add product hotspots with info panels|                     |
       |  POST /scenes/{id}/hotspots             |                     |
       |------------------>|                     |                     |
       | 10. Save hotspot data (title, description, price)            |
       |                   |                     |                     |
       | 11. Configure CTA buttons               |                     |
       |  POST /scenes/{id}/ctas                 |                     |
       |  (Buy Now, Contact, Learn More)         |                     |
       |------------------>|                     |                     |
       | 12. Save CTA configs (url, action, style)                    |
       |                   |                     |                     |
       | 13. Set up product catalog              |                     |
       |  POST /scenes/{id}/catalog              |                     |
       |------------------>|                     |                     |
       | 14. Save catalog (products, categories, pricing)             |
       |                   |                     |                     |
       | 15. Configure lighting + materials      |                     |
       |  PATCH /scenes/{id}/environment         |                     |
       |------------------>|                     |                     |
       | 16. Save environment config              |                     |
       |                   |                     |                     |
       | 17. Preview showroom                    |                     |
       |  GET /scenes/{id}/preview               |                     |
       |------------------>|                     |                     |
       | 18. Return full scene graph              |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 19. Publish                             |                     |
       |  POST /scenes/{id}/publish              |                     |
       |------------------>|                     |                     |
       | 20. Validate + publish + generate URL   |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       |                   |                     |     21. End User opens showroom URL
       |                   |                     |                     |
       |                   |                     |     22. Browses products in VR
       |                   |                     |                     |
       |                   |                     |     23. Clicks CTA -> opens product page
       |                   |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | VR Designer | Creates Showroom project | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates showroom record, returns ID | scene-service | DB insert |
| 3 | VR Designer | Selects template or blank canvas | scene-service | `PATCH /api/v1/scenes/{id}/template` |
| 4 | scene-service | Loads template scene graph (or empty) | scene-service | Template lookup |
| 5 | VR Designer | Browses 3D product models | asset-service | `GET /api/v1/assets?type=MODEL_3D` |
| 6 | asset-service | Returns 3D model list | asset-service | HTTP 200 |
| 7 | VR Designer | Adds product models to showroom | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 8 | scene-service | Adds model nodes with position/rotation/scale | scene-service | Graph upsert |
| 9 | VR Designer | Adds product hotspots with info panels | scene-service | `POST /api/v1/scenes/{id}/hotspots` |
| 10 | scene-service | Saves hotspot data (title, description, price) | scene-service | DB insert |
| 11 | VR Designer | Configures CTA buttons | scene-service | `POST /api/v1/scenes/{id}/ctas` |
| 12 | scene-service | Saves CTA configs (url, action, style) | scene-service | DB insert |
| 13 | VR Designer | Sets up product catalog | scene-service | `POST /api/v1/scenes/{id}/catalog` |
| 14 | scene-service | Saves catalog data | scene-service | DB insert |
| 15 | VR Designer | Configures lighting and materials | scene-service | `PATCH /api/v1/scenes/{id}/environment` |
| 16 | scene-service | Saves environment config | scene-service | DB update |
| 17 | VR Designer | Previews showroom | scene-service | `GET /api/v1/scenes/{id}/preview` |
| 18 | scene-service | Returns full scene graph | scene-service | HTTP 200 |
| 19 | VR Designer | Publishes showroom | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 20 | scene-service | Validates + publishes + generates URL | scene-service | DB update + Kafka |
| 21-23 | End User | Opens URL, browses products, clicks CTAs | xr-service | WebXR / browser |

### Postconditions

- Showroom published with public URL
- 3D product models placed in scene with transforms
- Product hotspots with info panels attached
- CTA buttons configured (Buy Now, Contact, Learn More)
- Product catalog with pricing data stored
- Lighting and materials configured
- End users can browse products in VR and click CTAs

### Error Handling

| Error | Handling |
|-------|----------|
| No products placed | HTTP 400 — "Showroom must have at least one product" |
| CTA URL invalid | HTTP 400 — "CTA URL must be a valid URL" |
| Template not found | HTTP 404 — "Template not found"; fallback to blank |
| Catalog product mismatch | Warning; missing products highlighted in editor |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.published` | Showroom published | `{ sceneId, type: "SHOWROOM", productCount, publicUrl }` |

---

## Flow 8: AR Experience

### Overview

An AR Designer creates an AR experience with marker-based or markerless tracking, places 3D content, configures interactions, and publishes for end users.

### Actors

| Actor | Service Role |
|-------|-------------|
| AR Designer | AR experience author |
| Scene Service | AR scene management |
| Asset Service | 3D model library |
| End User | AR viewer on mobile device |

### Preconditions

- User is authenticated with `AR_DESIGNER` role
- 3D models available in asset library
- Mobile device with camera access (for end user)
- Scene Service operational

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+
| AR Designer  |  |  Scene Service   |  |  Asset Service   |  |    End User      |
|              |  |     (8122)       |  |     (8121)       |  |    (Mobile)      |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |
       | 1. POST /scenes (type=AR_EXPERIENCE)    |                     |
       |------------------>|                     |                     |
       | 2. Create AR scene record               |                     |
       |  Return sceneId  |                     |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       | 3. Select AR type                       |                     |
       |  PATCH /scenes/{id}/ar-config           |                     |
       |  (marker / markerless / image-tracking / surface)            |
       |------------------>|                     |                     |
       | 4. Save AR tracking config              |                     |
       |                   |                     |                     |
       | 5. Upload marker/target image           |                     |
       |  POST /scenes/{id}/ar-marker            |                     |
       |------------------>|                     |                     |
       | 6. Process + store marker image         |                     |
       |                   |                     |                     |
       | 7. GET /assets?type=MODEL_3D            |                     |
       |--------------------------------------->|                     |
       | 8. Return 3D models                    |                     |
       |<---------------------------------------|                     |
       |                   |                     |                     |
       | 9. Place 3D content in AR scene         |                     |
       |  POST /scenes/{id}/nodes                |                     |
       |------------------>|                     |                     |
       | 10. Add 3D model nodes with AR transforms                   |
       |                   |                     |                     |
       | 11. Configure interactions              |                     |
       |  POST /scenes/{id}/interactions         |                     |
       |  (tap, drag, rotate, scale, animate)    |                     |
       |------------------>|                     |                     |
       | 12. Save interaction configs             |                     |
       |                   |                     |                     |
       | 13. Publish AR experience               |                     |
       |  POST /scenes/{id}/publish              |                     |
       |------------------>|                     |                     |
       | 14. Validate + publish + generate AR URL                     |
       |  (deep link for mobile)                 |                     |
       |<------------------|                     |                     |
       |                   |                     |                     |
       |                   |                     |     15. End User opens AR URL on mobile
       |                   |                     |                     |
       |                   |                     |     16. Camera permission requested
       |                   |                     |                     |
       |                   |                     |     17. Scan marker / point at surface
       |                   |                     |                     |
       |                   |                     |     18. 3D content appears in real world
       |                   |                     |                     |
       |                   |                     |     19. End User interacts (tap, drag, rotate)
       |                   |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | AR Designer | Creates AR Experience | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates AR scene record, returns ID | scene-service | DB insert |
| 3 | AR Designer | Selects AR type (marker/markerless/image-tracking/surface) | scene-service | `PATCH /api/v1/scenes/{id}/ar-config` |
| 4 | scene-service | Saves AR tracking configuration | scene-service | DB update |
| 5 | AR Designer | Uploads marker or target image | scene-service | `POST /api/v1/scenes/{id}/ar-marker` |
| 6 | scene-service | Processes and stores marker image | scene-service | Image processing |
| 7 | AR Designer | Browses 3D models from asset library | asset-service | `GET /api/v1/assets?type=MODEL_3D` |
| 8 | asset-service | Returns 3D model list | asset-service | HTTP 200 |
| 9 | AR Designer | Places 3D content in AR scene | scene-service | `POST /api/v1/scenes/{id}/nodes` |
| 10 | scene-service | Adds 3D model nodes with AR transforms | scene-service | Graph upsert |
| 11 | AR Designer | Configures interactions (tap, drag, rotate) | scene-service | `POST /api/v1/scenes/{id}/interactions` |
| 12 | scene-service | Saves interaction configurations | scene-service | DB insert |
| 13 | AR Designer | Publishes AR experience | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 14 | scene-service | Validates + publishes + generates deep link URL | scene-service | DB update + Kafka |
| 15-19 | End User | Opens URL on mobile, scans marker, interacts | xr-service | AR.js / WebXR |

### Postconditions

- AR Experience published with deep link URL
- Marker/target image processed and stored
- 3D content placed with AR-specific transforms
- Interactions configured (tap, drag, rotate, scale)
- End users can view 3D content overlaid on real world via mobile camera

### Error Handling

| Error | Handling |
|-------|----------|
| No marker image for marker-based | HTTP 400 — "Marker image required for marker-based AR" |
| Image too large for marker | HTTP 400 — "Marker image must be under 5MB" |
| No 3D content placed | HTTP 400 — "AR scene must have at least one 3D object" |
| Camera permission denied | Graceful fallback with instructions to enable camera |
| Device not AR-capable | HTTP 400 — "Device does not support AR"; show fallback view |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.published` | AR published | `{ sceneId, type: "AR_EXPERIENCE", arType, publicUrl }` |

---

## Flow 9: Multi-user VR Collaboration

### Overview

A Host creates a collaboration room, shares it with participants, and all users interact in real-time with synchronized positions, voice, and shared object manipulation.

### Actors

| Actor | Service Role |
|-------|-------------|
| Host User | Room creator and moderator |
| Collaboration Service | Real-time sync engine |
| Participants | Room joiners |
| Analytics Service | Session tracking |

### Preconditions

- Host is authenticated
- Collaboration Service operational (WebSocket server running)
- Scene published and accessible
- WebRTC signaling server available for voice

### Swimlane Diagram

```
+----------+  +------------------+  +------------------+  +------------------+  +----------+
|   Host   |  |  Collaboration   |  | Analytics Svc    |  |Notif Service     |  |Participant|
|          |  |    Service       |  |     (8127)       |  |     (8118)       |  |          |
+----+-----+  +--------+---------+  +--------+---------+  +--------+---------+  +----+-----+
     |                 |                     |                     |                 |
     | 1. POST /collab/rooms                |                     |                 |
     |  (sceneId, maxParticipants)          |                     |                 |
     |---------------->|                     |                     |                 |
     |                 | 2. Create room record                     |                 |
     |                 |  Generate roomCode + roomLink             |                 |
     |                 |                     |                     |                 |
     | 3. Return roomCode + roomLink        |                     |                 |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
     | 4. Share roomCode/link               |                     |                 |
     |--------------------------------------------------------------->|
     |                 |                     |                     |                 |
     |                 |                     |                     |  5. Participant joins room
     |                 |                     |                     |                 |
     |                 | 6. POST /collab/rooms/{code}/join         |                 |
     |                 |<------------------------------------------------------------|
     |                 |                     |                     |                 |
     |                 | 7. Validate room capacity                 |                 |
     |                 |  Create participant record                |                 |
     |                 |                     |                     |                 |
     |                 | 8. Broadcast user.joined event            |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     |                     |                 |
     |                 | 9. Notify host: new participant           |                 |
     |                 |-------------------------->|               |                 |
     |                 |                     |                     |                 |
     |                 | 10. Return room + scene data              |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
     | 11. Host selects avatar config       |                     |                 |
     |  WebSocket------>|                   |                     |                 |
     |                 |                     |                     |                 |
     |                 | 12. Broadcast avatar config to all        |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
     |                 | 13. Real-time sync loop (WebSocket)       |                 |
     |                 |  - Position updates (x, y, z)            |                 |
     |                 |  - Rotation updates (qx,qy,qz,qw)       |                 |
     |                 |  - Voice data (WebRTC)                   |                 |
     |  WS<---------->|<---------------------------------------->|                 |
     |                 |                     |                     |                 |
     | 14. Host shares screen in VR         |                     |                 |
     |  WebSocket------>|                   |                     |                 |
     |                 | 15. Stream screen data to all             |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
     | 16. Participants manipulate objects  |                     |                 |
     |  WebSocket------>|                   |                     |                 |
     |                 | 17. Sync object state to all              |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
     | 18. Host ends session                |                     |                 |
     |  WebSocket------>|                   |                     |                 |
     |                 | 19. Record session analytics              |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     | 20. Save: duration, participants, gaze data, interactions
     |                 |                     |                     |                 |
     |                 | 21. Notify all: session ended             |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Host | Creates collaboration room | collaboration-service | `POST /api/v1/collab/rooms` |
| 2 | collaboration-service | Creates room, generates roomCode + roomLink | collaboration-service | DB insert |
| 3 | collaboration-service | Returns room details | collaboration-service | HTTP 201 |
| 4 | Host | Shares room code/link with participants | notification-service | Copy link / send invite |
| 5-6 | Participant | Joins room via link/code | collaboration-service | `POST /api/v1/collab/rooms/{code}/join` |
| 7 | collaboration-service | Validates capacity, creates participant record | collaboration-service | DB insert |
| 8 | collaboration-service | Publishes `user.joined` event | Kafka | Topic: `user.joined` |
| 9 | notification-service | Notifies host of new participant | notification-service | WebSocket push |
| 10 | collaboration-service | Returns room + scene data to participant | collaboration-service | HTTP 200 |
| 11-12 | Participants | Select/configure avatar | collaboration-service | WebSocket message |
| 13 | All Users | Real-time sync loop via WebSocket | collaboration-service | WS: position, rotation, voice |
| 14-15 | Host | Shares screen in VR | collaboration-service | WebSocket: screen stream |
| 16-17 | Participants | Manipulate objects together | collaboration-service | WebSocket: object state sync |
| 18 | Host | Ends session | collaboration-service | WebSocket: end session |
| 19-20 | collaboration-service | Records session analytics | analytics-service | `POST /api/v1/analytics/sessions` |
| 21 | collaboration-service | Notifies all users: session ended | notification-service | WebSocket: disconnect |

### Postconditions

- Collaboration room created with unique code/link
- All participants connected via WebSocket
- Real-time position, rotation, voice sync active
- Screen sharing available (host)
- Object manipulation synced across all participants
- Session analytics recorded (duration, participants, gaze, interactions)

### Error Handling

| Error | Handling |
|-------|----------|
| Room capacity exceeded | HTTP 409 — "Room is full (max participants reached)" |
| WebSocket disconnect | Auto-reconnect with 5s timeout; participant marked as "reconnecting" |
| Voice chat fails | Fallback to text chat; WebRTC retry |
| Host disconnects | Host transfer to next participant; or session ends if < 2 participants |
| Room idle > 30 minutes | Auto-close room; analytics recorded |
| Object manipulation conflict | Last-write-wins with conflict notification to all users |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `user.joined` | User joined room | `{ roomId, userId, participantCount }` |
| `user.joined` | User left room | `{ roomId, userId, participantCount }` |
| `analytics.event` | Session recorded | `{ sessionId, duration, participants, gazeData }` |

---

## Flow 10: AI Scene Generation

### Overview

A Content Creator provides a text description and the AI Service generates a complete VR scene with floor plan, 3D assets, hotspots, and narration.

### Actors

| Actor | Service Role |
|-------|-------------|
| Content Creator | Provides text description |
| AI Service | Scene generation engine |
| Scene Service | Scene graph management |
| Asset Service | 3D asset library for AI selection |

### Preconditions

- User is authenticated with `CONTENT_CREATOR` role
- AI Service operational with LLM integration
- 3D asset library populated
- Scene Service operational

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
|   Content    |  |   AI Service     |  |  Scene Service   |  |  Asset Service   |  |     MongoDB      |
|   Creator    |  |     (8126)       |  |     (8122)       |  |     (8121)       |  |                  |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. POST /ai/generate-scene              |                     |                     |
       |  { description: "Modern art museum      |                     |                     |
       |    with 5 rooms, minimalist" }          |                     |                     |
       |------------------>|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 2. Parse intent with LLM                  |                     |
       |                   |  - sceneType: VR_MUSEUM                   |                     |
       |                   |  - rooms: 5                               |                     |
       |                   |  - style: minimalist                      |                     |
       |                   |  - exhibits: art pieces                   |                     |
       |                   |                     |                     |                     |
       |                   | 3. Generate floor plan layout             |                     |
       |                   |  (room dimensions, connections)           |                     |
       |                   |                     |                     |                     |
       |                   | 4. Query asset library for matches        |                     |
       |                   |--------------------------------------->|                     |
       |                   | 5. Return matching 3D assets              |                     |
       |                   |<---------------------------------------|                     |
       |                   |                     |                     |                     |
       |                   | 6. Select + arrange 3D assets per room    |                     |
       |                   |  (AI placement algorithm)                 |                     |
       |                   |                     |                     |                     |
       |                   | 7. Generate hotspot placements            |                     |
       |                   |  (info panels for each exhibit)          |                     |
       |                   |                     |                     |                     |
       |                   | 8. Generate narration script              |                     |
       |                   |  (LLM-generated room descriptions)       |                     |
       |                   |                     |                     |                     |
       |                   | 9. Assemble scene graph                   |                     |
       |                   |  { nodes: [...], edges: [...] }           |                     |
       |                   |                     |                     |                     |
       |                   | 10. Create scene + store graph            |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     | 11. Store in MongoDB                     |
       |                   |                     |--------------------------------------->|
       |                   |                     |                     |                     |
       |                   | 12. Publish ai.generation.complete        |                     |
       |                   |-------------------->|                     |                     |
       |                   |                     |                     |                     |
       | 13. Notification: AI scene ready        |                     |                     |
       |<---------------------------------------|                     |                     |
       |                   |                     |                     |                     |
       | 14. Open generated scene in editor      |                     |                     |
       |  GET /scenes/{id}/editor               |                     |                     |
       |------------------------>|               |                     |                     |
       | 15. Return scene graph  |               |                     |                     |
       |<------------------------|               |                     |                     |
       |                   |                     |                     |                     |
       | 16. User reviews + edits generated scene                      |
       |  (modify nodes, add/remove objects)                           |
       |                   |                     |                     |                     |
       | 17. Publish                             |                     |                     |
       |  POST /scenes/{id}/publish              |                     |                     |
       |------------------------>|               |                     |                     |
       | 18. Validate + publish |               |                     |                     |
       |<------------------------|               |                     |                     |
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Content Creator | Submits text description | ai-service | `POST /api/v1/ai/generate-scene` |
| 2 | ai-service | Parses intent with LLM (scene type, rooms, style, exhibits) | ai-service | LLM API call |
| 3 | ai-service | Generates floor plan layout (room dimensions, connections) | ai-service | AI algorithm |
| 4 | ai-service | Queries asset library for matching 3D assets | asset-service | `GET /api/v1/assets?type=MODEL_3D` |
| 5 | asset-service | Returns matching 3D assets | asset-service | HTTP 200 |
| 6 | ai-service | Selects and arranges 3D assets per room | ai-service | Placement algorithm |
| 7 | ai-service | Generates hotspot placements (info panels) | ai-service | AI generation |
| 8 | ai-service | Generates narration script via LLM | ai-service | LLM API call |
| 9 | ai-service | Assembles complete scene graph | ai-service | Graph assembly |
| 10 | ai-service | Creates scene record + stores graph | scene-service | `POST /api/v1/scenes` |
| 11 | scene-service | Stores scene graph in MongoDB | MongoDB | Document insert |
| 12 | ai-service | Publishes `ai.generation.complete` event | Kafka | Topic: `ai.generation.complete` |
| 13 | scene-service | Sends notification: AI scene ready | notification-service | WebSocket push |
| 14-15 | Content Creator | Opens generated scene in editor | scene-service | `GET /api/v1/scenes/{id}/editor` |
| 16 | Content Creator | Reviews and edits generated scene | scene-service | Scene Builder UI |
| 17-18 | Content Creator | Publishes final scene | scene-service | `POST /api/v1/scenes/{id}/publish` |

### Postconditions

- AI-generated scene created with complete scene graph
- Floor plan with room layout generated
- 3D assets placed in appropriate rooms
- Hotspots with info panels generated
- Narration script created
- Scene editable in Scene Builder before publishing

### Error Handling

| Error | Handling |
|-------|----------|
| Description too vague | HTTP 400 — "Please provide more detail (type, rooms, style)" |
| LLM service unavailable | HTTP 503 — "AI service temporarily unavailable; try again" |
| No matching 3D assets found | AI generates scene without 3D models; user notified |
| Generation timeout | Job queued; user notified when complete (> 60s) |
| Generated scene invalid | AI retries generation up to 2 times; fallback to template |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `ai.generation.complete` | AI scene generated | `{ sceneId, tenantId, userId, rooms, assetCount, generationTime }` |

---

## Flow 11: VR Training (LMS Integration)

### Overview

A Training Admin creates VR training scenes with modules, quizzes, and completion criteria, then integrates with an LMS via xAPI/SCORM for tracking learner progress.

### Actors

| Actor | Service Role |
|-------|-------------|
| Training Admin | Training content author |
| Scene Service | Scene and training module management |
| LMS | Learning Management System (external) |
| Learner | Training participant |
| Analytics Service | Progress and completion tracking |

### Preconditions

- User is authenticated with `VR_DESIGNER` role (training admin)
- LMS endpoint configured (xAPI/SCORM endpoint URL)
- Scene Service operational
- Analytics Service operational

### Swimlane Diagram

```
+----------+  +------------------+  +------------------+  +------------------+  +----------+
|Training  |  |  Scene Service   |  | Analytics Svc    |  |  LMS (Ext)       |  | Learner  |
|  Admin   |  |     (8122)       |  |     (8127)       |  |  xAPI/SCORM      |  |          |
+----+-----+  +--------+---------+  +--------+---------+  +--------+---------+  +----+-----+
     |                 |                     |                     |                 |
     | 1. POST /scenes (type=VR_TRAINING)    |                     |                 |
     |---------------->|                     |                     |                 |
     | 2. Create training scene record       |                     |                 |
     |  Return sceneId |                     |                     |                 |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
     | 3. Add training modules               |                     |                 |
     |  POST /scenes/{id}/modules            |                     |                 |
     |  (rooms/steps with content)           |                     |                 |
     |---------------->|                     |                     |                 |
     | 4. Save module definitions            |                     |                 |
     |                 |                     |                     |                 |
     | 5. Add quiz hotspots                  |                     |                 |
     |  POST /scenes/{id}/quizzes            |                     |                 |
     |  (questions, answers, points)         |                     |                 |
     |---------------->|                     |                     |                 |
     | 6. Save quiz data                     |                     |                 |
     |                 |                     |                     |                 |
     | 7. Configure completion criteria      |                     |                 |
     |  PATCH /scenes/{id}/training-config   |                     |                 |
     |  (minScore, requiredModules, timeLimit)                     |                 |
     |---------------->|                     |                     |                 |
     | 8. Save training config               |                     |                 |
     |                 |                     |                     |                 |
     | 9. Link to LMS                       |                     |                 |
     |  POST /scenes/{id}/lms-link           |                     |                 |
     |  { endpoint, auth, format: xAPI }     |                     |                 |
     |---------------->|                     |                     |                 |
     | 10. Store LMS config + test connection|                     |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     | 11. Connection OK |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
     | 12. Publish training scene            |                     |                 |
     |  POST /scenes/{id}/publish            |                     |                 |
     |---------------->|                     |                     |                 |
     | 13. Validate + publish                |                     |                 |
     |                 |                     |                     |                 |
     | 14. Share training link with learner  |                     |                 |
     |--------------------------------------------------------------->|
     |                 |                     |                     |                 |
     |                 |                     |                     | 15. Learner starts training
     |                 |                     |                     |                 |
     |                 | 16. GET /scenes/{id}/training             |                 |
     |                 |<------------------------------------------------------------|
     |                 |                     |                     |                 |
     |                 | 17. Return training modules + scene        |                 |
     |                 |---------------------------------------------------------->|
     |                 |                     |                     |                 |
     |                 | 18. Learner progresses through modules     |                 |
     |                 |  WebSocket------>|   |                     |                 |
     |                 |                     | 19. Record module progress              |
     |                 |                     |                     |                 |
     |                 | 20. Learner answers quiz                   |                 |
     |                 |  WebSocket------>|   |                     |                 |
     |                 |                     | 21. Score quiz + record result          |
     |                 |                     |                     |                 |
     |                 | 22. Send xAPI statement to LMS             |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     | 23. LMS records progress
     |                 |                     |                     |                 |
     |                 | 24. Training complete (all modules done + minScore met)   |
     |                 |  WebSocket------>|   |                     |                 |
     |                 |                     |                     |                 |
     |                 | 25. Publish training.completed event        |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     |                     |                 |
     |                 | 26. Send completion xAPI to LMS            |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     | 27. LMS marks complete
     |                 |                     |                     |                 |
     | 28. View training analytics           |                     |                 |
     |  GET /scenes/{id}/training/analytics  |                     |                 |
     |---------------->|                     |                     |                 |
     | 29. Return analytics (completion rate, avg score, time spent)               |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Training Admin | Creates VR Training scene | scene-service | `POST /api/v1/scenes` |
| 2 | scene-service | Creates training record, returns ID | scene-service | DB insert |
| 3 | Training Admin | Adds training modules (rooms/steps) | scene-service | `POST /api/v1/scenes/{id}/modules` |
| 4 | scene-service | Saves module definitions | scene-service | DB insert |
| 5 | Training Admin | Adds quiz hotspots with questions | scene-service | `POST /api/v1/scenes/{id}/quizzes` |
| 6 | scene-service | Saves quiz data (questions, answers, points) | scene-service | DB insert |
| 7 | Training Admin | Configures completion criteria | scene-service | `PATCH /api/v1/scenes/{id}/training-config` |
| 8 | scene-service | Saves training configuration | scene-service | DB update |
| 9 | Training Admin | Links to LMS via xAPI/SCORM | scene-service | `POST /api/v1/scenes/{id}/lms-link` |
| 10 | scene-service | Stores LMS config + tests connection | scene-service | HTTP health check |
| 11 | LMS | Confirms connection | LMS | HTTP 200 |
| 12-13 | Training Admin | Publishes training scene | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 14 | Training Admin | Shares training link with learner | notification-service | Email / LMS assignment |
| 15-17 | Learner | Starts training, loads modules | scene-service | `GET /api/v1/scenes/{id}/training` |
| 18-19 | Learner | Progresses through modules | analytics-service | Module progress tracking |
| 20-21 | Learner | Answers quizzes | analytics-service | Quiz scoring |
| 22-23 | analytics-service | Sends xAPI statement to LMS | LMS | xAPI POST |
| 24-25 | analytics-service | Training completed event | Kafka | `training.completed` |
| 26-27 | analytics-service | Sends completion xAPI to LMS | LMS | xAPI POST |
| 28-29 | Training Admin | Views training analytics | analytics-service | `GET /api/v1/scenes/{id}/training/analytics` |

### Postconditions

- VR Training scene published with modules and quizzes
- LMS linked via xAPI/SCORM
- Learner progress tracked in real-time
- Quiz responses scored and recorded
- Completion status sent to LMS
- Training analytics available (completion rate, avg score, time spent)

### Error Handling

| Error | Handling |
|-------|----------|
| No training modules defined | HTTP 400 — "Training must have at least one module" |
| No quizzes defined | Warning; training publishable without quizzes |
| LMS connection fails | HTTP 400 — "Cannot connect to LMS endpoint"; retry |
| xAPI statement fails | Queued for retry; analytics preserved locally |
| Learner exceeds time limit | Training paused; completion not recorded |
| Quiz score below minimum | Completion status = "INCOMPLETE"; allow retry |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.published` | Training published | `{ sceneId, type: "VR_TRAINING", moduleCount, quizCount }` |
| `training.completed` | Training completed | `{ sceneId, userId, score, completionTime, passed }` |

---

## Flow 12: Digital Twin Sync

### Overview

IoT sensors stream data to the Digital Twin Service which maintains real-time state. The XR Service reflects these changes in VR scenes, enabling live data overlays and anomaly alerts.

### Actors

| Actor | Service Role |
|-------|-------------|
| IoT Sensors | Data source devices |
| Digital Twin Service | State management and sync |
| Kafka | Event transport |
| XR Service | VR scene rendering |
| End User | VR viewer |

### Preconditions

- IoT endpoints registered in Digital Twin Service
- Kafka topics provisioned
- XR Service connected to Digital Twin Service
- End user viewing a scene linked to a digital twin

### Swimlane Diagram

```
+----------+  +------------------+  +------------------+  +------------------+  +----------+
|IoT Sensors|  |  Digital Twin    |  |      Kafka       |  |   XR Service     |  | End User |
|          |  |    Service       |  |                  |  |     (8125)       |  |   (VR)   |
+----+-----+  +--------+---------+  +--------+---------+  +--------+---------+  +----+-----+
     |                 |                     |                     |                 |
     | 1. Register IoT endpoints             |                     |                 |
     |  POST /twins/{id}/endpoints           |                     |                 |
     |---------------->|                     |                     |                 |
     | 2. Validate + store endpoint config   |                     |                 |
     |                 |                     |                     |                 |
     | 3. IoT sensor sends telemetry data    |                     |                 |
     |  (temperature, humidity, pressure)    |                     |                 |
     |---------------->|                     |                     |                 |
     |                 |                     |                     |                 |
     |                 | 4. Validate + normalize data              |                 |
     |                 |                     |                     |                 |
     |                 | 5. Update twin state in DB               |                 |
     |                 |  (current values + timestamp)            |                 |
     |                 |                     |                     |                 |
     |                 | 6. Publish iot.data.received event       |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     |                     |                 |
     |                 |                     | 7. Route to XR Service subscribers    |
     |                 |                     |-------------------->|                 |
     |                 |                     |                     |                 |
     |                 |                     | 8. Check anomaly rules               |
     |                 |                     |  (threshold exceeded?)               |
     |                 |                     |                     |                 |
     |                 |                     | 9a. Normal: update VR data overlay   |
     |                 |                     |------------------------------------>|
     |                 |                     |                     | 10. VR scene shows live data
     |                 |                     |                     |  (gauges, charts, values)
     |                 |                     |                     |                 |
     |                 |                     | 9b. Anomaly: trigger alert           |
     |                 |                     |-------------------->|                 |
     |                 |                     |                     | 11. VR alert displayed
     |                 |                     |                     |  (flashing indicator, sound)
     |                 |                     |                     |                 |
     |                 |                     | 12. Analytics recorded               |
     |                 |                     |-------------------->|                 |
     |                 |                     |                     |                 |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | IoT Admin | Registers IoT endpoints | digital-twin-service | `POST /api/v1/twins/{id}/endpoints` |
| 2 | digital-twin-service | Validates + stores endpoint config | digital-twin-service | DB insert |
| 3 | IoT Sensors | Sends telemetry data | digital-twin-service | HTTP/MQTT/Kafka producer |
| 4 | digital-twin-service | Validates + normalizes incoming data | digital-twin-service | Schema validation |
| 5 | digital-twin-service | Updates twin state in DB | digital-twin-service | DB upsert |
| 6 | digital-twin-service | Publishes `iot.data.received` event | Kafka | Topic: `iot.data.received` |
| 7 | Kafka | Routes to XR Service subscribers | Kafka | Consumer group |
| 8 | xr-service | Checks anomaly rules (thresholds) | xr-service | Rule engine |
| 9a | xr-service | Updates VR data overlay (normal values) | xr-service | WebSocket push |
| 10 | End User | Views live data in VR (gauges, charts) | xr-service | VR rendering |
| 9b | xr-service | Triggers alert (threshold exceeded) | xr-service | Alert dispatch |
| 11 | End User | Sees VR alert (flashing, sound) | xr-service | VR alert overlay |
| 12 | analytics-service | Records IoT data + anomaly events | analytics-service | DB insert |

### Postconditions

- IoT endpoints registered and streaming data
- Digital Twin state maintained in real-time
- VR scene reflects live IoT data
- Anomaly alerts displayed in VR when thresholds exceeded
- Analytics recorded for all IoT data points

### Error Handling

| Error | Handling |
|-------|----------|
| IoT sensor offline | Endpoint marked `OFFLINE`; last-known state preserved |
| Data format invalid | Rejected with 400; logged for sensor maintenance |
| Kafka consumer lag | XR data updates delayed; alert if lag > 5s |
| Twin state DB unavailable | In-memory cache used; DB sync on recovery |
| Anomaly threshold misconfigured | Warning logged; default thresholds applied |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `iot.data.received` | IoT data received | `{ twinId, endpointId, metrics: {...}, timestamp }` |
| `analytics.event` | Anomaly detected | `{ twinId, metric, value, threshold, severity }` |

---

## Flow 13: Subscription & Billing

### Overview

A Tenant Admin selects a subscription plan, processes payment, and the system enforces feature limits, meters usage, and generates invoices.

### Actors

| Actor | Service Role |
|-------|-------------|
| Tenant Admin | Subscription manager |
| Billing Service | Subscription and invoice management |
| Payment Gateway | External payment processor (Stripe) |
| Tenant Service | Feature flag enforcement |
| Notification Service | Invoice and alert delivery |

### Preconditions

- User is authenticated with `TENANT_ADMIN` role
- Payment Gateway configured (Stripe API keys)
- Billing Service operational
- Stripe webhook endpoint configured

### Swimlane Diagram

```
+----------+  +------------------+  +------------------+  +------------------+  +------------------+
| Tenant   |  | Billing Service  |  |  Payment GW      |  | Tenant Service   |  |Notif Service     |
|  Admin   |  |     (8128)       |  |   (Stripe)       |  |     (8120)       |  |     (8118)       |
+----+-----+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
     |                 |                     |                     |                 |
     | 1. GET /billing/plans                |                     |                 |
     |---------------->|                     |                     |                 |
     | 2. Return available plans             |                     |                 |
     |  (Free/Starter/Pro/Enterprise)        |                     |                 |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
     | 3. Select plan + payment method       |                     |                 |
     |  POST /billing/subscriptions          |                     |                 |
     |  { planId, paymentMethodId }          |                     |                 |
     |---------------->|                     |                     |                 |
     |                 |                     |                     |                 |
     |                 | 4. Create Stripe subscription            |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     |                     |                 |
     |                 | 5. Payment processed  |                     |                 |
     |                 |<--------------------|                     |                 |
     |                 |                     |                     |                 |
     |                 | 6. Create subscription record            |                 |
     |                 |  (status=ACTIVE)     |                     |                 |
     |                 |                     |                     |                 |
     |                 | 7. Activate plan features                |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     |                 |
     |                 | 8. Tenant features updated:              |                 |
     |                 |  - maxStorage, maxBandwidth              |                 |
     |                 |  - maxScenes, maxUsers                   |                 |
     |                 |  - featureFlags (VR, AR, AI)            |                 |
     |                 |                     |                     |                 |
     | 9. Subscription confirmed             |                     |                 |
     |<----------------|                     |                     |                 |
     |                 |                     |                     |                 |
     |                 | ============ ONGOING USAGE ============= |                 |
     |                 |                     |                     |                 |
     |                 | 10. Meter usage (storage, bandwidth, scenes)              |
     |                 |  (hourly job)       |                     |                 |
     |                 |                     |                     |                 |
     |                 | 11. Check against plan limits            |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     | 12. Enforce limits|
     |                 |                     |                     |  (block if over) |
     |                 |                     |                     |                 |
     |                 | 13. Track overage charges                |                 |
     |                 |                     |                     |                 |
     |                 | ============ MONTHLY INVOICE =========== |                 |
     |                 |                     |                     |                 |
     |                 | 14. Generate monthly invoice              |                 |
     |                 |  (base plan + overage)                    |                 |
     |                 |                     |                     |                 |
     |                 | 15. Charge via Stripe                    |                 |
     |                 |-------------------->|                     |                 |
     |                 |                     |                     |                 |
     |                 | 16. Send invoice to tenant admin          |                 |
     |                 |------------------------------------------------>|
     |                 |                     |                     |                 |
     |                 | ============ RENEWAL / CANCEL ===========|                 |
     |                 |                     |                     |                 |
     | 17. Cancel subscription (optional)     |                     |                 |
     |  DELETE /billing/subscriptions/{id}    |                     |                 |
     |---------------->|                     |                     |                 |
     |                 | 18. Cancel Stripe subscription            |                 |
     |                 |-------------------->|                     |                 |
     |                 | 19. Set status=CANCELED                   |                 |
     |                 |  Deactivate features at period end        |                 |
     |                 |-------------------------------------->|                 |
     |                 |                     |                     | 20. Features disabled
     |                 |                     |                     |                 |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Tenant Admin | Lists available plans | billing-service | `GET /api/v1/billing/plans` |
| 2 | billing-service | Returns plan details (Free/Starter/Pro/Enterprise) | billing-service | HTTP 200 |
| 3 | Tenant Admin | Selects plan + provides payment method | billing-service | `POST /api/v1/billing/subscriptions` |
| 4 | billing-service | Creates Stripe subscription | billing-service | Stripe API |
| 5 | Stripe | Processes payment | Stripe | Webhook confirmation |
| 6 | billing-service | Creates subscription record (status=ACTIVE) | billing-service | DB insert |
| 7-8 | tenant-service | Activates plan features (limits, flags) | tenant-service | Feature update |
| 9 | billing-service | Returns subscription confirmation | billing-service | HTTP 201 |
| 10 | billing-service | Meters usage (hourly job) | billing-service | Scheduled task |
| 11-12 | tenant-service | Enforces plan limits (blocks if over) | tenant-service | Limit check |
| 13 | billing-service | Tracks overage charges | billing-service | Usage logging |
| 14 | billing-service | Generates monthly invoice | billing-service | Invoice generation |
| 15 | billing-service | Charges via Stripe | billing-service | Stripe API |
| 16 | notification-service | Sends invoice email | notification-service | Email dispatch |
| 17-18 | Tenant Admin | Cancels subscription | billing-service | `DELETE /api/v1/billing/subscriptions/{id}` |
| 19-20 | tenant-service | Deactivates features at period end | tenant-service | Feature disable |

### Postconditions

- Subscription record created and active
- Stripe subscription linked
- Plan features activated (storage, bandwidth, scene, user limits)
- Usage metering active
- Monthly invoices generated and charged
- Cancellation flow available (features disabled at period end)

### Error Handling

| Error | Handling |
|-------|----------|
| Payment declined | HTTP 402 — "Payment failed"; subscription stays PENDING |
| Stripe webhook fails | Retry with exponential backoff; manual reconciliation |
| Usage exceeds limit | Feature blocked with upgrade prompt; no data loss |
| Invoice charge fails | Retry 3 times over 7 days; tenant notified |
| Plan downgrade mid-cycle | Prorated credit applied; features reduced at next cycle |
| Cancellation during trial | Immediate cancellation; no charge |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `billing.usage.metered` | Usage metered | `{ tenantId, storage, bandwidth, scenes, timestamp }` |
| `tenant.created` | Plan activated | `{ tenantId, plan, features }` |

---

## Flow 14: Content Publishing & Embedding

### Overview

A Content Creator finalizes a scene, publishes it, and generates embed codes, QR codes, and public URLs. The system performs device detection and selects the optimal rendering path.

### Actors

| Actor | Service Role |
|-------|-------------|
| Content Creator | Scene author |
| Scene Service | Publishing orchestration |
| CDN | Asset caching and delivery |
| XR Service | Device detection and rendering |
| End User | Viewer on any device |

### Preconditions

- Scene is complete (background, hotspots, assets)
- CDN configured
- Scene Service operational
- XR Service operational

### Swimlane Diagram

```
+--------------+  +------------------+  +------------------+  +------------------+  +------------------+
|   Content    |  |  Scene Service   3|  |       CDN        |  |   XR Service     |  |    End User      |
|   Creator    |  |     (8122)       |  |   CloudFront     |  |     (8125)       |  |                  |
+------+-------+  +--------+---------+  +--------+---------+  +--------+---------+  +--------+---------+
       |                   |                     |                     |                     |
       | 1. Finish editing scene                |                     |                     |
       |  (all nodes, hotspots, assets in place)                     |                     |
       |                   |                     |                     |                     |
       | 2. Click "Publish"                     |                     |                     |
       |  POST /scenes/{id}/publish             |                     |                     |
       |------------------>|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   | 3. Validate completeness:                |                     |
       |                   |  - Has background                       |                     |
       |                   |  - Has >= 1 hotspot                     |                     |
       |                   |  - All assets status=READY              |                     |
       |                   |  - No broken asset references           |                     |
       |                   |                     |                     |                     |
       |                   | 4. Set status=PUBLISHED                 |                     |
       |                   |  Generate unique public URL              |                     |
       |                   |  (https://xrvista.com/scene/{sceneId})  |                     |
       |                   |                     |                     |                     |
       |                   | 5. Generate embed code                  |                     |
       |                   |  <iframe src="..." width="100%"         |                     |
       |                   |   height="600" frameborder="0">         |                     |
       |                   |  </iframe>                               |                     |
       |                   |                     |                     |                     |
       |                   | 6. Generate script embed                |                     |
       |                   |  <script src="https://cdn.xrvista.com   |                     |
       |                   |   /embed.js" data-scene="{id}">         |                     |
       |                   |  </script>                               |                     |
       |                   |                     |                     |                     |
       |                   | 7. Generate QR code                     |                     |
       |                   |  (for mobile AR/VR access)               |                     |
       |                   |                     |                     |                     |
       |                   | 8. Publish scene.published event         |                     |
       |                   |------------------>|                     |                     |
       |                   |                     |                     |                     |
       |                   |                     | 9. CDN caches published assets            |
       |                   |                     |  (scene graph, 3D models, textures,       |
       |                   |                     |   360 images, audio files)                |
       |                   |                     |                     |                     |
       |                   |                     | 10. Edge locations populated              |
       |                   |                     |                     |                     |
       | 11. Return publish results:             |                     |                     |
       |  - publicUrl                           |                     |                     |
       |  - embedCode (iframe)                  |                     |                     |
       |  - scriptCode                          |                     |                     |
       |  - qrCode (PNG)                        |                     |                     |
       |<------------------|                     |                     |                     |
       |                   |                     |                     |                     |
       |                   |                     |                     | 12. End User accesses via URL
       |                   |                     |                     |  (or embed, or QR scan)
       |                   |                     |                     |                     |
       |                   |                     |                     | 13. Device detection:
       |                   |                     |                     |  - User-Agent parsing
       |                   |                     |                     |  - Screen size check
       |                   |                     |                     |  - WebXR capability
       |                   |                     |                     |                     |
       |                   |                     |                     | 14. Select optimal rendering path:
       |                   |                     |                     |  A) Web Browser (Three.js)
       |                   |                     |                     |  B) Mobile (AR.js / WebXR)
       |                   |                     |                     |  C) VR Headset (WebXR Full)
       |                   |                     |                     |  D) Cardboard (Gyroscope)
       |                   |                     |                     |                     |
       |                   |                     |                     | 15. Load scene from CDN
       |                   |                     |<--------------------|                     |
       |                   |                     |                     |                     |
       |                   |                     | 16. Serve cached assets                   |
       |                   |                     |---------------------------------------->|
       |                   |                     |                     |                     |
       |                   |                     |                     | 17. Render XR experience
       |                   |                     |                     |  (device-appropriate)
       |                   |                     |                     |                     |
```

### Steps

| Step | Actor | Action | Service | API/Event |
|------|-------|--------|---------|-----------|
| 1 | Content Creator | Completes scene editing | frontend | Scene Builder UI |
| 2 | Content Creator | Clicks "Publish" | scene-service | `POST /api/v1/scenes/{id}/publish` |
| 3 | scene-service | Validates completeness (background, hotspots, asset status) | scene-service | Validation rules |
| 4 | scene-service | Sets status=PUBLISHED, generates unique public URL | scene-service | DB update |
| 5 | scene-service | Generates iframe embed code | scene-service | Embed builder |
| 6 | scene-service | Generates script embed code | scene-service | Embed builder |
| 7 | scene-service | Generates QR code (for mobile access) | scene-service | QR library |
| 8 | scene-service | Publishes `scene.published` event | Kafka | Topic: `scene.published` |
| 9 | CDN | Caches published assets (graph, models, textures, images, audio) | CloudFront | Asset synchronization |
| 10 | CDN | Populates edge locations worldwide | CloudFront | Global CDN distribution |
| 11 | scene-service | Returns publish results (URL, embed codes, QR) | scene-service | HTTP 200 |
| 12-13 | End User | Accesses via URL/embed/QR; device detection runs | xr-service | User-Agent + feature detection |
| 14 | xr-service | Selects optimal rendering path | xr-service | Device capability check |
| 15-16 | End User | Loads scene from CDN | CloudFront | Edge cache delivery |
| 17 | End User | Renders XR experience | xr-service | Three.js / WebXR / AR.js |

### Device Detection Matrix

| Device | Detection Method | Rendering Path | XR API |
|--------|-----------------|----------------|--------|
| Desktop Browser | User-Agent + screen > 1024px | Three.js WebGL | None |
| Mobile Browser | User-Agent + touch events | Three.js + gyroscope | WebXR (if supported) |
| Mobile AR | User-Agent + camera + AR.js | AR.js marker tracking | WebXR AR Module |
| Meta Quest | User-Agent + WebXR Fullscreen | Three.js + WebXR | WebXR VR Module |
| Apple Vision Pro | User-Agent + WebXR Fullscreen | Three.js + WebXR | WebXR VR Module |
| Google Cardboard | User-Agent + gyroscope | Three.js + stereo split | DeviceOrientation |
| Embed (iframe) | Parent page context | Responsive Three.js | WebXR (if available) |

### Postconditions

- Scene published with unique public URL
- Embed codes generated (iframe + script)
- QR code generated for mobile access
- CDN cached with all published assets at global edge locations
- Device detection active; optimal rendering path selected
- End users can access experience on any device

### Error Handling

| Error | Handling |
|-------|----------|
| Scene incomplete | HTTP 400 — "Missing required elements: [list]" |
| Asset not READY | HTTP 400 — "Asset {assetId} still processing" |
| CDN cache invalidation fails | Warning; content propagated by TTL (max 15 min) |
| Embed blocked by CSP | Documentation link provided; CSP header guidance |
| QR code generation fails | Fallback to URL-only display |
| Device not supported | HTTP 400 — "Device not supported"; show compatibility matrix |
| Scene graph too large for CDN | Warning; suggest scene optimization |

### Kafka Events Triggered

| Topic | Event | Payload |
|-------|-------|---------|
| `scene.published` | Scene published | `{ sceneId, tenantId, type, publicUrl, embedUrl, publishedAt, assetCount }` |

---

## Appendix A: Cross-Flow Event Dependency Map

This diagram shows how Kafka events from one flow trigger actions in other flows.

```
+-------------------+     asset.uploaded      +-------------------+
| Flow 2: Asset     |------------------------>| Flow 3: 3D Model  |
| Upload            |                         | Processing        |
+-------------------+                         +-------------------+
        |                                              |
        | video.transcoded                             | asset.processed
        v                                              v
+-------------------+                         +-------------------+
| Flow 5: 360 Video |                         | Flow 4: VR Scene  |
| Experience         |                         | Creation           |
+-------------------+                         +-------------------+
        |                                              |
        | scene.published                              | scene.published
        v                                              v
+-------------------+                         +-------------------+
| Flow 14: Publish  |                         | Flow 6: Virtual   |
| & Embedding       |                         | Tour               |
+-------------------+                         +-------------------+
                                                      |
                                                      | scene.published
                                                      v
                                              +-------------------+
                                              | Flow 7: Showroom  |
                                              | Builder            |
                                              +-------------------+

+-------------------+     ai.generation      +-------------------+
| Flow 10: AI Scene |<-----------------------| AI Service         |
| Generation         |----------------------->| Flow 4: VR Scene   |
+-------------------+                         +-------------------+

+-------------------+     user.joined        +-------------------+
| Flow 9: Multi-    |<-----------------------| Collaboration      |
| user VR           |----------------------->| Service            |
+-------------------+                         +-------------------+

+-------------------+     training.completed  +-------------------+
| Flow 11: VR       |<-----------------------| Scene Service      |
| Training           |----------------------->| Analytics          |
+-------------------+                         +-------------------+

+-------------------+     iot.data.received  +-------------------+
| Flow 12: Digital  |<-----------------------| Digital Twin       |
| Twin Sync          |----------------------->| XR Service         |
+-------------------+                         +-------------------+

+-------------------+  billing.usage.metered +-------------------+
| Flow 13: Billing  |<-----------------------| Billing Service    |
| & Subscription    |----------------------->| Tenant Service     |
+-------------------+                         +-------------------+
```

---

## Appendix B: Service Interaction Matrix

| Service | Flows Involved | Publishes To | Consumes From |
|---------|---------------|--------------|---------------|
| iam-service | 1 | — | tenant.created |
| tenant-service | 1, 13 | tenant.created | billing.usage.metered |
| asset-service | 2, 3, 4, 5, 6, 7, 8, 10 | asset.uploaded, asset.processed | — |
| scene-service | 4, 5, 6, 7, 8, 10, 11, 14 | scene.published, scene.saved | asset.processed, ai.generation.complete |
| video-service | 2, 5 | video.transcoded | asset.uploaded |
| streaming-service | 2, 5 | — | video.transcoded |
| xr-service | 8, 9, 12, 14 | xr.session.start | iot.data.received, scene.published |
| ai-service | 10 | ai.generation.complete | — |
| analytics-service | 9, 11, 12 | analytics.event, training.completed | user.joined, scene.published |
| billing-service | 1, 13 | billing.usage.metered | — |
| collaboration-service | 9 | user.joined | — |
| digital-twin-service | 12 | iot.data.received | — |
| notification-service | 1, 9, 11, 13 | — | tenant.created, user.joined, analytics.event, training.completed |

---

## Appendix C: Flow Complexity Summary

| Flow | Steps | Services | Kafka Events | Complexity |
|------|-------|----------|-------------|------------|
| 1: Tenant Onboarding | 16 | 5 | 2 | Medium |
| 2: Asset Upload & Processing | 18 | 5 | 3 | High |
| 3: 3D Model Processing | 13 | 4 | 2 | Medium |
| 4: VR Scene Creation | 29 | 5 | 2 | Very High |
| 5: 360 Video Experience | 20 | 5 | 3 | High |
| 6: Virtual Tour Creation | 20 | 4 | 1 | High |
| 7: Showroom Builder | 23 | 4 | 1 | High |
| 8: AR Experience | 19 | 4 | 1 | Medium |
| 9: Multi-user VR Collaboration | 21 | 4 | 3 | Very High |
| 10: AI Scene Generation | 18 | 5 | 1 | High |
| 11: VR Training (LMS) | 29 | 5 | 2 | Very High |
| 12: Digital Twin Sync | 12 | 5 | 2 | Medium |
| 13: Subscription & Billing | 20 | 5 | 2 | High |
| 14: Content Publishing & Embedding | 17 | 5 | 1 | Medium |
| **Total** | **275** | **13** | **26** | — |

---

*Document generated for XRVista — Enterprise XR SaaS Platform*
*End of Business Flow Specifications*
