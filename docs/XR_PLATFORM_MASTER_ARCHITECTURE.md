# XR PLATFORM MASTER ARCHITECTURE
## Immersive Experience Platform (VR/AR/XR SaaS Enterprise)

---

## 1. PRODUCT VISION

### 1.1 Vision Statement
Xây dựng nền tảng XR SaaS Multi-Tenant hàng đầu khu vực, cho phép doanh nghiệp tạo, quản lý, phân phối và triển khai trải nghiệm thực tế ảo/thực tế tăng cường trên mọi thiết bị mà không cần kiến thức lập trình.

### 1.2 Competitive Landscape
| Competitor | Strength | Our Advantage |
|-----------|----------|---------------|
| Matterport | 3D scan, digital twin | Multi-format (360, 3D, AR, XR), AI-powered, no-code builder |
| Pano2VR | Panoramic tours | Enterprise SaaS, multi-tenant, streaming infrastructure |
| Spatial.io | VR meetings | Full platform (tour, showroom, training, museum) |
| Virbela | Virtual campus | Asian market focus, AI integration, BIM/GIS support |
| Mozilla Hubs | Open source VR | Enterprise security, compliance, scalability |

### 1.3 Target Users
- **Enterprise**: Bất động sản, du lịch, giáo dục, y tế, sản xuất, bán lẻ
- **Content Creator**:摄影师, videographer, 3D artist
- **Training Organization**: Doanh nghiệp, trường đại học, bệnh viện
- **Museum/Cultural Heritage**: Bảo tàng, di tích lịch sử

---

## 2. PLATFORM CAPABILITIES MATRIX

### 2.1 Content Types
| Content Type | Upload | Create | AI Generate | Streaming | VR Playback |
|-------------|--------|--------|-------------|-----------|-------------|
| 360 Monoscopic Video | ✅ | - | - | HLS/DASH | ✅ |
| 360 Stereoscopic Video | ✅ | - | - | HLS/DASH | ✅ |
| 360 Photo/Panorama | ✅ | - | AI Enhance | CDN | ✅ |
| 3D Model (GLB/GLTF) | ✅ | - | AI Generate | CDN | ✅ |
| 3D Model (FBX/OBJ) | ✅ Convert | - | - | CDN | ✅ |
| Audio (Spatial) | ✅ | - | AI Narration | CDN | ✅ |
| Hotspot | - | Drag & Drop | AI Suggest | - | ✅ |
| Text/Annotation | - | Drag & Drop | AI Generate | - | ✅ |

### 2.2 Experience Types
| Experience | Builder | VR | AR | XR | Web | Mobile |
|-----------|---------|-----|-----|-----|-----|--------|
| VR Tour | ✅ | ✅ | - | - | ✅ | ✅ |
| VR Showroom | ✅ | ✅ | - | - | ✅ | ✅ |
| VR Museum | ✅ | ✅ | - | - | ✅ | ✅ |
| VR Training | ✅ | ✅ | - | - | ✅ | ✅ |
| VR Campus | ✅ | ✅ | - | - | ✅ | ✅ |
| Virtual Real Estate | ✅ | ✅ | - | - | ✅ | ✅ |
| AR Experience | - | - | ✅ | - | ✅ | ✅ |
| XR Space | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Metaverse Space | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 360 Video Player | ✅ | ✅ | - | - | ✅ | ✅ |

### 2.3 Device Support
| Device | Web | Cardboard | Quest | Vision Pro | Android | iOS |
|--------|-----|-----------|-------|------------|---------|-----|
| VR Tour | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| VR Showroom | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 360 Video | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| AR Experience | ✅ | - | ✅ | ✅ | ✅ | ✅ |
| XR Space | ✅ | - | ✅ | ✅ | ✅ | ✅ |
| Metaverse | ✅ | - | ✅ | ✅ | ✅ | ✅ |

---

## 3. SYSTEM ARCHITECTURE

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
├──────────┬──────────┬──────────┬──────────┬──────────────────────┤
│ Web App  │ Mobile   │ VR App   │ AR App   │ Embedded Player      │
│ Next.js  │ Android  │ Quest/   │ ARCore/  │ WebView/iFrame       │
│ React    │ Kotlin   │ Vision   │ ARKit    │                      │
│ ThreeJS  │ Swift    │ Pro      │          │                      │
│ AFrame   │          │          │          │                      │
│ WebXR    │          │          │          │                      │
└────┬─────┴────┬─────┴────┬─────┴────┬─────┴──────────────────────┘
     │          │          │          │
     ▼          ▼          ▼          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (8080)                          │
│  Spring Cloud Gateway │ JWT Auth │ Rate Limit │ CORS │ WAF      │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────────────┐
│                     SERVICE MESH                                 │
├────────────────────────────┼────────────────────────────────────┤
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────┐      │
│  │              IAM SERVICE (8101)                        │      │
│  │  Keycloak │ OAuth2 │ OIDC │ MFA │ RBAC │ ABAC        │      │
│  └─────────────────────────┼─────────────────────────────┘      │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────┐      │
│  │           TENANT SERVICE (8120)                        │      │
│  │  Multi-Tenant │ Subscription │ Billing │ Feature Flags │      │
│  └─────────────────────────┼─────────────────────────────┘      │
│                            │                                     │
│  ┌──── XR CONTENT PIPELINE ────────────────────────────┐       │
│  │                                                       │       │
│  │  Asset Service (8121) ──► Video Service (8123)       │       │
│  │       │                        │                      │       │
│  │       ▼                        ▼                      │       │
│  │  Scene Service (8122) ──► Streaming Service (8124)   │       │
│  │       │                                                   │       │
│  │       ▼                                                   │       │
│  │  AI Service (8126)                                        │       │
│  │  - Scene Generator                                        │       │
│  │  - Narration                                              │       │
│  │  - Translation                                            │       │
│  │  - Tour Guide                                             │       │
│  │  - Hotspot Generator                                      │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌──── ENTERPRISE MODULES ─────────────────────────────┐       │
│  │                                                       │       │
│  │  Digital Twin Service (8130)                          │       │
│  │  BIM/CAD Service (8131)                               │       │
│  │  GIS Service (8132)                                   │       │
│  │  Collaboration Service (8129)                         │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌──── ANALYTICS & MONITORING ─────────────────────────┐       │
│  │                                                       │       │
│  │  Analytics Service (8127)                             │       │
│  │  XR Service (8125)                                    │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌──── SUPPORT SERVICES ───────────────────────────────┐       │
│  │                                                       │       │
│  │  Notification Service │ Billing Service │ Audit       │       │
│  └───────────────────────────────────────────────────────┘       │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────────────┐
│                     DATA LAYER                                   │
├──────────┬─────────┬──────────┬──────────┬───────────────────────┤
│PostgreSQL│ MongoDB │ Redis    │ ElasticS │ MinIO                 │
│(per svc) │(scene)  │(cache)   │(search)  │(objects)              │
├──────────┴─────────┴──────────┴──────────┴───────────────────────┤
│  ClickHouse (analytics) │ Kafka (events) │ Prometheus/Grafana   │
└─────────────────────────┴────────────────┴──────────────────────┘
```

### 3.2 Microservices Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES MAP                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  INFRASTRUCTURE SERVICES                                          │
│  ├── service-registry (8761) - Eureka Discovery                  │
│  ├── config-server (8888) - Centralized Config                   │
│  └── api-gateway (8080) - Spring Cloud Gateway                   │
│                                                                   │
│  CORE PLATFORM SERVICES                                          │
│  ├── iam-service (8101) - Identity & Access Management           │
│  ├── tenant-service (8120) - Multi-Tenant Management             │
│  └── billing-service (8115) - Subscription & Billing             │
│                                                                   │
│  XR CONTENT SERVICES                                             │
│  ├── asset-service (8121) - 3D/360 Asset Management              │
│  ├── scene-service (8122) - Scene Graph & Builder                │
│  ├── video-service (8123) - 360 Video Transcoding                │
│  ├── streaming-service (8124) - HLS/DASH/CDN                    │
│  ├── xr-service (8125) - WebXR Session Management                │
│  └── ai-service (8126) - AI Generation & Intelligence            │
│                                                                   │
│  ENTERPRISE SERVICES                                             │
│  ├── digital-twin-service (8130) - Digital Twin Management       │
│  ├── bim-cad-service (8131) - BIM/CAD Viewer                     │
│  ├── gis-service (8132) - Indoor Navigation & GIS                │
│  └── collaboration-service (8129) - Multi-User VR                │
│                                                                   │
│  ANALYTICS & MONITORING                                          │
│  ├── analytics-service (8127) - XR Analytics & Heatmap          │
│  └── reporting-service (8117) - Dashboard & Reports              │
│                                                                   │
│  SUPPORT SERVICES                                                │
│  ├── notification-service (8118) - Push/Email/SMS                │
│  ├── audit-service (8119) - Audit Logging                        │
│  └── webhooks-service - Webhook Management                       │
│                                                                   │
│  TOTAL: 37 MICROSERVICES                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. DOMAIN-DRIVEN DESIGN (DDD)

### 4.1 Bounded Contexts

```
┌─────────────────────────────────────────────────────────────────┐
│                    BOUNDED CONTEXTS                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. IDENTITY & ACCESS                                            │
│     Aggregate: User, Role, Permission, MFA                       │
│     Events: user.created, user.authenticated, role.assigned      │
│                                                                   │
│  2. TENANT MANAGEMENT                                            │
│     Aggregate: Tenant, Subscription, Plan, FeatureFlag           │
│     Events: tenant.provisioned, subscription.renewed              │
│                                                                   │
│  3. ASSET MANAGEMENT                                             │
│     Aggregate: Asset, ProcessingJob, Variant                     │
│     Events: asset.uploaded, asset.processed, asset.transcoded    │
│                                                                   │
│  4. SCENE MANAGEMENT                                             │
│     Aggregate: Scene, SceneNode, Hotspot, SceneVersion           │
│     Events: scene.created, scene.published, scene.duplicated     │
│                                                                   │
│  5. VIDEO PROCESSING                                             │
│     Aggregate: VideoJob, TranscodeProfile, OutputVariant         │
│     Events: video.queued, video.processing, video.completed      │
│                                                                   │
│  6. STREAMING & DELIVERY                                         │
│     Aggregate: Stream, Manifest, CDNDistribution                 │
│     Events: stream.ready, cache.invalidated                      │
│                                                                   │
│  7. XR SESSION                                                   │
│     Aggregate: XrSession, DeviceInfo, GazeData                   │
│     Events: session.started, session.ended, gaze.captured        │
│                                                                   │
│  8. AI INTELLIGENCE                                              │
│     Aggregate: AiJob, GeneratedScene, Narration                  │
│     Events: ai.generation.completed, ai.narration.ready          │
│                                                                   │
│  9. ANALYTICS                                                    │
│     Aggregate: AnalyticsEvent, Heatmap, SessionStats             │
│     Events: analytics.event.tracked                              │
│                                                                   │
│  10. COLLABORATION                                               │
│      Aggregate: CollaborationRoom, Participant, Avatar           │
│      Events: room.created, user.joined, user.left                │
│                                                                   │
│  11. DIGITAL TWIN                                                │
│      Aggregate: DigitalTwin, IoTEndpoint, SyncState              │
│      Events: twin.created, twin.synced, iot.data.received        │
│                                                                   │
│  12. GEOSPATIAL                                                  │
│      Aggregate: Floor, Room, Path, NavigationRoute               │
│      Events: floor.created, navigation.requested                 │
│                                                                   │
│  13. BIM/CAD                                                     │
│      Aggregate: BimModel, IfcEntity, ModelHierarchy              │
│      Events: bim.uploaded, bim.parsed                            │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Aggregate Design

```
Scene Aggregate (Root: Scene)
├── SceneNode[] (value object - embedded in MongoDB)
├── Hotspot[] (value object - embedded in MongoDB)
├── SceneSettings (value object)
└── SceneVersion (value object)

Asset Aggregate (Root: Asset)
├── AssetVariant[] (value object)
├── TranscodeJob (entity)
└── ProcessingMetadata (value object)

DigitalTwin Aggregate (Root: DigitalTwin)
├── IoTEndpoint[] (entity)
├── FloorMapping[] (value object)
└── SyncState (value object)
```

---

## 5. TECHNICAL ARCHITECTURE

### 5.1 Backend Stack
| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Runtime | Java | 21 | LTS Runtime |
| Framework | Spring Boot | 3.2.5 | Application Framework |
| Cloud | Spring Cloud | 2023.0.1 | Microservice Infrastructure |
| Gateway | Spring Cloud Gateway | - | API Gateway |
| Discovery | Netflix Eureka | - | Service Discovery |
| Config | Spring Cloud Config | - | Centralized Configuration |
| ORM | Spring Data JPA | - | Database Access |
| NoSQL | Spring Data MongoDB | - | Scene Graph Storage |
| Cache | Spring Data Redis | - | Caching |
| Search | Spring Data Elasticsearch | - | Full-text Search |
| Messaging | Spring Kafka | - | Event Streaming |
| Migration | Flyway | - | Database Versioning |
| Resilience | Resilience4j | 2.2.0 | Circuit Breaker, Rate Limit |
| Mapping | MapStruct | 1.5.5 | DTO Mapping |
| API Doc | SpringDoc OpenAPI | 2.5.0 | Swagger |
| Observability | Micrometer + OTel | 1.38.0 | Tracing, Metrics |
| Security | Spring Security + Keycloak | - | Auth, OAuth2, OIDC |

### 5.2 Frontend Stack (Web)
| Technology | Version | Purpose |
|-----------|---------|---------|
| Next.js | 14.x | React Framework |
| TypeScript | 5.7 | Type Safety |
| React | 18.3 | UI Library |
| Three.js | 0.164 | 3D Rendering |
| @react-three/fiber | 0.164 | React Three.js Integration |
| @react-three/drei | 0.164 | Three.js Helpers |
| A-Frame | 1.6.0 | WebXR Framework |
| WebXR API | - | VR/AR Browser API |
| HLS.js | 1.5.7 | Video Streaming |
| Ant Design | 5.24 | UI Components |
| Redux Toolkit | 2.6 | State Management |
| React Query | 3.39 | Server State |
| Socket.IO | 4.8 | Real-time Communication |
| TailwindCSS | 3.4 | Styling |

### 5.3 Mobile Native Stack
| Platform | Language | Framework | XR SDK |
|----------|---------|-----------|--------|
| Android | Kotlin | Native Android | ARCore, Cardboard SDK, WebXR WebView |
| iOS | Swift | Native iOS | ARKit, Cardboard SDK, WebXR WebView |

> **NOT using React Native** - Native performance for VR/AR is critical.

### 5.4 VR Player Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      VR PLAYER STACK                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─── PRESENTATION LAYER ──────────────────────────────┐       │
│  │                                                       │       │
│  │  Web: Three.js + A-Frame + WebXR API                  │       │
│  │  Android: Kotlin + OpenGL ES + Cardboard SDK          │       │
│  │  iOS: Swift + Metal + Cardboard SDK                   │       │
│  │  Quest: OpenXR + WebGL                                │       │
│  │  Vision Pro: RealityKit + WebXR                       │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌─── RENDERING PIPELINE ──────────────────────────────┐       │
│  │                                                       │       │
│  │  360 Video ──► Equirectangular Projection             │       │
│  │           ──► Stereoscopic Separation (L/R Eye)       │       │
│  │           ──► Sphere Geometry Rendering                │       │
│  │           ──► Head Tracking (Gyroscope/Sensor)         │       │
│  │                                                       │       │
│  │  3D Scene ──► GLTF/GLB Parser                         │       │
│  │           ──► Draco Decompression                     │       │
│  │           ──► KTX2 Texture Loading                    │       │
│  │           ──► PBR Material Rendering                  │       │
│  │           ──► Spatial Audio Integration               │       │
│  │                                                       │       │
│  │  AR Scene ──► Camera Feed                             │       │
│  │           ──► Surface Detection                       │       │
│  │           ──► Image Tracking                          │       │
│  │           ──► Object Placement                        │       │
│  │           ──► Occlusion Handling                      │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌─── INPUT HANDLING ──────────────────────────────────┐       │
│  │                                                       │       │
│  │  Gyroscope ──► Device Orientation (360 Video)         │       │
│  │  Gamepad   ──► VR Controller (Quest/Vision Pro)       │       │
│  │  Gaze      ──► Look-at Selection (Cardboard)          │       │
│  │  Touch     ──► Mobile Interaction                     │       │
│  │  Hand      ──► Hand Tracking (Quest 3/Vision Pro)     │       │
│  │  Voice     ──► Voice Commands                         │       │
│  └───────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌─── AUDIO PIPELINE ──────────────────────────────────┐       │
│  │                                                       │       │
│  │  Spatial Audio ──► HRTF Rendering                     │       │
│  │  Ambisonics   ──► 360 Audio                           │       │
│  │  Voice Chat   ──► WebRTC                              │       │
│  │  Narration    ──► TTS Integration                     │       │
│  └───────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### 5.5 360 Video Streaming Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                   360 VIDEO PIPELINE                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  UPLOAD                                                          │
│  ├── Monoscopic 360 (equirectangular)                           │
│  ├── Stereoscopic 360 (top-bottom / side-by-side)               │
│  └── Resolution: 4K / 6K / 8K                                   │
│                                                                   │
│  PROCESSING (video-service + FFmpeg)                             │
│  ├── Input Validation (format, resolution, metadata)            │
│  ├── Transcoding Profiles:                                       │
│  │   ├── 4K  (3840x2160) ──► HLS 4K + DASH 4K                  │
│  │   ├── 6K  (5760x2880) ──► HLS 6K + DASH 6K                  │
│  │   └── 8K  (7680x3840) ──► HLS 8K + DASH 8K                  │
│  ├── Tiled Streaming (DASH only):                                │
│  │   ──► Split into tiles for adaptive quality                  │
│  │   ──► Only decode visible region                              │
│  │   ──► Reduce bandwidth by 60-80%                             │
│  ├── Stereoscopic Processing:                                    │
│  │   ──► Separate L/R eye views                                 │
│  │   ──► Generate metadata for stereo sync                      │
│  │   ──► Side-by-side to top-bottom conversion                  │
│  ├── Audio Processing:                                           │
│  │   ──► Ambisonics channel detection                           │
│  │   ──► Spatial audio metadata embedding                       │
│  │   ──► Audio normalization                                    │
│  └── Thumbnail Generation:                                       │
│      ──► Equirectangular preview                                 │
│      ──► Cubemap preview                                        │
│                                                                   │
│  STORAGE (MinIO)                                                 │
│  ├── Original: xr-videos/{tenant}/{asset-id}/original.mp4       │
│  ├── HLS: xr-videos/{tenant}/{asset-id}/hls/                    │
│  │   ├── master.m3u8                                            │
│  │   ├── 4k/playlist.m3u8                                       │
│  │   ├── 6k/playlist.m3u8                                       │
│  │   └── 8k/playlist.m3u8                                       │
│  ├── DASH: xr-videos/{tenant}/{asset-id}/dash/                   │
│  │   ├── manifest.mpd                                           │
│  │   └── tiles/                                                  │
│  └── Thumbnails: xr-videos/{tenant}/{asset-id}/thumbs/           │
│                                                                   │
│  DELIVERY (streaming-service + CDN)                              │
│  ├── HLS Manifest Generation (M3U8)                              │
│  ├── DASH Manifest Generation (MPD)                              │
│  ├── Adaptive Bitrate (ABR) ──► Auto quality selection          │
│  ├── Tiled DASH ──► Only decode visible tile                    │
│  ├── Signed URLs ──► DRM protection                              │
│  ├── CDN Distribution ──► Edge caching                           │
│  └── CORS Configuration ──► Cross-origin access                 │
│                                                                   │
│  PLAYBACK (VR Player)                                            │
│  ├── Equirectangular ──► Sphere mapping                         │
│  ├── Stereoscopic ──► Dual viewport (L/R eye)                   │
│  ├── Head Tracking ──► Gyroscope/Sensor fusion                  │
│  ├── Quality Selection ──► ABR based on bandwidth                │
│  ├── Spatial Audio ──► HRTF + Ambisonics                        │
│  └── Controls ──► Play/Pause/Seek/Volume/Quality                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.6 3D Engine Architecture (ThreeJS + AFrame)

```
┌─────────────────────────────────────────────────────────────────┐
│                    3D RENDERING ENGINE                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  THREE.JS CORE                                                   │
│  ├── Scene Graph Management                                      │
│  ├── PBR Material System (MeshStandardMaterial)                  │
│  ├── Draco Compression Support                                   │
│  ├── KTX2 Texture Compression                                   │
│  ├── GLTF/GLB Loader                                             │
│  ├── FBX/OBJ Import (via conversion)                            │
│  ├── HDR Environment Maps                                        │
│  └── Post-Processing Pipeline                                    │
│                                                                   │
│  AFRAME LAYER                                                    │
│  ├── a-scene ──► Scene container                                 │
│  ├── a-entity ──► Game object                                    │
│  ├── a-assets ──► Asset management                               │
│  ├── a-videosphere ──► 360 video rendering                      │
│  ├── a-sky ──► 360 image background                             │
│  ├── a-gltf-model ──► 3D model loading                          │
│  ├── a-sound ──► Spatial audio                                   │
│  ├── a-animation ──► Animation system                            │
│  └── Custom Components ──► Extend via AFrame API                 │
│                                                                   │
│  WEBXR INTEGRATION                                               │
│  ├── navigator.xr.requestSession()                               │
│  ├── XRReferenceSpace (local-floor, viewer)                      │
│  ├── XRInputSource (gamepad, screen, hand)                       │
│  ├── XRLayer (WebGL layer for VR rendering)                      │
│  ├── XRFrame (animation loop)                                    │
│  ├── XRHitTest (AR surface detection)                            │
│  ├── XRImageTracking (AR image recognition)                      │
│  └── XRMeshDetection (AR mesh scanning)                          │
│                                                                   │
│  PERFORMANCE OPTIMIZATION                                        │
│  ├── LOD (Level of Detail) ──► Reduce poly on distant objects   │
│  ├── Frustum Culling ──► Skip off-screen objects                 │
│  ├── Instanced Rendering ──► Batch similar objects               │
│  ├── Texture Atlas ──► Reduce draw calls                         │
│  ├── Lazy Loading ──► Load on demand                             │
│  ├── Worker Thread ──► Offload parsing/decompression             │
│  ├── WebGPU (future) ──► Better GPU utilization                  │
│  └── Memory Management ──► Dispose unused resources              │
│                                                                   │
│  QUALITY ADAPTATION                                              │
│  ├── Auto-detect device capability                               │
│  ├── Set render resolution (1x, 1.5x, 2x)                       │
│  ├── Adjust polygon count based on FPS                           │
│  ├── Texture quality (full, half, quarter)                       │
│  ├── Shadow quality (off, low, medium, high)                     │
│  └── Post-processing (off, bloom, AO)                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. DATABASE DESIGN

### 6.1 Database-per-Service Mapping

| Service | Database | Engine | Purpose |
|---------|----------|--------|---------|
| iam-service | vcall_iam | PostgreSQL 16 | Users, Roles, Permissions |
| tenant-service | vcall_tenant | PostgreSQL 16 | Tenants, Subscriptions |
| asset-service | vcall_asset | PostgreSQL 16 | Assets, Processing Jobs |
| scene-service | vcall_scene | MongoDB 7.0 | Scene Graph, Nodes, Hotspots |
| video-service | vcall_video | PostgreSQL 16 | Video Jobs, Transcoding |
| xr-service | vcall_xr | PostgreSQL 16 | XR Sessions, Gaze Data |
| analytics-service | vcall_analytics | ClickHouse | Analytics Events, Heatmaps |
| digital-twin-service | vcall_twin | PostgreSQL 16 | Digital Twins, IoT |
| gis-service | vcall_gis | PostgreSQL 16 + PostGIS | Floors, Rooms, Navigation |
| collaboration-service | vcall_collab | PostgreSQL 16 | Rooms, Participants |
| billing-service | vcall_billing | PostgreSQL 16 | Plans, Invoices |
| notification-service | vcall_notification | PostgreSQL 16 | Templates, Devices |
| audit-service | vcall_audit | PostgreSQL 16 | Audit Logs |

### 6.2 ERD - Core Entities

```
┌─────────────────────────────────────────────────────────────────┐
│                    ENTITY RELATIONSHIP DIAGRAM                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  [Tenant] ──1:N──► [User]                                        │
│     │                                                             │
│     ├──1:N──► [Subscription] ──N:1──► [Plan]                     │
│     │                                                             │
│     ├──1:N──► [Scene]                                             │
│     │            ├──1:N──► [SceneNode] (MongoDB)                  │
│     │            ├──1:N──► [Hotspot] (MongoDB)                    │
│     │            └──N:M──► [Asset]                                │
│     │                                                             │
│     ├──1:N──► [Asset]                                             │
│     │            ├──1:N──► [AssetVariant]                         │
│     │            └──1:N──► [TranscodeJob]                         │
│     │                                                             │
│     ├──1:N──► [VideoJob]                                          │
│     │            └──N:1──► [Asset]                                │
│     │                                                             │
│     ├──1:N──► [XrSession]                                         │
│     │            └──N:1──► [Scene]                                │
│     │                                                             │
│     ├──1:N──► [DigitalTwin]                                       │
│     │            ├──1:N──► [IoTEndpoint]                          │
│     │            ├──N:1──► [Asset] (BIM model)                    │
│     │            └──N:1──► [Scene]                                │
│     │                                                             │
│     ├──1:N──► [Floor] ──1:N──► [Room]                            │
│     │                                                             │
│     ├──1:N──► [CollaborationRoom]                                 │
│     │            └──1:N──► [CollaborationParticipant]              │
│     │                                                             │
│     └──1:N──► [AnalyticsEvent] (ClickHouse)                       │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Key Schema Definitions

#### Scene (MongoDB)
```json
{
  "_id": "ObjectId",
  "tenantId": "uuid",
  "name": "string",
  "description": "string",
  "type": "VR_TOUR|VR_SHOWROOM|VR_MUSEUM|VR_TRAINING|AR_EXPERIENCE|XR_SPACE",
  "thumbnailUrl": "string",
  "backgroundType": "SOLID_COLOR|GRADIENT|IMAGE|VIDEO_360|ENVIRONMENT_MAP",
  "backgroundAssetId": "string",
  "status": "DRAFT|IN_REVIEW|PUBLISHED|UNPUBLISHED|ARCHIVED",
  "publishedUrl": "string",
  "publishedAt": "datetime",
  "version": "integer",
  "settings": {
    "gravity": "boolean",
    "teleportEnabled": "boolean",
    "locomotionType": "TELEPORT|WALK|FLY",
    "audioEnabled": "boolean",
    "spatialAudioEnabled": "boolean",
    "qualityPreset": "LOW|MEDIUM|HIGH|ULTRA",
    "maxParticipants": "integer",
    "enableMultiUser": "boolean"
  },
  "nodes": [
    {
      "id": "uuid",
      "nodeType": "ROOT|GROUP|MODEL_3D|IMAGE|VIDEO|AUDIO|TEXT|LIGHT|CAMERA|ANNOTATION|UI_ELEMENT|ENVIRONMENT",
      "name": "string",
      "parentId": "uuid|null",
      "position": {"x": 0, "y": 0, "z": 0},
      "rotation": {"x": 0, "y": 0, "z": 0},
      "scale": {"x": 1, "y": 1, "z": 1},
      "content": {
        "assetUrl": "string",
        "material": {},
        "animation": {},
        "physics": {}
      },
      "visible": true,
      "interactive": true,
      "sortOrder": 0
    }
  ],
  "hotspots": [
    {
      "id": "uuid",
      "nodeId": "uuid|null",
      "hotspotType": "INFO|NAVIGATION|MEDIA|LINK|ACTION|ANNOTATION",
      "title": "string",
      "description": "string",
      "position": {"latitude": 0, "longitude": 0},
      "actionType": "OPEN_URL|PLAY_MEDIA|NAVIGATE_SCENE|SHOW_INFO|TRIGGER_ANIMATION|OPEN_MODAL",
      "actionPayload": {},
      "style": {
        "iconUrl": "string",
        "color": "#hex",
        "size": "SMALL|MEDIUM|LARGE",
        "animation": "PULSE|ROTATE|BOUNCE|NONE"
      }
    }
  ],
  "viewCount": 0,
  "avgViewTimeSeconds": 0,
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

#### Asset (PostgreSQL)
```sql
CREATE TABLE xr_asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- VIDEO_360, IMAGE_360, MODEL_3D, AUDIO, PANORAMA
    original_url TEXT NOT NULL,
    processed_url TEXT,
    thumbnail_url TEXT,
    mime_type VARCHAR(100),
    file_size BIGINT,
    
    -- Video-specific
    video_type VARCHAR(50), -- MONOSCOPIC, STEREOSCOPIC
    resolution VARCHAR(20), -- 4K, 6K, 8K
    duration_seconds INTEGER,
    
    -- 3D Model-specific
    model_format VARCHAR(20), -- GLB, GLTF, FBX, OBJ
    has_draco_compression BOOLEAN DEFAULT FALSE,
    has_ktx2_textures BOOLEAN DEFAULT FALSE,
    polygon_count BIGINT,
    vertex_count BIGINT,
    
    -- Processing
    transcode_status VARCHAR(50) DEFAULT 'PENDING',
    processing_progress INTEGER DEFAULT 0,
    hls_url TEXT,
    dash_url TEXT,
    
    -- Metadata
    variants JSONB,
    metadata JSONB,
    
    -- Audit
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

#### XrSession (PostgreSQL)
```sql
CREATE TABLE xr_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    scene_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL, -- WEB, IOS, ANDROID, QUEST, VISION_PRO, CARDBOARD
    device_info JSONB,
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP,
    duration_seconds INTEGER,
    gaze_data JSONB,
    interactions JSONB,
    fps_avg FLOAT,
    load_time_ms INTEGER,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### AnalyticsEvent (ClickHouse)
```sql
CREATE TABLE xr_analytics_event (
    id String,
    tenant_id String,
    user_id String,
    scene_id String,
    session_id String,
    event_type String, -- SESSION_START, GAZE_ENTER, INTERACTION_CLICK, etc.
    event_data String,
    device_type String,
    timestamp DateTime
) ENGINE = MergeTree()
ORDER BY (tenant_id, scene_id, timestamp);
```

---

## 7. API DESIGN

### 7.1 API Gateway Routes

```yaml
# api-gateway application.yml routes

spring:
  cloud:
    gateway:
      routes:
        # IAM
        - id: iam-service
          uri: lb://iam-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**, /api/v1/roles/**
        
        # Tenant
        - id: tenant-service
          uri: lb://tenant-service
          predicates:
            - Path=/api/v1/tenants/**, /api/v1/subscriptions/**, /api/v1/plans/**
        
        # Assets
        - id: asset-service
          uri: lb://asset-service
          predicates:
            - Path=/api/v1/assets/**
          filters:
            - name: RequestSizeLimiter
              args:
                maxSize: 500MB
        
        # Scenes
        - id: scene-service
          uri: lb://scene-service
          predicates:
            - Path=/api/v1/scenes/**
        
        # Video
        - id: video-service
          uri: lb://video-service
          predicates:
            - Path=/api/v1/video-jobs/**
        
        # Streaming
        - id: streaming-service
          uri: lb://streaming-service
          predicates:
            - Path=/api/v1/streaming/**
        
        # XR Sessions
        - id: xr-service
          uri: lb://xr-service
          predicates:
            - Path=/api/v1/xr-sessions/**
        
        # AI
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/v1/ai/**
        
        # Analytics
        - id: analytics-service
          uri: lb://analytics-service
          predicates:
            - Path=/api/v1/analytics/**
        
        # Digital Twin
        - id: digital-twin-service
          uri: lb://digital-twin-service
          predicates:
            - Path=/api/v1/digital-twins/**
        
        # BIM/CAD
        - id: bim-cad-service
          uri: lb://bim-cad-service
          predicates:
            - Path=/api/v1/bim/**
        
        # GIS
        - id: gis-service
          uri: lb://gis-service
          predicates:
            - Path=/api/v1/gis/**
        
        # Collaboration
        - id: collaboration-service
          uri: lb://collaboration-service
          predicates:
            - Path=/api/v1/collaboration/**
```

### 7.2 Core API Endpoints

#### Scene Management
```
POST   /api/v1/scenes                          - Create scene
GET    /api/v1/scenes                          - List scenes (paginated)
GET    /api/v1/scenes/{id}                     - Get scene detail
PUT    /api/v1/scenes/{id}                     - Update scene
DELETE /api/v1/scenes/{id}                     - Delete scene (soft)
POST   /api/v1/scenes/{id}/publish             - Publish scene
POST   /api/v1/scenes/{id}/unpublish           - Unpublish scene
POST   /api/v1/scenes/{id}/duplicate           - Duplicate scene
GET    /api/v1/scenes/{id}/export              - Export scene as JSON

# Scene Nodes
POST   /api/v1/scenes/{sceneId}/nodes          - Add node
GET    /api/v1/scenes/{sceneId}/nodes           - List nodes
PUT    /api/v1/scenes/{sceneId}/nodes/{nodeId}  - Update node
DELETE /api/v1/scenes/{sceneId}/nodes/{nodeId}  - Delete node
PUT    /api/v1/scenes/{sceneId}/nodes/reorder   - Reorder nodes

# Hotspots
POST   /api/v1/scenes/{sceneId}/hotspots       - Create hotspot
GET    /api/v1/scenes/{sceneId}/hotspots        - List hotspots
PUT    /api/v1/scenes/{sceneId}/hotspots/{id}   - Update hotspot
DELETE /api/v1/scenes/{sceneId}/hotspots/{id}   - Delete hotspot
```

#### Asset Management
```
POST   /api/v1/assets                          - Upload asset (multipart)
GET    /api/v1/assets                          - List assets
GET    /api/v1/assets/{id}                     - Get asset detail
DELETE /api/v1/assets/{id}                     - Delete asset
POST   /api/v1/assets/{id}/process             - Start processing
GET    /api/v1/assets/{id}/streaming-url       - Get streaming URL
GET    /api/v1/assets/stats                    - Get asset statistics
```

#### 360 Video
```
POST   /api/v1/video-jobs                      - Create transcoding job
GET    /api/v1/video-jobs/{id}                 - Get job status
POST   /api/v1/video-jobs/{id}/process         - Start processing
POST   /api/v1/video-jobs/{id}/cancel          - Cancel job
GET    /api/v1/video-jobs/stats                - Get active job count
```

#### Streaming
```
POST   /api/v1/streaming/streams/{id}/manifest/hls  - Generate HLS manifest
POST   /api/v1/streaming/streams/{id}/manifest/dash  - Generate DASH manifest
GET    /api/v1/streaming/streams/{id}/manifest        - Get manifest URLs
GET    /api/v1/streaming/streams/{id}/playback        - Get playback URLs
DELETE /api/v1/streaming/streams/{id}/cache           - Invalidate cache
```

#### WebXR Sessions
```
POST   /api/v1/xr-sessions                     - Start XR session
POST   /api/v1/xr-sessions/{id}/end            - End session
GET    /api/v1/xr-sessions/{id}                - Get session
PUT    /api/v1/xr-sessions/{id}/gaze-data      - Update gaze data
PUT    /api/v1/xr-sessions/{id}/interactions   - Update interactions
PUT    /api/v1/xr-sessions/{id}/fps            - Update FPS
GET    /api/v1/xr-sessions/stats               - Get session stats
```

#### AI Services
```
POST   /api/v1/ai/scenes/generate              - Generate scene from prompt
POST   /api/v1/ai/scenes/refine                - Refine generated scene
POST   /api/v1/ai/narration/generate           - Generate narration (TTS)
POST   /api/v1/ai/translate                    - Translate text
POST   /api/v1/ai/tour-guide/create            - Create AI tour guide
POST   /api/v1/ai/tour-guide/respond           - Guide responds to query
POST   /api/v1/ai/hotspots/generate            - AI hotspot suggestions
```

#### Analytics
```
POST   /api/v1/analytics/events                - Track event
POST   /api/v1/analytics/events/batch          - Track events batch
GET    /api/v1/analytics/scenes/{id}/stats     - Scene analytics
GET    /api/v1/analytics/gaze-heatmap/{id}     - Gaze heatmap data
GET    /api/v1/analytics/scenes/top-views      - Top scenes by views
GET    /api/v1/analytics/scenes/top-interactions - Top scenes by interactions
```

#### Digital Twin
```
POST   /api/v1/digital-twins                   - Create digital twin
GET    /api/v1/digital-twins/{id}              - Get twin
PUT    /api/v1/digital-twins/{id}              - Update twin
DELETE /api/v1/digital-twins/{id}              - Delete twin
POST   /api/v1/digital-twins/{id}/sync         - Sync twin data
POST   /api/v1/digital-twins/{id}/iot-endpoints - Update IoT endpoints
```

#### BIM/CAD
```
POST   /api/v1/bim/upload                      - Upload IFC/Revit
GET    /api/v1/bim/viewer/{id}/metadata        - Get model metadata
GET    /api/v1/bim/viewer/{id}/hierarchy       - Get model hierarchy
GET    /api/v1/bim/viewer/{id}/statistics      - Get model statistics
```

#### GIS / Indoor Navigation
```
POST   /api/v1/gis/floors                      - Create floor
GET    /api/v1/gis/floors/{id}                 - Get floor
POST   /api/v1/gis/rooms                       - Create room
GET    /api/v1/gis/rooms/locate                - Find room by GPS
GET    /api/v1/gis/rooms/nearby                - Find nearby rooms
GET    /api/v1/gis/navigation/context          - Get navigation context
GET    /api/v1/gis/building/{id}/map           - Get building map
```

#### Collaboration
```
POST   /api/v1/collaboration/rooms             - Create room
GET    /api/v1/collaboration/rooms/{id}        - Get room
POST   /api/v1/collaboration/rooms/{id}/join   - Join room
POST   /api/v1/collaboration/rooms/{id}/leave  - Leave room
PUT    /api/v1/collaboration/rooms/{id}/avatar - Update avatar
PUT    /api/v1/collaboration/rooms/{id}/mute   - Toggle mute
```

---

## 8. EVENT-DRIVEN ARCHITECTURE

### 8.1 Kafka Topics

```
┌─────────────────────────────────────────────────────────────────┐
│                    KAFKA TOPICS                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ASSET EVENTS                                                     │
│  ├── xr.asset.uploaded          - New asset uploaded             │
│  ├── xr.asset.processed         - Asset processing complete      │
│  ├── xr.asset.transcoded        - Video transcoding complete     │
│  └── xr.asset.deleted            - Asset deleted                  │
│                                                                   │
│  SCENE EVENTS                                                     │
│  ├── xr.scene.created            - New scene created             │
│  ├── xr.scene.updated            - Scene updated                 │
│  ├── xr.scene.published          - Scene published               │
│  ├── xr.scene.unpublished        - Scene unpublished             │
│  └── xr.scene.deleted            - Scene deleted                  │
│                                                                   │
│  VIDEO EVENTS                                                     │
│  ├── xr.video.queued             - Video job queued              │
│  ├── xr.video.processing         - Video transcoding started     │
│  ├── xr.video.completed          - Video transcoding complete    │
│  └── xr.video.failed             - Video transcoding failed      │
│                                                                   │
│  SESSION EVENTS                                                   │
│  ├── xr.session.started          - XR session started            │
│  ├── xr.session.ended            - XR session ended              │
│  └── xr.session.gaze             - Gaze data captured            │
│                                                                   │
│  ANALYTICS EVENTS                                                 │
│  ├── xr.analytics.event          - Analytics event tracked       │
│  └── xr.analytics.batch          - Batch analytics events        │
│                                                                   │
│  AI EVENTS                                                        │
│  ├── xr.ai.generation.completed  - AI generation done            │
│  ├── xr.ai.narration.ready       - Narration generated           │
│  └── xr.ai.translation.done      - Translation completed         │
│                                                                   │
│  COLLABORATION EVENTS                                             │
│  ├── xr.collab.room.created      - Room created                  │
│  ├── xr.collab.user.joined       - User joined room              │
│  ├── xr.collab.user.left         - User left room                │
│  └── xr.collab.user.moved        - User position updated         │
│                                                                   │
│  DIGITAL TWIN EVENTS                                              │
│  ├── xr.twin.created             - Digital twin created          │
│  ├── xr.twin.synced              - Twin data synced              │
│  └── xr.twin.iot.data            - IoT data received             │
│                                                                   │
│  NOTIFICATION EVENTS                                              │
│  ├── xr.notification.push        - Push notification             │
│  ├── xr.notification.email       - Email notification            │
│  └── xr.notification.sms         - SMS notification              │
│                                                                   │
│  BILLING EVENTS                                                   │
│  ├── xr.billing.subscription.renewed  - Subscription renewed     │
│  ├── xr.billing.invoice.created       - Invoice created          │
│  └── xr.billing.payment.failed        - Payment failed           │
│                                                                   │
│  TOTAL: 30+ KAFKA TOPICS                                         │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Event Schema (CloudEvents)

```json
{
  "specversion": "1.0",
  "type": "com.vcall.xr.scene.published",
  "source": "scene-service",
  "id": "uuid",
  "time": "2026-01-01T00:00:00Z",
  "datacontenttype": "application/json",
  "data": {
    "tenantId": "uuid",
    "sceneId": "uuid",
    "sceneName": "string",
    "publishedBy": "uuid",
    "publishedAt": "datetime",
    "version": 1,
    "sceneType": "VR_TOUR"
  }
}
```

---

## 9. SECURITY ARCHITECTURE

### 9.1 Authentication & Authorization

```
┌─────────────────────────────────────────────────────────────────┐
│                    SECURITY ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  IDENTITY PROVIDER: Keycloak                                     │
│  ├── OAuth 2.0 / OpenID Connect (OIDC)                          │
│  ├── JWT Access Tokens (RS256)                                   │
│  ├── Refresh Token Rotation                                      │
│  ├── Multi-Factor Authentication (MFA)                           │
│  ├── Social Login (Google, Microsoft)                            │
│  └── SAML 2.0 (Enterprise SSO)                                  │
│                                                                   │
│  AUTHORIZATION: RBAC + ABAC                                      │
│  ├── Roles: SuperAdmin, TenantAdmin, ContentCreator,             │
│  │          3DDesigner, VRDesigner, ARDesigner, EndUser          │
│  ├── Permissions: scene.create, scene.publish, asset.upload,    │
│  │                analytics.view, billing.manage, tenant.admin   │
│  ├── Resource-based: Tenant isolation via TenantContext          │
│  └── Feature-based: Plan limits via FeatureFlag                  │
│                                                                   │
│  API SECURITY                                                     │
│  ├── JWT Validation at Gateway                                   │
│  ├── Rate Limiting (per-tenant, per-endpoint)                    │
│  ├── CORS Configuration                                          │
│  ├── Request Size Limits                                         │
│  ├── Input Validation (Bean Validation)                          │
│  ├── SQL Injection Prevention (JPA/Hibernate)                    │
│  ├── XSS Prevention (Output Encoding)                            │
│  └── CSRF Protection (SameSite cookies)                          │
│                                                                   │
│  CONTENT SECURITY                                                 │
│  ├── DRM (Widevine for video)                                    │
│  ├── Signed URLs (MinIO, CDN)                                    │
│  ├── Token-based Asset Access                                    │
│  ├── Encryption at Rest (AES-256)                                │
│  ├── Encryption in Transit (TLS 1.3)                             │
│  └── Watermarking (360 video, 3D models)                         │
│                                                                   │
│  DATA SECURITY                                                    │
│  ├── Multi-Tenant Data Isolation (Row-level security)            │
│  ├── PII Encryption (GDPR compliance)                            │
│  ├── Data Retention Policies                                     │
│  ├── Audit Logging (all write operations)                        │
│  └── Backup & Recovery Procedures                                │
│                                                                   │
│  VR/AR SPECIFIC SECURITY                                          │
│  ├── Camera Permission Management                                │
│  ├── Motion Sensor Permission                                    │
│  ├── Microphone Permission (Voice Chat)                          │
│  ├── Location Permission (AR Features)                           │
│  ├── WebView Security (Mobile VR)                                │
│  └── Session Timeout (VR comfort)                                │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 RBAC Permission Matrix

| Role | Scene | Asset | Video | Analytics | AI | Billing | Tenant | Admin |
|------|-------|-------|-------|-----------|-----|---------|--------|-------|
| SuperAdmin | CRUD | CRUD | CRUD | CRUD | CRUD | CRUD | CRUD | Full |
| TenantAdmin | CRUD (tenant) | CRUD (tenant) | CRUD (tenant) | Read (tenant) | Use (tenant) | Read (tenant) | Read | Full (tenant) |
| ContentCreator | CRUD (own) | Upload | Create | Read (own) | Use | - | - | - |
| 3DDesigner | CRUD (own) | Upload (3D) | - | Read (own) | Use | - | - | - |
| VRDesigner | CRUD (own) | Upload | Create | Read (own) | Use | - | - | - |
| ARDesigner | CRUD (own) | Upload | - | Read (own) | Use | - | - | - |
| EndUser | Read (published) | - | - | - | - | - | - | - |

---

## 10. DEVOPS & DEPLOYMENT

### 10.1 Container Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    CONTAINER ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  DOCKER IMAGES (37 services)                                     │
│  ├── vcall/service-registry:1.0                                  │
│  ├── vcall/config-server:1.0                                     │
│  ├── vcall/api-gateway:1.0                                       │
│  ├── vcall/iam-service:1.0                                       │
│  ├── vcall/tenant-service:1.0                                    │
│  ├── vcall/asset-service:1.0                                     │
│  ├── vcall/scene-service:1.0                                     │
│  ├── vcall/video-service:1.0                                     │
│  ├── vcall/streaming-service:1.0                                 │
│  ├── vcall/xr-service:1.0                                        │
│  ├── vcall/ai-service:1.0                                        │
│  ├── vcall/analytics-service:1.0                                 │
│  ├── vcall/collaboration-service:1.0                             │
│  ├── vcall/digital-twin-service:1.0                              │
│  ├── vcall/bim-cad-service:1.0                                   │
│  ├── vcall/gis-service:1.0                                       │
│  ├── vcall/billing-service:1.0                                   │
│  ├── vcall/notification-service:1.0                              │
│  ├── vcall/audit-service:1.0                                     │
│  ├── vcall/reporting-service:1.0                                 │
│  └── ... (remaining services)                                    │
│                                                                   │
│  INFRASTRUCTURE CONTAINERS                                        │
│  ├── postgres:16-alpine                                           │
│  ├── mongo:7.0                                                    │
│  ├── redis:7-alpine                                               │
│  ├── elasticsearch:8.13                                           │
│  ├── confluentinc/cp-kafka:7.6.0                                 │
│  ├── confluentinc/cp-zookeeper:7.6.0                             │
│  ├── clickhouse/clickhouse-server:24.3                           │
│  ├── minio/minio:latest                                           │
│  ├── prom/prometheus:latest                                       │
│  ├── grafana/grafana:latest                                       │
│  ├── grafana/loki:latest                                          │
│  └── keycloak/keycloak:latest                                    │
│                                                                   │
│  KUBERNETES                                                       │
│  ├── Namespace: vcall-xr                                          │
│  ├── Deployments: 37 microservices                               │
│  ├── StatefulSets: PostgreSQL, MongoDB, Kafka, ClickHouse        │
│  ├── Services: ClusterIP (internal), NodePort (external)         │
│  ├── Ingress: NGINX Ingress Controller                           │
│  ├── ConfigMaps: Centralized configuration                       │
│  ├── Secrets: Database credentials, API keys                     │
│  ├── HPA: Horizontal Pod Autoscaler (CPU/Memory)                 │
│  ├── PDB: Pod Disruption Budget                                  │
│  └── NetworkPolicies: Service-to-service communication           │
│                                                                   │
│  CI/CD PIPELINE                                                   │
│  ├── Build: Maven multi-module                                   │
│  ├── Test: Unit + Integration (Testcontainers)                   │
│  ├── Security: OWASP Dependency Check, Snyk                      │
│  ├── Container: Docker multi-stage build                         │
│  ├── Registry: Harbor / Docker Hub                               │
│  ├── Deploy: ArgoCD (GitOps)                                     │
│  └── Monitor: Prometheus + Grafana                               │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Kubernetes Resource Allocation

```yaml
# Resource limits per service tier

TIER 1 (High Traffic):
  api-gateway:
    replicas: 3-10
    resources: { cpu: "500m-2000m", memory: "512Mi-2Gi" }
  
  iam-service:
    replicas: 3-8
    resources: { cpu: "500m-1500m", memory: "512Mi-1.5Gi" }

TIER 2 (Core Business):
  scene-service:
    replicas: 3-6
    resources: { cpu: "500m-1500m", memory: "1Gi-2Gi" }
  
  asset-service:
    replicas: 3-6
    resources: { cpu: "500m-1500m", memory: "1Gi-2Gi" }
  
  video-service:
    replicas: 2-4
    resources: { cpu: "1000m-4000m", memory: "2Gi-4Gi" }

TIER 3 (Support):
  ai-service:
    replicas: 2-4
    resources: { cpu: "1000m-4000m", memory: "2Gi-4Gi" }
  
  analytics-service:
    replicas: 2-4
    resources: { cpu: "500m-2000m", memory: "1Gi-3Gi" }

TIER 4 (Background):
  streaming-service:
    replicas: 2-4
    resources: { cpu: "500m-1500m", memory: "512Mi-1.5Gi" }
  
  collaboration-service:
    replicas: 2-6
    resources: { cpu: "500m-2000m", memory: "1Gi-2Gi" }
```

---

## 11. ENTERPRISE FEATURES

### 11.1 Digital Twin Platform
```
┌─────────────────────────────────────────────────────────────────┐
│                    DIGITAL TWIN ARCHITECTURE                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  DATA SOURCES                                                     │
│  ├── BIM Models (IFC, Revit) ──► BIM/CAD Service                │
│  ├── IoT Sensors ──► MQTT/HTTP → Digital Twin Service            │
│  ├── GIS Data ──► PostGIS → GIS Service                          │
│  ├── Point Clouds ──► LAS/LAZ → Asset Service                   │
│  └── Real-time Feeds ──► WebSocket → Collaboration Service       │
│                                                                   │
│  TWIN TYPES                                                       │
│  ├── Building Twin ──► Full BIM + IoT integration               │
│  ├── Campus Twin ──► Multi-building + GIS                       │
│  ├── City Twin ──► Urban-scale visualization                     │
│  ├── Asset Twin ──► Individual equipment monitoring              │
│  └── Process Twin ──► Workflow simulation                        │
│                                                                   │
│  CAPABILITIES                                                     │
│  ├── Real-time Monitoring ──► Live sensor data in 3D            │
│  ├── Predictive Maintenance ──► AI anomaly detection             │
│  ├── Energy Optimization ──► Consumption visualization           │
│  ├── Space Management ──► Occupancy tracking                     │
│  ├── Emergency Simulation ──► Evacuation planning                │
│  └── Integration APIs ──► BACnet, Modbus, OPC-UA                 │
└─────────────────────────────────────────────────────────────────┘
```

### 11.2 Multi-User VR Session
```
┌─────────────────────────────────────────────────────────────────┐
│                    MULTI-USER VR ARCHITECTURE                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  PROTOCOL: WebSocket + WebRTC (Data Channel)                     │
│                                                                   │
│  STATE SYNCHRONIZATION                                            │
│  ├── Avatar Position ──► 60Hz broadcast                          │
│  ├── Head Rotation ──► 60Hz broadcast                            │
│  ├── Hand Pose ──► 30Hz broadcast                                │
│  ├── Voice Audio ──► WebRTC peer-to-peer                         │
│  ├── Screen Share ──► WebRTC video track                         │
│  ├── Object Interaction ──► CRDT conflict resolution            │
│  └── Scene State ──► Eventual consistency                        │
│                                                                   │
│  AVATAR SYSTEM                                                    │
│  ├── Predefined Avatars ──► Library of 3D characters             │
│  ├── Custom Avatar ──► Upload photo → AI generate                │
│  ├── Body Tracking ──► IK (Inverse Kinematics)                   │
│  ├── Face Tracking ──► Blend shapes (Quest Pro, Vision Pro)      │
│  └── Lip Sync ──► Audio-driven mouth animation                   │
│                                                                   │
│  SCALABILITY                                                      │
│  ├── Max 50 users per room (standard)                            │
│  ├── Max 200 users per room (enterprise)                         │
│  ├── Spatial Audio ──► Distance-based volume                     │
│  ├── LOD for Avatars ──► Reduce detail at distance               │
│  └── Interest Management ──► Only sync visible objects           │
└─────────────────────────────────────────────────────────────────┘
```

### 11.3 AI NPC Guide
```
┌─────────────────────────────────────────────────────────────────┐
│                    AI NPC / TOUR GUIDE ARCHITECTURE               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  AI ENGINE: OpenAI GPT-4 / Claude / Gemini                       │
│                                                                   │
│  NPC TYPES                                                        │
│  ├── Museum Guide ──► Explains exhibits, answers questions       │
│  ├── Tour Guide ──► Leads visitors through scenes                │
│  ├── Sales Assistant ──► Shows products, answers FAQs           │
│  ├── Training Instructor ──► Guides training scenarios           │
│  ├── Receptionist ──► Welcomes visitors, provides info           │
│  └── Custom Persona ──► Define custom NPC character              │
│                                                                   │
│  CAPABILITIES                                                     │
│  ├── Natural Language Understanding                               │
│  ├── Context-Aware Responses (scene content knowledge)           │
│  ├── Multilingual (AI Translation integration)                   │
│  ├── Text-to-Speech (AI Narration integration)                   │
│  ├── Gesture Animation (idle, talking, pointing, walking)        │
│  ├── Pathfinding (A* navigation in 3D space)                     │
│  ├── Waypoint System (lead visitors between hotspots)            │
│  ├── Knowledge Base (upload exhibit/product info)                │
│  └── Fallback to Human Agent                                     │
│                                                                   │
│  INTEGRATION                                                      │
│  ├── WebSocket ──► Real-time conversation                        │
│  ├── Spatial Audio ──► Voice sounds from NPC position            │
│  ├── Lip Sync ──► Animated mouth during speech                   │
│  └── Analytics ──► Track NPC interactions                        │
└─────────────────────────────────────────────────────────────────┘
```

### 11.4 VR Commerce
```
┌─────────────────────────────────────────────────────────────────┐
│                    VR COMMERCE ARCHITECTURE                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  SHOWROOM FEATURES                                                │
│  ├── 3D Product Display ──► Interactive product models            │
│  ├── Product Hotspots ──► Info, pricing, add-to-cart             │
│  ├── Size/Color Picker ──► In-VR customization                   │
│  ├── 360 Product View ──► Spin and zoom                          │
│  ├── AR Try-On ──► Place product in real environment             │
│  └── Virtual Fitting ──► Clothing/accessory preview              │
│                                                                   │
│  CHECKOUT FLOW                                                    │
│  ├── Cart Management ──► Add/remove from VR                      │
│  ├── Payment Gateway ──► Stripe, PayPal integration              │
│  ├── Order Confirmation ──► In-VR receipt                        │
│  └── Analytics ──► Conversion tracking                           │
│                                                                   │
│  LMS VR TRAINING                                                  │
│  ├── Course Management ──► Create training modules                │
│  ├── Progress Tracking ──► Completion status                      │
│  ├── Assessment ──► Quiz, simulation scoring                     │
│  ├── Certificate ──► Blockchain-verified certificates            │
│  └── SCORM/xAPI Integration ──► LMS compatibility                │
└─────────────────────────────────────────────────────────────────┘
```

### 11.5 Vision Pro & Meta Quest Optimization

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEVICE OPTIMIZATION                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  APPLE VISION PRO                                                │
│  ├── RealityKit Integration                                       │
│  ├── Shared Space (multi-app)                                    │
│  ├── Full Space (immersive)                                      │
│  ├── Hand Tracking ──► Pinch, grab, point                        │
│  ├── Eye Tracking ──► Gaze-based selection                       │
│  ├── Spatial Persona ──► Memoji integration                      │
│  ├── VisionOS WebView ──► WebXR content                          │
│  ├── Optic ID ──► Biometric authentication                       │
│  └── Performance ──► 90Hz, foveated rendering                    │
│                                                                   │
│  META QUEST (2/3/Pro)                                            │
│  ├── OpenXR Runtime                                               │
│  ├── Quest Browser ──► WebXR content                             │
│  ├── Hand Tracking ──► v2 hand tracking                          │
│  ├── Eye Tracking ──► Quest Pro/3 only                           │
│  ├── Passthrough ──► Mixed reality (Quest 3)                     │
│  ├── Spatial Anchors ──► Persistent AR markers                   │
│  ├── Scene Understanding ──► Automatic room mapping              │
│  ├── Voice SDK ──► Voice commands                                │
│  └── Performance ──► 72/90/120Hz, foveated rendering             │
│                                                                   │
│  WEBGPU RENDERER (Future)                                        │
│  ├── Better GPU utilization than WebGL                            │
│  ├── Compute shaders ──► Physics, particles                      │
│  ├── Ray tracing ──► Realistic reflections                        │
│  └── Fallback to WebGL ──► Progressive enhancement               │
│                                                                   │
│  PIXEL STREAMING (For heavy 3D scenes)                           │
│  ├── Server-side rendering (Unreal/Unity)                         │
│  ├── WebRTC video stream to client                               │
│  ├── Input forwarding ──► Controller/keyboard/mouse              │
│  ├── Scalability ──► GPU-per-user on server                      │
│  └── Use case ──► High-fidelity showrooms, city twins            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12. PERFORMANCE REQUIREMENTS

### 12.1 Scale Targets

| Metric | Target | Strategy |
|--------|--------|----------|
| Concurrent Users | 1,000,000 | Horizontal scaling, CDN, caching |
| 8K Video Streaming | 10,000 concurrent | Tiled DASH, CDN edge caching |
| Asset Upload | 10,000/hour | Async processing, Kafka queue |
| Scene Rendering | < 3s load time | LOD, lazy loading, compression |
| API Response | < 200ms (p99) | Redis caching, connection pooling |
| WebSocket | 100,000 concurrent | Sticky sessions, Redis pub/sub |
| Storage | 100TB+ | MinIO distributed, lifecycle policies |

### 12.2 Performance Optimization Checklist

```
VR PERFORMANCE (Critical - must maintain 72+ FPS)
├── Polygon budget: < 500K triangles per frame
├── Draw calls: < 100 per frame
├── Texture memory: < 512MB per scene
├── Initial load: < 5 seconds
├── Scene transition: < 2 seconds
├── Stereoscopic: 2x render (one per eye)
├── Foveated rendering: Render center at full quality
├── Late latching: Reduce motion-to-photon latency
├── Async timewarp: Smooth head tracking
└── Level of Detail: Auto-adjust based on FPS

STREAMING PERFORMANCE
├── 4K streaming: 25 Mbps bitrate
├── 6K streaming: 40 Mbps bitrate
├── 8K streaming: 60 Mbps bitrate
├── Tiled DASH: 60-80% bandwidth reduction
├── Adaptive bitrate: Switch within 2 seconds
├── CDN cache hit ratio: > 95%
└── Buffer startup: < 2 seconds

API PERFORMANCE
├── Authentication: < 100ms
├── Scene list: < 200ms
├── Scene detail: < 300ms
├── Asset upload: Async (returns 202)
├── Video transcoding: Background job
├── AI generation: < 10 seconds
└── Analytics query: < 1 second
```

---

## 13. TESTING STRATEGY

### 13.1 Test Pyramid

```
┌─────────────────────────────────────────────────────────────────┐
│                    TESTING PYRAMID                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  E2E TESTS (5%)                                                   │
│  ├── Playwright (Web) ──► Full user journey                      │
│  ├── Appium (Mobile) ──► Mobile VR/AR flow                       │
│  └── Cypress ──► Scene builder workflow                          │
│                                                                   │
│  INTEGRATION TESTS (25%)                                          │
│  ├── Testcontainers ──► Database, Kafka, MinIO                   │
│  ├── API Contract ──► Pact testing                               │
│  ├── Kafka Consumer ──► Event verification                       │
│  └── WebSocket ──► Real-time message testing                     │
│                                                                   │
│  UNIT TESTS (70%)                                                 │
│  ├── Service Layer ──► Business logic                            │
│  ├── Repository Layer ──► Data access                            │
│  ├── Controller Layer ──► API validation                         │
│  └── Utility ──► Helper functions                                │
│                                                                   │
│  XR-SPECIFIC TESTS                                                │
│  ├── VR Session Test ──► Session lifecycle                       │
│  ├── Streaming Test ──► HLS/DASH manifest validation             │
│  ├── Asset Processing ──► Transcoding pipeline                   │
│  ├── Scene Rendering ──► Browser-based 3D validation             │
│  ├── Mobile VR ──► Gyroscope, camera permission                  │
│  └── Performance ──► FPS benchmark, load time                    │
│                                                                   │
│  LOAD TESTS (k6 / Gatling)                                        │
│  ├── 1M concurrent users ──► Gateway + services                  │
│  ├── 10K concurrent 8K streams ──► Streaming service             │
│  ├── 100K WebSocket connections ──► Collaboration service        │
│  └── Spike test ──► Sudden traffic surge                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 14. GAP ANALYSIS

### 14.1 What Exists vs What's Needed

| Module | Current State | Gap | Priority |
|--------|--------------|-----|----------|
| Scene Builder | CRUD + basic UI | Full drag-and-drop 3D editor, real-time preview | HIGH |
| VR Player | Placeholder | Full WebXR/ThreeJS/AFrame player with all modes | HIGH |
| 360 Video Player | Placeholder | Equirectangular rendering, stereo support | HIGH |
| Asset Processing | Basic upload | FFmpeg transcoding pipeline, format conversion | HIGH |
| Streaming | Manifest generation | Tiled DASH, ABR, CDN integration | HIGH |
| AR Builder | Not implemented | ARCore/ARKit integration, surface detection | HIGH |
| XR Player (Mobile) | Not implemented | Native Kotlin/Swift VR/AR apps | HIGH |
| Collaboration | Basic CRUD | WebSocket/WebRTC real-time sync | MEDIUM |
| Digital Twin | Basic CRUD | IoT integration, real-time sync | MEDIUM |
| BIM Viewer | Basic IFC parse | Real IFC SDK, glTF conversion | MEDIUM |
| GIS Navigation | Basic PostGIS | Indoor navigation algorithm, pathfinding | MEDIUM |
| AI Scene Gen | Basic OpenAI | Full scene generation, hotspot suggestion | MEDIUM |
| Analytics | Basic events | Gaze heatmap, session replay, AI insights | MEDIUM |
| Cardboard SDK | Not implemented | Android/iOS Cardboard integration | MEDIUM |
| Meta Quest | Not implemented | OpenXR integration, passthrough | LOW |
| Vision Pro | Not implemented | RealityKit, hand/eye tracking | LOW |
| WebGPU | Not implemented | Progressive enhancement from WebGL | LOW |
| Pixel Streaming | Not implemented | Server-side rendering pipeline | LOW |
| Avatar System | Not implemented | 3D avatar library, IK, face tracking | LOW |
| VR Commerce | Not implemented | Product display, checkout flow | LOW |
| LMS Integration | Not implemented | SCORM/xAPI, progress tracking | LOW |

### 14.2 Missing Microservices

| Service | Purpose | Priority |
|---------|---------|----------|
| avatar-service | Avatar management, customization | MEDIUM |
| commerce-service | Product catalog, cart, checkout | MEDIUM |
| lms-service | Training courses, progress, certificates | MEDIUM |
| voice-service | Spatial voice chat, voice commands | MEDIUM |
| presence-service | Real-time user presence, location | HIGH |
| search-service | Elasticsearch indexing, full-text search | MEDIUM |

### 14.3 Missing Frontend Components

| Component | Purpose | Priority |
|-----------|---------|----------|
| VR Scene Editor | Real-time 3D scene editing | HIGH |
| VR Player Component | Full-featured VR video player | HIGH |
| 3D Model Viewer | Interactive 3D model display | HIGH |
| AR Viewer Component | WebAR experience | HIGH |
| Gaze Heatmap | Visual gaze tracking overlay | MEDIUM |
| Avatar Picker | Avatar selection/customization | MEDIUM |
| Spatial Chat UI | Voice chat in VR | MEDIUM |

### 14.4 Missing Mobile Features

| Feature | Platform | Priority |
|---------|----------|----------|
| Native VR Player | Android/iOS | HIGH |
| Cardboard Integration | Android/iOS | HIGH |
| AR Camera View | Android/iOS | HIGH |
| Gyroscope 360 View | Android/iOS | HIGH |
| Offline Mode | Android/iOS | MEDIUM |
| Push Notifications | Android/iOS | MEDIUM |

---

## 15. IMPLEMENTATION ROADMAP

### Phase 1: Foundation (Weeks 1-4)
- [ ] Complete asset processing pipeline (FFmpeg, format conversion)
- [ ] Implement 360 video transcoding (4K/6K/8K)
- [ ] Build HLS/DASH streaming with ABR
- [ ] Implement basic VR player (WebXR + ThreeJS + AFrame)
- [ ] Scene builder drag-and-drop editor
- [ ] Dashboard and admin UI

### Phase 2: Core Features (Weeks 5-8)
- [ ] Stereoscopic 360 video support
- [ ] Cardboard mode (gyroscope control)
- [ ] Spatial audio integration
- [ ] AI scene generation
- [ ] AI hotspot suggestions
- [ ] Virtual tour builder
- [ ] Virtual showroom builder

### Phase 3: Enterprise (Weeks 9-12)
- [ ] Digital twin platform
- [ ] BIM/CAD viewer (proper IFC parsing)
- [ ] Indoor navigation (GIS)
- [ ] Multi-user VR collaboration
- [ ] Voice chat (WebRTC)
- [ ] Avatar system

### Phase 4: Mobile & Advanced (Weeks 13-16)
- [ ] Android native VR player (Kotlin + Cardboard SDK)
- [ ] iOS native VR player (Swift + Cardboard SDK)
- [ ] AR experience builder
- [ ] ARCore/ARKit integration
- [ ] Meta Quest optimization
- [ ] Apple Vision Pro optimization

### Phase 5: AI & Analytics (Weeks 17-20)
- [ ] AI NPC tour guide
- [ ] AI narration (multilingual)
- [ ] Gaze heatmap analytics
- [ ] Session replay
- [ ] AI-powered insights
- [ ] Predictive analytics

### Phase 6: Scale & Polish (Weeks 21-24)
- [ ] CDN global distribution
- [ ] WebGPU renderer (progressive)
- [ ] Pixel streaming (heavy scenes)
- [ ] VR commerce
- [ ] LMS integration
- [ ] Performance optimization for 1M users
- [ ] Security audit
- [ ] Load testing

---

## 16. MONITORING & OBSERVABILITY

```
┌─────────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY STACK                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  METRICS: Prometheus + Grafana                                    │
│  ├── Service metrics (CPU, memory, request rate, error rate)     │
│  ├── Business metrics (scenes created, sessions, uploads)        │
│  ├── XR metrics (FPS, load time, device distribution)            │
│  ├── Streaming metrics (bitrate, buffer, CDN hit ratio)          │
│  └── Custom dashboards per service                               │
│                                                                   │
│  LOGGING: Loki + Grafana                                          │
│  ├── Structured logging (JSON)                                    │
│  ├── Log aggregation across all services                         │
│  ├── Log-based alerting                                          │
│  └── Trace correlation                                            │
│                                                                   │
│  TRACING: OpenTelemetry + Jaeger                                  │
│  ├── Distributed tracing across services                         │
│  ├── Trace: Gateway → Service → Database → Kafka                 │
│  ├── Span analysis for slow requests                             │
│  └── Error root cause analysis                                   │
│                                                                   │
│  ALERTING: Grafana Alerting                                       │
│  ├── Service down                                                 │
│  ├── Error rate > 1%                                              │
│  ├── Response time > 500ms                                        │
│  ├── Disk usage > 80%                                             │
│  ├── Kafka consumer lag                                           │
│  ├── VR session FPS < 30                                         │
│  └── Streaming buffer underrun                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 17. COMPETITIVE ANALYSIS

### Feature Comparison with Matterport

| Feature | Our Platform | Matterport | Advantage |
|---------|-------------|-----------|-----------|
| 360 Video | ✅ Mono + Stereo | ❌ | More formats |
| 3D Scan Processing | ✅ (planned) | ✅ Core | Parity target |
| VR Tour | ✅ | ✅ | Equal |
| VR Showroom | ✅ | ❌ | Differentiator |
| VR Training | ✅ | ❌ | Differentiator |
| VR Museum | ✅ | ❌ | Differentiator |
| AR Experience | ✅ | ✅ (limited) | More AR features |
| Digital Twin | ✅ | ✅ | Equal |
| BIM Viewer | ✅ | ❌ | Differentiator |
| AI NPC Guide | ✅ | ❌ | Differentiator |
| Multi-user VR | ✅ | ❌ | Differentiator |
| Metaverse | ✅ | ❌ | Differentiator |
| No-code Builder | ✅ | ✅ | Equal |
| Multi-tenant SaaS | ✅ | ✅ | Equal |
| Web + Mobile + VR | ✅ | ✅ (limited) | More devices |
| Meta Quest | ✅ | ✅ | Equal |
| Vision Pro | ✅ | ❌ | First mover |
| Price | Lower | Higher | Cost advantage |
| Asian Market | ✅ Focus | ❌ | Regional advantage |

---

*Document Version: 1.0*
*Last Updated: 2026-01-01*
*Author: XR Platform Architecture Team*
