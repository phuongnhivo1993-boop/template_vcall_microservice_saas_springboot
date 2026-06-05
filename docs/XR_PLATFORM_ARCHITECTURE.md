# XR SaaS Enterprise Platform - Architecture Overview

## 1. PRODUCT VISION

**XRVista** - Enterprise XR SaaS Platform

Cho phép doanh nghiệp tạo, quản lý, phân phối và triển khai trải nghiệm VR/AR/XR trên mọi thiết bị mà không cần lập trình.

### Competitive Positioning
| Feature | XRVista | Matterport | Unity Cloud | Mozilla Hubs |
|---------|---------|------------|-------------|--------------|
| No-code VR Builder | ✅ | ✅ | ❌ | ❌ |
| 360 Video Player | ✅ | Partial | ❌ | ❌ |
| AR Experience | ✅ | ❌ | ✅ | ❌ |
| WebXR Native | ✅ | ❌ | ❌ | ✅ |
| Meta Quest | ✅ | ✅ | ✅ | ❌ |
| Apple Vision Pro | ✅ | ❌ | ✅ | ❌ |
| Multi-tenant SaaS | ✅ | ✅ | ❌ | ❌ |
| AI Scene Generation | ✅ | ❌ | ❌ | ❌ |
| Digital Twin | ✅ | ✅ | ✅ | ❌ |
| BIM/CAD Viewer | ✅ | Partial | ✅ | ❌ |
| Multi-user VR | ✅ | ✅ | ✅ | ✅ |
| Price/User/Month | $29-199 | $50-300 | Custom | Free |

---

## 2. SYSTEM ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                      │
├──────────┬──────────┬──────────┬──────────┬──────────┬─────────────────┤
│ Web SPA  │ iOS App  │Android   │ Meta     │ Apple    │ Cardboard       │
│ Next.js  │ Native   │ Native   │ Quest    │ Vision   │ Viewer          │
│ +ThreeJS │ Swift    │ Kotlin   │ App      │ Pro App  │                 │
│ +AFrame  │          │          │          │          │                 │
│ +WebXR   │          │          │          │          │                 │
└────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┴────────┬────────┘
     │          │          │          │          │              │
     └──────────┴──────────┴──────────┴──────────┴──────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   CDN / Edge      │
                    │   CloudFront      │
                    │   (8K Streaming)  │
                    └─────────┬─────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────────────┐
│                        API GATEWAY (Spring Cloud Gateway)                │
│   JWT Auth │ Rate Limiting │ Tenant Routing │ CORS │ Request Transform  │
└─────────────────────────────┬───────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────────────┐
│                     MICROSERVICES LAYER                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │ IAM Service  │ │Tenant Service│ │Asset Service │ │Scene Service │   │
│  │ (8101)       │ │ (8120)       │ │ (8121)       │ │ (8122)       │   │
│  │ Auth, RBAC   │ │ Multi-Tenant │ │ Upload, Transcode │ │ VR/AR Builder │  │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘   │
│                                                                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │Video Service │ │Stream Service│ │ XR Service   │ │AI Service    │   │
│  │ (8123)       │ │ (8124)       │ │ (8125)       │ │ (8126)       │   │
│  │ 360 Encode   │ │ HLS/DASH     │ │ WebXR,Quest  │ │ Scene Gen    │   │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘   │
│                                                                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │Analytics Svc │ │Billing Svc   │ │Notification  │ │Collaboration │   │
│  │ (8127)       │ │ (8128)       │ │ (8118)       │ │ (8129)       │   │
│  │ Gaze,Heatmap │ │ Subscription │ │ Push,Email   │ │ Multi-user   │   │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘   │
│                                                                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                    │
│  │Digital Twin  │ │BIM/CAD Svc   │ │GIS Service   │                    │
│  │ (8130)       │ │ (8131)       │ │ (8132)       │                    │
│  │ IoT, Sync    │ │ IFC, Revit   │ │ Indoor Nav   │                    │
│  └──────────────┘ └──────────────┘ └──────────────┘                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────────────┐
│                     EVENT BUS (Apache Kafka)                             │
│   asset.uploaded │ scene.published │ video.transcoded │ user.joined     │
│   analytics.event │ xr.session.start │ ai.generation.complete          │
└─────────────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────────────┐
│                        DATA LAYER                                        │
├──────────┬──────────┬──────────┬──────────┬──────────┬─────────────────┤
│PostgreSQL│ Redis    │Elastic-  │ MinIO    │ Click-   │ MongoDB         │
│ (16)     │ (7)      │search    │ (S3)     │ House    │ (Scene JSON)   │
│          │          │ (8.13)   │          │ (24.3)   │                 │
└──────────┴──────────┴──────────┴──────────┴──────────┴─────────────────┘
```

---

## 3. MICROSERVICES DESIGN

### 3.1 New XR Microservices

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| `tenant-service` | 8120 | vcall_tenant | Multi-tenant management, subscription, feature flags |
| `asset-service` | 8121 | vcall_asset | Upload, store, transcode 360video/3D/audio |
| `scene-service` | 8122 | vcall_scene | VR/AR/XR scene CRUD, builder state, publish |
| `video-service` | 8123 | vcall_video | 360 video processing, monoscopic/stereoscopic |
| `streaming-service` | 8124 | - | HLS/DASH manifest, CDN origin, adaptive bitrate |
| `xr-service` | 8125 | vcall_xr | WebXR sessions, device management, XR rendering config |
| `ai-service` | 8126 | vcall_ai | AI scene gen, hotspot gen, narration, translation |
| `analytics-service` | 8127 | vcall_analytics + ClickHouse | Session tracking, gaze, heatmap, device stats |
| `billing-service` | 8128 | vcall_billing | Subscription plans, invoicing, usage metering |
| `collaboration-service` | 8129 | vcall_collab | Multi-user VR, voice chat, avatar, screen share |
| `digital-twin-service` | 8130 | vcall_twin | Digital twin sync, IoT integration, BIM |
| `bim-cad-service` | 8131 | vcall_bim | IFC/Revit/CAD file parsing, BIM viewer API |
| `gis-service` | 8132 | vcall_gis + PostGIS | Indoor navigation, GIS map, spatial queries |

### 3.2 Existing Services (Retained)

| Service | Enhancement |
|---------|-------------|
| iam-service (8101) | Add XR-specific roles: VR_DESIGNER, AR_DESIGNER, CONTENT_CREATOR, VIEWER |
| billing-service (8115) | Extend with XR subscription tiers, usage-based billing for streaming |
| notification-service (8118) | Add push notification for VR devices, Web Push |
| audit-service (8119) | Extend with XR session audit, asset access audit |

---

## 4. DOMAIN-DRIVEN DESIGN

### 4.1 Bounded Contexts

```
┌─────────────────────────────────────────────────────────────┐
│                    XR PLATFORM DOMAIN                        │
├─────────────┬─────────────┬─────────────┬──────────────────┤
│ Tenant      │ Asset       │ Scene       │ Experience       │
│ Context     │ Context     │ Context     │ Context          │
│             │             │             │                  │
│ - Tenant    │ - Asset     │ - Scene     │ - VRSession      │
│ - Plan      │ - Upload    │ - Hotspot   │ - ARSession      │
│ - Feature   │ - Transcode │ - Node      │ - XRSession      │
│ - Billing   │ - Storage   │ - Timeline  │ - Analytics      │
├─────────────┼─────────────┼─────────────┼──────────────────┤
│ Streaming   │ Collab      │ AI          │ Device           │
│ Context     │ Context     │ Context     │ Context          │
│             │             │             │                  │
│ - HLS       │ - Room      │ - Generator │ - Headset        │
│ - DASH      │ - Avatar    │ - Narrator  │ - Mobile         │
│ - CDN       │ - VoiceChat │ - Translator│ - Browser        │
│ - Adaptive  │ - Screen    │ - Guide     │ - Cardboard      │
└─────────────┴─────────────┴─────────────┴──────────────────┘
```

### 4.2 Core Entities

```java
// Tenant Context
@Entity
public class Tenant {
    UUID id;
    String name;
    String slug;
    TenantPlan plan;        // FREE, STARTER, PRO, ENTERPRISE
    TenantStatus status;
    FeatureFlags features;
    DateTime createdAt;
}

// Asset Context
@Entity
public class Asset {
    UUID id;
    UUID tenantId;
    AssetType type;         // VIDEO_360, IMAGE_360, MODEL_3D, AUDIO, PANORAMA
    String originalUrl;
    String processedUrl;
    AssetMetadata metadata; // resolution, format, size, duration
    TranscodeStatus status;
    DateTime createdAt;
}

// Scene Context
@Entity
public class Scene {
    UUID id;
    UUID tenantId;
    String name;
    SceneType type;         // VR_TOUR, VR_SHOWROOM, VR_MUSEUM, VR_TRAINING, AR_EXPERIENCE, XR_SPACE
    SceneGraph sceneGraph;  // JSON - stored in MongoDB
    List<Hotspot> hotspots;
    PublishStatus status;
    String publishedUrl;
    DateTime createdAt;
}

// Experience Context
@Entity
public class XRSession {
    UUID id;
    UUID userId;
    UUID sceneId;
    DeviceType device;      // WEB, IOS, ANDROID, QUEST, VISION_PRO, CARDBOARD
    SessionMetrics metrics; // viewTime, gazeData, interactions
    DateTime startedAt;
}
```

---

## 5. HEXAGONAL ARCHITECTURE (Per Service)

```
┌─────────────────────────────────────────────────────┐
│                  DRIVING ADAPTERS                     │
│  REST Controller │ Kafka Consumer │ gRPC Server      │
├─────────────────────────────────────────────────────┤
│                    PORTS                              │
│  InboundPort           │  OutboundPort               │
│  (UseCase Interface)   │  (Repository Interface)     │
├─────────────────────────────────────────────────────┤
│                 APPLICATION CORE                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ Use Cases   │  │ Domain Model │  │ Domain Svc  │ │
│  │ (Commands)  │  │ (Entities)   │  │ (Logic)     │ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
├─────────────────────────────────────────────────────┤
│               DRIVEN ADAPTERS                        │
│  JPA Repository │ Kafka Producer │ MinIO Client     │
│  Redis Cache    │ Elasticsearch  │ FFmpeg Processor │
└─────────────────────────────────────────────────────┘
```

### CQRS Pattern

```
Command Side (Write):
  CreateSceneCommand → SceneAggregate → SceneRepository → PostgreSQL
  
Query Side (Read):
  GetSceneQuery → SceneReadModel → Elasticsearch/Redis
```

---

## 6. ASSET PROCESSING PIPELINE

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Upload  │───▶│ Validate │───▶│ Process  │───▶│ Store    │───▶│ Notify   │
│  Client  │    │ Metadata │    │ FFmpeg   │    │ MinIO    │    │ Kafka    │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                  ▼
              ┌──────────┐    ┌──────────┐    ┌──────────┐
              │ 360 Video│    │ 3D Model │    │ Audio    │
              │ Transcode│    │ Optimize │    │ Process  │
              │          │    │          │    │          │
              │ - 4K     │    │ - GLB    │    │ - Spatial│
              │ - 6K     │    │ - Draco  │    │ - Format │
              │ - 8K     │    │ - KTX2   │    │ - Normalize│
              │ - HLS    │    │ - LOD    │    │          │
              │ - DASH   │    │ - HDR    │    │          │
              └──────────┘    └──────────┘    └──────────┘
```

### FFmpeg Processing Chain for 360 Video

```bash
# Monoscopic 360 → HLS with multiple quality levels
ffmpeg -i input_360.mp4 \
  -vf "scale=3840:2160,equirectangular" \
  -c:v libx265 -crf 23 \
  -hls_time 6 -hls_list_size 0 \
  -hls_segment_filename "segment_%03d.ts" \
  output_master.m3u8

# Stereoscopic 360 (top-bottom) → Split + encode
ffmpeg -i input_stereo_360.mp4 \
  -vf "crop=iw:ih/2:0:0" left.mp4 \
  -vf "crop=iw:ih/2:0:ih/2" right.mp4

# Adaptive bitrate ladder
# 8K  (7680x4320) → 40Mbps  → master.m3u8
# 6K  (5760x2880) → 25Mbps  → variant_6k.m3u8
# 4K  (3840x2160) → 15Mbps  → variant_4k.m3u8
# 2K  (2560x1440) → 8Mbps   → variant_2k.m3u8
# FHD (1920x1080) → 5Mbps   → variant_1080.m3u8
# HD  (1280x720)  → 2.5Mbps → variant_720.m3u8
```

---

## 7. STREAMING ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CDN (CloudFront / Cloudflare)                 │
│   Edge Locations ─── Cache 360 Video Segments ─── Origin Shield    │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │  Streaming Server │
                    │  (Nginx + HLS)    │
                    │                   │
                    │  - Master.m3u8    │
                    │  - Variant playlists│
                    │  - TS/CMF segments │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │   MinIO / S3      │
                    │   (Origin Store)  │
                    │                   │
                    │  bucket:          │
                    │  xr-{tenant}/     │
                    │    /video/        │
                    │    /3d/           │
                    │    /audio/        │
                    │    /thumbnails/   │
                    └───────────────────┘
```

### Adaptive Streaming Logic

```typescript
// Client-side adaptive quality selection
class AdaptiveQualitySelector {
  private currentQuality: QualityLevel;
  private networkMonitor: NetworkMonitor;
  private deviceCapabilities: DeviceCapabilities;
  
  async selectOptimalQuality(): Promise<QualityLevel> {
    const bandwidth = await this.networkMonitor.estimateBandwidth();
    const gpu = this.deviceCapabilities.gpuLevel;
    const isVR = this.deviceCapabilities.isVRMode;
    
    // VR requires minimum 4K per eye
    if (isVR && bandwidth > 20_000_000) return '8K';
    if (isVR && bandwidth > 10_000_000) return '6K';
    if (isVR) return '4K';
    
    // Non-VR adaptive
    if (bandwidth > 15_000_000 && gpu >= 3) return '4K';
    if (bandwidth > 8_000_000) return '2K';
    if (bandwidth > 5_000_000) return 'FHD';
    return 'HD';
  }
}
```

---

## 8. WebXR + ThreeJS + AFrame INTEGRATION

### 8.1 WebXR API Integration

```typescript
// WebXR Session Manager
class WebXRManager {
  private session: XRSession;
  private referenceSpace: XRReferenceSpace;
  
  async initVR(): Promise<void> {
    if (!navigator.xr) throw new WebXRNotSupported();
    
    const supported = await navigator.xr.isSessionSupported('immersive-vr');
    if (!supported) throw new ImmersiveVRNotSupported();
    
    this.session = await navigator.xr.requestSession('immersive-vr', {
      requiredFeatures: ['local-floor'],
      optionalFeatures: ['hand-tracking', 'hit-test', 'dom-overlay']
    });
    
    this.referenceSpace = await this.session.requestReferenceSpace('local-floor');
    this.session.addEventListener('end', this.onSessionEnd.bind(this));
  }
  
  async initAR(): Promise<void> {
    const supported = await navigator.xr.isSessionSupported('immersive-ar');
    this.session = await navigator.xr.requestSession('immersive-ar', {
      requiredFeatures: ['hit-test', 'dom-overlay'],
      domOverlay: { root: document.getElementById('ar-overlay') }
    });
  }
  
  startRenderLoop(renderer: THREE.WebGLRenderer): void {
    const onFrame = (time: number, frame: XRFrame) => {
      const pose = frame.getViewerPose(this.referenceSpace);
      if (pose) {
        this.updateCameraFromPose(pose);
        this.updateControllers(frame);
      }
      renderer.render(this.scene, this.camera);
      this.session.requestAnimationFrame(onFrame);
    };
    this.session.requestAnimationFrame(onFrame);
  }
}
```

### 8.2 ThreeJS VR Scene Renderer

```typescript
class VRSceneRenderer {
  private scene: THREE.Scene;
  private camera: THREE.PerspectiveCamera;
  private renderer: THREE.WebGLRenderer;
  private controls: OrbitControls;
  private hotspotManager: HotspotManager;
  
  async loadScene(sceneData: SceneGraph): Promise<void> {
    // Load 360 background
    if (sceneData.background360) {
      const texture = await this.loadEquirectangularTexture(sceneData.background360);
      const geometry = new THREE.SphereGeometry(500, 60, 40);
      geometry.scale(-1, 1, 1); // Inside-out sphere
      const material = new THREE.MeshBasicMaterial({ map: texture });
      this.scene.add(new THREE.Mesh(geometry, material));
    }
    
    // Load 3D models
    for (const model of sceneData.models) {
      const gltf = await this.loadGLTF(model.url);
      gltf.scene.position.copy(model.position);
      gltf.scene.rotation.copy(model.rotation);
      gltf.scene.scale.copy(model.scale);
      this.scene.add(gltf.scene);
    }
    
    // Place hotspots
    for (const hotspot of sceneData.hotspots) {
      this.hotspotManager.createHotspot(hotspot);
    }
  }
  
  private async loadGLTF(url: string): Promise<GLTF> {
    const loader = new GLTFLoader();
    loader.setDRACOLoader(this.dracoLoader);
    loader.setKTX2Loader(this.ktx2Loader);
    return loader.loadAsync(url);
  }
}
```

### 8.3 AFrame Integration

```html
<!-- A-Frame VR Tour Scene -->
<a-scene embedded vr-mode-ui="enabled: true" 
         renderer="colorManagement: true; sortObjects: true">
  
  <!-- 360 Background -->
  <a-sky src="#panorama-texture" rotation="0 -90 0"></a-sky>
  
  <!-- Hotspots -->
  <a-entity id="hotspot-1" 
            class="hotspot"
            geometry="primitive: circle; radius: 0.3"
            material="color: #00ff00; shader: flat"
            position="2 -1 -3"
            animation="property: scale; to: 1.2 1.2 1.2; dur: 2000; loop: true"
            cursor="rayOrigin: mouse"
            click-handler>
  </a-entity>
  
  <!-- 3D Model -->
  <a-entity gltf-model="url(model.glb)" 
            position="0 0 -5"
            scale="1 1 1"
            animation-mixer>
  </a-entity>
  
  <!-- Spatial Audio -->
  <a-entity sound="src: url(audio.mp3); 
                   positional: true; 
                   refDistance: 10;
                   rolloffFactor: 2"
            position="3 2 -4">
  </a-entity>
  
  <!-- Info Panel -->
  <a-entity id="info-panel"
            position="0 1.6 -2"
            text="value: Welcome; color: white; align: center; width: 4"
            visible="false">
  </a-entity>
</a-scene>
```

---

## 9. MOBILE VR ARCHITECTURE

### 9.1 Android (Kotlin)

```kotlin
// Android VR Activity using SceneView (Filament-based)
class VRActivity : AppCompatActivity() {
    
    private lateinit var sceneView: SceneView
    private lateinit var xrSession: XrSession
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SceneView for 3D rendering
        sceneView = SceneView(this)
        setContentView(sceneView)
        
        // Load scene
        val sceneUrl = intent.getStringExtra("scene_url") ?: return
        lifecycleScope.launch {
            val scene = loadSceneFromApi(sceneUrl)
            renderScene(scene)
        }
        
        // Initialize gyroscope
        initGyroscope()
    }
    
    private fun initGyroscope() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0] // pitch
                val y = event.values[1] // yaw
                val z = event.values[2] // roll
                sceneView.camera.rotation = Quaternion.fromEulerAngles(x, y, z)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }, gyroscope, SensorManager.SENSOR_DELAY_GAME)
    }
    
    // Google Cardboard support
    private fun initCardboard() {
        val cardboardView = CardboardView(this)
        cardboardView.setRenderer(CardboardVRRenderer(sceneView))
        setContentView(cardboardView)
    }
}
```

### 9.2 iOS (Swift)

```swift
// iOS VR View using RealityKit + SceneKit
import RealityKit
import CoreMotion

class VRViewController: UIViewController {
    
    var arView: ARView!
    var motionManager: CMMotionManager!
    var sceneData: SceneData!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        arView = ARView(frame: view.bounds)
        view.addSubview(arView)
        
        motionManager = CMMotionManager()
        startGyroscope()
        
        loadScene()
    }
    
    func loadScene() {
        guard let url = URL(string: sceneData.modelUrl) else { return }
        
        ModelEntity.loadModel(contentsOf: url) { [weak self] result in
            switch result {
            case .success(let entity):
                let anchor = AnchorEntity(world: .zero)
                anchor.addChild(entity)
                self?.arView.scene.addAnchor(anchor)
            case .failure(let error):
                print("Failed to load model: \(error)")
            }
        }
    }
    
    func startGyroscope() {
        guard motionManager.isGyroAvailable else { return }
        motionManager.startDeviceMotionUpdates(to: .main) { [weak self] motion, error in
            guard let motion = motion else { return }
            let rotation = motion.attitude.rotationMatrix
            self?.updateCameraRotation(rotation)
        }
    }
}
```

---

## 10. DATABASE SCHEMA

### 10.1 PostgreSQL Tables

```sql
-- Tenant Management
CREATE TABLE xr_tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_scenes INT DEFAULT 10,
    max_storage_gb INT DEFAULT 5,
    max_bandwidth_gb INT DEFAULT 10,
    features JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Asset Management
CREATE TABLE xr_asset (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- VIDEO_360, IMAGE_360, MODEL_3D, AUDIO, PANORAMA
    original_url TEXT NOT NULL,
    processed_url TEXT,
    thumbnail_url TEXT,
    mime_type VARCHAR(100),
    file_size BIGINT,
    -- 360 Video specific
    video_type VARCHAR(50), -- MONOSCOPIC, STEREOSCOPIC
    resolution VARCHAR(20), -- 4K, 6K, 8K
    duration_seconds INT,
    -- 3D Model specific
    model_format VARCHAR(20), -- GLB, GLTF, FBX, OBJ
    has_draco_compression BOOLEAN DEFAULT FALSE,
    has_ktx2 TEXTures BOOLEAN DEFAULT FALSE,
    -- Processing
    transcode_status VARCHAR(50) DEFAULT 'PENDING',
    processing_progress INT DEFAULT 0,
    hls_url TEXT,
    dash_url TEXT,
    variants JSONB DEFAULT '[]',
    -- Metadata
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Scene Management
CREATE TABLE xr_scene (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL, -- VR_TOUR, VR_SHOWROOM, VR_MUSEUM, VR_TRAINING, AR_EXPERIENCE, XR_SPACE
    thumbnail_url TEXT,
    -- Scene Configuration (stored as JSONB)
    scene_config JSONB DEFAULT '{}',
    -- Background
    background_type VARCHAR(50), -- COLOR, IMAGE_360, VIDEO_360, GRADIENT
    background_asset_id UUID REFERENCES xr_asset(id),
    -- Publishing
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, REVIEW, PUBLISHED, ARCHIVED
    published_url TEXT,
    published_at TIMESTAMP,
    version INT DEFAULT 1,
    -- Settings
    settings JSONB DEFAULT '{}',
    -- Stats
    view_count INT DEFAULT 0,
    avg_view_time_seconds FLOAT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Scene Nodes (Scene Graph)
CREATE TABLE xr_scene_node (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scene_id UUID NOT NULL REFERENCES xr_scene(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES xr_scene_node(id),
    node_type VARCHAR(50) NOT NULL, -- HOTSPOT, TEXT, IMAGE, VIDEO, AUDIO, MODEL, UI_PANEL
    name VARCHAR(255),
    -- Position in 3D space
    position_x FLOAT DEFAULT 0,
    position_y FLOAT DEFAULT 0,
    position_z FLOAT DEFAULT 0,
    rotation_x FLOAT DEFAULT 0,
    rotation_y FLOAT DEFAULT 0,
    rotation_z FLOAT DEFAULT 0,
    scale_x FLOAT DEFAULT 1,
    scale_y FLOAT DEFAULT 1,
    scale_z FLOAT DEFAULT 1,
    -- Node content
    content JSONB DEFAULT '{}',
    -- Visibility
    visible BOOLEAN DEFAULT TRUE,
    interactive BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Hotspot Configuration
CREATE TABLE xr_hotspot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scene_id UUID NOT NULL REFERENCES xr_scene(id) ON DELETE CASCADE,
    node_id UUID REFERENCES xr_scene_node(id),
    hotspot_type VARCHAR(50) NOT NULL, -- INFO, NAVIGATION, MEDIA, LINK, ACTION
    -- Position (spherical coordinates for 360)
    latitude FLOAT,
    longitude FLOAT,
    -- Content
    title VARCHAR(255),
    description TEXT,
    icon_url TEXT,
    -- Action
    action_type VARCHAR(50), -- NAVIGATE, PLAY_VIDEO, SHOW_INFO, OPEN_URL, TELEPORT
    action_payload JSONB DEFAULT '{}',
    -- Style
    style JSONB DEFAULT '{}',
    -- Animation
    animation VARCHAR(50), -- NONE, PULSE, FLOAT, ROTATE
    created_at TIMESTAMP DEFAULT NOW()
);

-- VR/AR/XR Sessions
CREATE TABLE xr_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    user_id UUID NOT NULL,
    scene_id UUID NOT NULL REFERENCES xr_scene(id),
    device_type VARCHAR(50) NOT NULL, -- WEB, IOS, ANDROID, QUEST, VISION_PRO, CARDBOARD
    device_info JSONB DEFAULT '{}',
    -- Session metrics
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP,
    duration_seconds INT,
    -- Gaze tracking
    gaze_data JSONB DEFAULT '[]',
    -- Interactions
    interactions JSONB DEFAULT '[]',
    -- Performance
    fps_avg FLOAT,
    load_time_ms INT,
    -- Location (for indoor navigation)
    floor_id UUID,
    room_id UUID,
    position JSONB
);

-- Multi-user Collaboration
CREATE TABLE xr_collaboration_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    scene_id UUID NOT NULL REFERENCES xr_scene(id),
    name VARCHAR(255),
    max_participants INT DEFAULT 10,
    status VARCHAR(50) DEFAULT 'WAITING', -- WAITING, ACTIVE, ENDED
    host_user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE xr_collaboration_participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES xr_collaboration_room(id),
    user_id UUID NOT NULL,
    avatar_config JSONB DEFAULT '{}',
    joined_at TIMESTAMP DEFAULT NOW(),
    left_at TIMESTAMP,
    is_muted BOOLEAN DEFAULT FALSE,
    is_screen_sharing BOOLEAN DEFAULT FALSE
);

-- Subscription & Billing
CREATE TABLE xr_subscription (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    plan VARCHAR(50) NOT NULL, -- FREE, STARTER, PRO, ENTERPRISE
    status VARCHAR(50) NOT NULL, -- ACTIVE, PAST_DUE, CANCELED, TRIAL
    billing_cycle VARCHAR(20) NOT NULL, -- MONTHLY, YEARLY
    price_cents INT NOT NULL,
    -- Usage this period
    storage_used_bytes BIGINT DEFAULT 0,
    bandwidth_used_bytes BIGINT DEFAULT 0,
    scenes_count INT DEFAULT 0,
    users_count INT DEFAULT 0,
    -- Period
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Digital Twin
CREATE TABLE xr_digital_twin (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- BUILDING, FACTORY, WAREHOUSE, HOSPITAL
    bim_asset_id UUID REFERENCES xr_asset(id),
    scene_id UUID REFERENCES xr_scene(id),
    -- IoT Integration
    iot_endpoints JSONB DEFAULT '[]',
    sync_interval_seconds INT DEFAULT 30,
    -- Spatial data
    floors JSONB DEFAULT '[]',
    rooms JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 10.2 MongoDB Collections (Scene Graph JSON)

```javascript
// Collection: xr_scene_graphs
{
  _id: UUID,
  tenantId: UUID,
  sceneId: UUID,
  version: 3,
  graph: {
    nodes: [
      {
        id: "node-1",
        type: "root",
        children: ["node-2", "node-3"],
        transform: { position: [0,0,0], rotation: [0,0,0], scale: [1,1,1] }
      },
      {
        id: "node-2",
        type: "model",
        assetId: "asset-uuid",
        url: "https://cdn.xrvista.com/tenant1/models/building.glb",
        transform: { position: [0,0,-5], rotation: [0,0,0], scale: [1,1,1] },
        animations: ["idle", "walk"],
        interactions: { onClick: "show-info", onHover: "glow" }
      },
      {
        id: "node-3",
        type: "hotspot",
        hotspotType: "navigation",
        targetSceneId: "scene-uuid",
        position: { lat: 45.5, lng: -30.2 },
        icon: "arrow-right",
        label: "Go to Room 2"
      }
    ],
    spatialAudio: [
      {
        id: "audio-1",
        assetId: "audio-uuid",
        position: [3, 2, -4],
        refDistance: 10,
        rolloffFactor: 2,
        maxDistance: 100,
        loop: true
      }
    ],
    lighting: {
      ambient: { color: "#ffffff", intensity: 0.5 },
      directional: [
        { color: "#ffeedd", intensity: 1.0, position: [5, 10, 5] }
      ],
      environmentMap: "hdr-url"
    }
  },
  createdAt: ISODate,
  updatedAt: ISODate
}
```

### 10.3 ClickHouse (Analytics)

```sql
-- Analytics events (high-volume writes)
CREATE TABLE xr_analytics_events (
    event_id UUID,
    tenant_id UUID,
    user_id UUID,
    scene_id UUID,
    session_id UUID,
    event_type String,      -- VIEW, GAZE, CLICK, HOTSPOT_CLICK, NAVIGATE, EXIT
    event_data String,      -- JSON payload
    device_type String,
    device_info String,
    timestamp DateTime64(3),
    date Date DEFAULT toDate(timestamp)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (tenant_id, scene_id, timestamp);

-- Aggregated session stats (materialized view)
CREATE TABLE xr_session_stats (
    tenant_id UUID,
    scene_id Date,
    total_sessions UInt64,
    avg_duration Float64,
    avg_fps Float64,
    bounce_rate Float64,
    device_distribution Map(String, UInt64)
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (tenant_id, scene_id, date);
```

---

## 11. AI MODULES

### 11.1 AI Scene Generator

```python
# AI Service - Scene Generation from Text/Image
class AISceneGenerator:
    """
    Input: Text description or reference image
    Output: Complete VR scene configuration
    """
    
    async def generate_scene_from_text(self, prompt: str, style: str) -> SceneGraph:
        # 1. Parse intent
        intent = await self.llm.parse_scene_intent(prompt)
        # e.g., { type: "museum", rooms: 3, style: "modern", exhibits: ["painting", "sculpture"] }
        
        # 2. Generate layout
        layout = await self.generate_layout(intent)
        
        # 3. Select/generate assets
        assets = await self.asset_selector.select_assets(intent)
        
        # 4. Generate hotspot placements
        hotspots = await self.hotspot_generator.generate(intent, layout)
        
        # 5. Generate narration script
        narration = await self.narration_generator.generate(intent)
        
        # 6. Assemble scene graph
        scene_graph = self.assembler.assemble(layout, assets, hotspots, narration)
        
        return scene_graph
    
    async def generate_tour_from_floorplan(self, floorplan_image: str) -> TourConfig:
        # Computer vision to detect rooms, doors, corridors
        rooms = await self.cv.detect_rooms(floorplan_image)
        connections = await self.cv.detect_doors(floorplan_image)
        
        # Generate tour path
        tour_path = await self.path_generator.optimize(rooms, connections)
        
        return TourConfig(rooms=rooms, connections=connections, path=tour_path)
```

### 11.2 AI NPC/Tour Guide

```python
class AITourGuide:
    """
    AI-powered NPC guide for virtual tours
    Supports: Text-to-Speech, Gesture Animation, Context-aware responses
    """
    
    def __init__(self):
        self.llm = ChatGPT4()
        self.tts = ElevenLabsTTS()
        self.animation = FacialAnimationEngine()
    
    async def respond_to_visitor(self, context: VisitorContext) -> GuideResponse:
        # Context includes: current room, gaze target, previous interactions, visitor profile
        prompt = f"""
        You are a tour guide at {context.venue_name}.
        The visitor is looking at: {context.gaze_target}
        Previous conversation: {context.history}
        
        Respond in a friendly, informative way. Keep it under 2 sentences.
        """
        
        response_text = await self.llm.generate(prompt)
        audio_url = await self.tts.synthesize(response_text, voice="guide_voice")
        animation = await self.animation.generate_gesture(response_text)
        
        return GuideResponse(
            text=response_text,
            audio_url=audio_url,
            animation=animation,
            emotion=self.detect_emotion(response_text)
        )
```

---

## 12. COLLABORATION / MULTI-USER VR

### 12.1 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  COLLABORATION SERVER                         │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐ │
│  │ WebSocket   │  │ WebRTC       │  │ State              │ │
│  │ Server      │  │ SFU          │  │ Synchronization    │ │
│  │ (Signals)   │  │ (Voice/Video)│  │ (CRDT)             │ │
│  └──────┬──────┘  └──────┬───────┘  └────────┬───────────┘ │
│         │                │                    │              │
│         └────────────────┼────────────────────┘              │
│                          │                                   │
│                    ┌─────▼─────┐                             │
│                    │  Redis    │                             │
│                    │  Pub/Sub  │                             │
│                    │  + State  │                             │
│                    └───────────┘                             │
└─────────────────────────────────────────────────────────────┘
```

### 12.2 Avatar System

```typescript
interface AvatarConfig {
  id: string;
  model3D: string;         // URL to GLB avatar model
  animations: {
    idle: string;
    walk: string;
    talk: string;
    gesture: string;
  };
  spatialAudio: {
    position: [number, number, number];
    rotation: [number, number, number];
    voiceEnabled: boolean;
  };
  customization: {
    bodyType: 'male' | 'female' | 'neutral';
    skinTone: string;
    clothing: string;
    accessories: string[];
  };
}
```

---

## 13. API DESIGN

### 13.1 REST API Endpoints

```
/api/v1/tenants
  POST   /api/v1/tenants                    - Create tenant
  GET    /api/v1/tenants/{id}                - Get tenant
  PUT    /api/v1/tenants/{id}                - Update tenant
  GET    /api/v1/tenants/{id}/subscription   - Get subscription
  
/api/v1/assets
  POST   /api/v1/assets/upload               - Upload asset (multipart)
  POST   /api/v1/assets/upload/url           - Upload from URL
  GET    /api/v1/assets/{id}                 - Get asset
  GET    /api/v1/assets/{id}/streaming-url   - Get HLS/DASH streaming URL
  DELETE /api/v1/assets/{id}                 - Delete asset
  GET    /api/v1/assets?type=VIDEO_360       - List assets
  
/api/v1/scenes
  POST   /api/v1/scenes                      - Create scene
  GET    /api/v1/scenes/{id}                 - Get scene
  PUT    /api/v1/scenes/{id}                 - Update scene
  PUT    /api/v1/scenes/{id}/graph           - Update scene graph
  POST   /api/v1/scenes/{id}/publish         - Publish scene
  POST   /api/v1/scenes/{id}/duplicate       - Duplicate scene
  GET    /api/v1/scenes/{id}/embed           - Get embed code
  DELETE /api/v1/scenes/{id}                 - Delete scene
  
/api/v1/scenes/{sceneId}/hotspots
  POST   /api/v1/scenes/{sceneId}/hotspots   - Add hotspot
  PUT    /api/v1/scenes/{sceneId}/hotspots/{id} - Update hotspot
  DELETE /api/v1/scenes/{sceneId}/hotspots/{id} - Delete hotspot
  
/api/v1/scenes/{sceneId}/nodes
  POST   /api/v1/scenes/{sceneId}/nodes      - Add node
  PUT    /api/v1/scenes/{sceneId}/nodes/{id} - Update node
  DELETE /api/v1/scenes/{sceneId}/nodes/{id} - Delete node
  
/api/v1/video
  POST   /api/v1/video/transcode             - Start transcoding
  GET    /api/v1/video/{id}/status           - Get transcode status
  GET    /api/v1/video/{id}/manifest         - Get HLS manifest
  POST   /api/v1/video/merge-stereo          - Merge stereoscopic
  
/api/v1/tours
  POST   /api/v1/tours                       - Create tour
  GET    /api/v1/tours/{id}                  - Get tour
  PUT    /api/v1/tours/{id}/rooms            - Update rooms
  PUT    /api/v1/tours/{id}/navigation       - Update navigation
  
/api/v1/xr
  POST   /api/v1/xr/sessions                 - Start XR session
  PUT    /api/v1/xr/sessions/{id}/metrics    - Update metrics
  POST   /api/v1/xr/sessions/{id}/end        - End session
  
/api/v1/collaboration
  POST   /api/v1/collab/rooms                - Create room
  POST   /api/v1/collab/rooms/{id}/join      - Join room
  POST   /api/v1/collab/rooms/{id}/leave     - Leave room
  PUT    /api/v1/collab/rooms/{id}/avatar    - Update avatar
  
/api/v1/ai
  POST   /api/v1/ai/generate-scene           - AI scene generation
  POST   /api/v1/ai/generate-tour            - AI tour from floorplan
  POST   /api/v1/ai/generate-narration       - AI narration
  POST   /api/v1/ai/translate                - AI translation
  POST   /api/v1/ai/voice-guide              - AI voice guide
  
/api/v1/analytics
  GET    /api/v1/analytics/scenes/{id}/views     - View stats
  GET    /api/v1/analytics/scenes/{id}/heatmap   - Gaze heatmap
  GET    /api/v1/analytics/scenes/{id}/funnel    - User funnel
  GET    /api/v1/analytics/tenant/dashboard      - Tenant dashboard
  
/api/v1/digital-twin
  POST   /api/v1/digital-twins              - Create twin
  PUT    /api/v1/digital-twins/{id}/sync    - Sync IoT data
  GET    /api/v1/digital-twins/{id}/status  - Twin status
```

### 13.2 WebSocket Events

```typescript
// Real-time collaboration events
interface CollaborationEvents {
  // Client → Server
  'user:join': { roomId: string; userId: string; avatar: AvatarConfig };
  'user:leave': { roomId: string; userId: string };
  'user:move': { roomId: string; position: Vector3; rotation: Quaternion };
  'user:voice': { roomId: string; audioChunk: ArrayBuffer };
  'user:gaze': { roomId: string; target: string; position: Vector3 };
  'user:gesture': { roomId: string; gesture: string };
  'screen:share:start': { roomId: string };
  'screen:share:stop': { roomId: string };
  'object:manipulate': { roomId: string; objectId: string; transform: Transform };
  
  // Server → Client
  'user:joined': { userId: string; avatar: AvatarConfig };
  'user:left': { userId: string };
  'user:moved': { userId: string; position: Vector3; rotation: Quaternion };
  'user:spoke': { userId: string; audioChunk: ArrayBuffer };
  'user:looked': { userId: string; target: string };
  'screen:shared': { userId: string; streamUrl: string };
  'object:moved': { objectId: string; transform: Transform };
  'state:sync': { fullState: CollaborationState };
}
```

---

## 14. SECURITY DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│                        SECURITY LAYERS                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Layer 1: Edge Security                                         │
│  ├── WAF (Web Application Firewall)                             │
│  ├── DDoS Protection (CloudFlare/AWS Shield)                    │
│  ├── TLS 1.3 (HTTPS everywhere)                                 │
│  └── CORS Policy                                                │
│                                                                  │
│  Layer 2: Authentication                                        │
│  ├── Keycloak (OIDC/OAuth2)                                    │
│  ├── JWT Access Token + Refresh Token                          │
│  ├── MFA (TOTP, WebAuthn)                                      │
│  ├── Social Login (Google, Microsoft, Apple)                    │
│  └── API Key for programmatic access                           │
│                                                                  │
│  Layer 3: Authorization                                         │
│  ├── RBAC: SUPER_ADMIN, TENANT_ADMIN, CONTENT_CREATOR,         │
│  │         VR_DESIGNER, AR_DESIGNER, VIEWER                    │
│  ├── ABAC: Per-tenant feature gates                            │
│  └── Resource-level: Scene ownership, asset access             │
│                                                                  │
│  Layer 4: Data Security                                         │
│  ├── Encryption at rest (AES-256)                              │
│  ├── Encryption in transit (TLS 1.3)                           │
│  ├── Signed URLs for asset access (MinIO/S3)                   │
│  ├── DRM for premium 360 video content                         │
│  └── PII encryption (user data)                                │
│                                                                  │
│  Layer 5: Content Protection                                    │
│  ├── Signed URLs with expiry                                    │
│  ├── Token-based video streaming auth                          │
│  ├── DRM integration (Widevine, FairPlay)                      │
│  ├── Watermarking for premium content                          │
│  └── Anti-scraping measures                                    │
│                                                                  │
│  Layer 6: VR-Specific Security                                  │
│  ├── Session token validation per VR device                    │
│  ├── Device fingerprinting                                     │
│  ├── Geo-restriction for content                               │
│  └── Age verification for VR content                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 15. DEPLOYMENT ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    KUBERNETES CLUSTER                                 │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  Ingress Controller (NGINX)                                 │    │
│  │  - TLS Termination                                           │    │
│  │  - Path-based routing                                        │    │
│  │  - Rate limiting                                             │    │
│  └─────────────────────────┬───────────────────────────────────┘    │
│                             │                                        │
│  ┌──────────────────────────▼──────────────────────────────────┐    │
│  │  Namespace: xr-platform                                     │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │    │
│  │  │ Gateway │ │  IAM    │ │ Tenant  │ │  Asset  │          │    │
│  │  │ (3 pod) │ │ (3 pod) │ │ (2 pod) │ │ (3 pod) │          │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │    │
│  │  │ Scene   │ │  Video  │ │Streaming│ │   XR    │          │    │
│  │  │ (3 pod) │ │ (5 pod) │ │ (3 pod) │ │ (3 pod) │          │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │    │
│  │  │   AI    │ │Analytics│ │  BIM    │ │Digital  │          │    │
│  │  │ (3 pod) │ │ (3 pod) │ │ (2 pod) │ │  Twin   │          │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐                                   │    │
│  │  │Collab   │ │   GIS   │                                   │    │
│  │  │(3 pod)  │ │ (2 pod) │                                   │    │
│  │  └─────────┘ └─────────┘                                   │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │  Namespace: xr-infra                                         │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │    │
│  │  │PostgreSQL│ │  Redis  │ │ Kafka   │ │  MinIO  │          │    │
│  │  │ HA (3)   │ │Sentinel │ │ (3 broker)│ │ (4 node)│         │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐                       │    │
│  │  │Elastic- │ │ClickHouse│ │MongoDB  │                       │    │
│  │  │search(3)│ │ (3 node) │ │ (3 node)│                       │    │
│  │  └─────────┘ └─────────┘ └─────────┘                       │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │  Namespace: xr-monitoring                                   │    │
│  │                                                              │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │    │
│  │  │Prometheus│ │ Grafana │ │  Loki   │ │OTel     │          │    │
│  │  │         │ │         │ │         │ │Collector│          │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │    │
│  └──────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### HPA (Horizontal Pod Autoscaler)

```yaml
# Video processing pods - scale on CPU + queue depth
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: video-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: video-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Pods
    pods:
      metric:
        name: kafka_consumer_lag
      target:
        type: AverageValue
        averageValue: "100"
```

---

## 16. PERFORMANCE REQUIREMENTS

### 16.1 1 Million User Scale

| Metric | Target | Strategy |
|--------|--------|----------|
| Concurrent Users | 100K | Horizontal scaling, CDN |
| Daily Active Users | 1M | Multi-region deployment |
| Scene Load Time | < 2s | Asset CDN, preload, compression |
| Video Start Time | < 1s | HLS low-latency, CDN edge |
| API Response Time | < 200ms | Redis cache, read replicas |
| 8K Streaming | 60fps | CDN edge, adaptive bitrate |
| VR Frame Rate | 90fps | GPU rendering, level-of-detail |
| Uptime | 99.99% | Multi-AZ, failover |

### 16.2 CDN Strategy for 8K 360 Video

```
┌───────────────────────────────────────────────────────────────┐
│                    CDN TOPOLOGY                                 │
│                                                                │
│  Origin (MinIO/S3) ──── Region: us-east-1                     │
│       │                                                        │
│       ├── Origin Shield: us-east-1 (cache hot content)        │
│       │                                                        │
│       ├── Edge: us-east-1, us-west-2, eu-west-1              │
│       │   ap-southeast-1, ap-northeast-1                      │
│       │                                                        │
│       └── Total 200+ edge locations worldwide                 │
│                                                                │
│  Bandwidth per 8K 360 stream: ~40 Mbps                        │
│  100K concurrent × 40 Mbps = 4 Tbps peak CDN bandwidth       │
│  Cost optimization: Transcode to 3 tiers (4K/6K/8K)          │
│  + Client-side adaptive quality selection                      │
└───────────────────────────────────────────────────────────────┘
```

---

## 17. SPRINT BREAKDOWN

### Phase 1: Foundation (Sprint 1-4)
- [ ] tenant-service microservice
- [ ] asset-service with MinIO integration
- [ ] video-service with FFmpeg transcoding pipeline
- [ ] Scene builder API (CRUD)
- [ ] PostgreSQL schema for all new tables
- [ ] Docker Compose updates
- [ ] K8s manifests for new services

### Phase 2: Core XR (Sprint 5-8)
- [ ] WebXR integration (ThreeJS + AFrame)
- [ ] 360 Video Player (mono/stereo)
- [ ] VR Scene Builder (drag & drop API)
- [ ] Hotspot system
- [ ] Spatial audio
- [ ] Cardboard SDK integration

### Phase 3: Mobile VR (Sprint 9-12)
- [ ] Android native VR app (Kotlin)
- [ ] iOS native VR app (Swift)
- [ ] Meta Quest app
- [ ] Apple Vision Pro app
- [ ] Gyroscope/motion sensor integration

### Phase 4: Advanced Features (Sprint 13-16)
- [ ] AI scene generation
- [ ] AI tour guide/NPC
- [ ] Multi-user collaboration
- [ ] Avatar system
- [ ] Voice chat (WebRTC)
- [ ] Screen sharing in VR

### Phase 5: Enterprise (Sprint 17-20)
- [ ] Digital Twin platform
- [ ] BIM/CAD viewer
- [ ] GIS/Indoor navigation
- [ ] LMS VR Training integration
- [ ] VR Commerce
- [ ] Virtual Event platform

### Phase 6: Scale & Polish (Sprint 21-24)
- [ ] Performance optimization
- [ ] CDN edge deployment
- [ ] Analytics dashboard
- [ ] WebGPU renderer
- [ ] Pixel Streaming for heavy scenes
- [ ] Multi-region deployment

---

## 18. TECHNOLOGY DECISIONS

| Decision | Choice | Rationale |
|----------|--------|-----------|
| 3D Engine | ThreeJS + AFrame | Web-native, large ecosystem, WebXR support |
| Mobile VR | SceneView (Android) / RealityKit (iOS) | Native performance, device sensor access |
| Video Processing | FFmpeg | Industry standard, supports equirectangular |
| Streaming | HLS + DASH | Universal browser/device support |
| Scene Storage | MongoDB (JSON) | Flexible scene graph, nested structures |
| Analytics | ClickHouse | High-volume event ingestion, fast aggregation |
| Collaboration | WebSocket + CRDT | Real-time sync, conflict resolution |
| Voice Chat | WebRTC SFU | Low-latency, scalable |
| AI | OpenAI API + Custom models | Scene generation, narration, translation |
| DRM | Widevine (Android/Web) + FairPlay (iOS) | Content protection |

---

## 19. COMPETITIVE ENTERPRISE FEATURES

### Must-Have (MVP)
1. No-code VR Scene Builder
2. 360 Video Player (mono + stereo)
3. Virtual Tour Builder
4. VR Website publish
5. Mobile VR apps (Android + iOS)
6. Asset management (upload + transcode)
7. Multi-tenant with subscription
8. Basic analytics

### Should-Have (Phase 2)
9. VR Showroom Builder
10. VR Training with LMS
11. AI Scene Generation
12. Multi-user VR collaboration
13. Avatar system
14. Voice chat in VR
15. Heatmap analytics
16. Gaze tracking

### Nice-to-Have (Phase 3)
17. Digital Twin platform
18. BIM/CAD Viewer
19. Indoor Navigation (GIS)
20. AI NPC Tour Guide
21. VR Commerce
22. Virtual Events
23. WebGPU renderer
24. Pixel Streaming

### Differentiators
25. Apple Vision Pro native support
26. Meta Quest optimization
27. 8K stereoscopic streaming
28. AI-powered scene generation from text
29. Real-time多人VR with spatial audio
30. Cross-platform publish (one scene → all devices)
