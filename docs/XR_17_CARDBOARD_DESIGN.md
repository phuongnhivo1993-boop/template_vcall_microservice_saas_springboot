# XR_17 - Google Cardboard VR Integration Design

## Document Metadata

| Field | Value |
|-------|-------|
| Document ID | XR_17 |
| Title | Google Cardboard VR Integration Design |
| Version | 1.0 |
| Status | Draft |
| Platform | XRVista - Enterprise XR SaaS Platform |
| Target | Android (8+), iOS (14+) |
| Estimated Effort | 3 sprints |

---

## 1. Overview

### 1.1 What is Google Cardboard

Google Cardboard is a low-cost VR headset platform that transforms a smartphone into a stereoscopic VR viewer. It uses the phone's display, sensors (gyroscope, accelerometer), and optics (biconvex lenses) to deliver an immersive VR experience without requiring dedicated VR hardware.

**Key Characteristics:**
- **Hardware-agnostic VR**: Any smartphone becomes a VR device
- **Low barrier to entry**: $5-25 headset vs $300-1000+ dedicated VR
- **Wide device reach**: 4+ billion smartphone users globally
- **WebXR compatible**: Works with AFrame, ThreeJS, WebXR API
- **No battery/power**: Passive device leveraging phone hardware

### 1.2 Target Devices

| Platform | Minimum Version | Recommended | Sensor Requirements |
|----------|----------------|-------------|---------------------|
| Android | 8.0 (API 26) | Android 12+ | Gyroscope mandatory |
| iOS | 14.0 | iOS 16+ | Gyroscope mandatory |

**Mandatory Hardware Sensors:**
- Gyroscope (angular velocity)
- Accelerometer (linear acceleration)
- Magnetometer (optional, for Cardboard V1 trigger)

### 1.3 Use Cases for the Platform

| Use Case | Description | Priority |
|----------|-------------|----------|
| Real Estate Virtual Tours | 360 property walkthroughs with hotspot navigation | P0 |
| Training & Education | Immersive training scenarios with interactive elements | P0 |
| Product Visualization | 3D product views with Cardboard immersion | P1 |
| Cultural Heritage | Museum/artifact virtual visits | P1 |
| Healthcare | Patient education, phobia therapy | P2 |
| Tourism | Destination previews, virtual travel | P2 |

---

## 2. Architecture

### 2.1 System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    MOBILE APPLICATION                        │
│                    (Kotlin / Swift)                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                WebView Layer                           │  │
│  │  ┌──────────────────────┐  ┌───────────────────────┐  │  │
│  │  │  AFrame / ThreeJS    │  │  WebXR Polyfill       │  │  │
│  │  │  Scene Renderer      │  │  (Cardboard Fallback) │  │  │
│  │  │                      │  │                       │  │  │
│  │  │  - Equirectangular   │  │  - Session Manager    │  │  │
│  │  │    Texture Mapping   │  │  - Reference Space    │  │  │
│  │  │  - Hotspot Rendering │  │  - Input Source       │  │  │
│  │  │  - 3D Model Loader   │  │  - Frame Scheduler    │  │  │
│  │  │  - Audio Spatializer │  │                       │  │  │
│  │  └──────────┬───────────┘  └───────────┬───────────┘  │  │
│  │             │                          │               │  │
│  │  ┌──────────▼──────────────────────────▼───────────┐  │  │
│  │  │           JavaScript Bridge (JSBridge)          │  │  │
│  │  │  - Native ↔ WebView communication               │  │  │
│  │  │  - Sensor data injection                        │  │  │
│  │  │  - Touch/trigger event forwarding               │  │  │
│  │  └──────────────────────┬──────────────────────────┘  │  │
│  └─────────────────────────┼──────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────▼──────────────────────────────┐  │
│  │            Native VR Rendering Layer                     │  │
│  │  ┌─────────────────────────────────────────────────┐   │  │
│  │  │         Cardboard SDK Integration               │   │  │
│  │  │                                                  │   │  │
│  │  │  ┌──────────────┐    ┌──────────────────────┐  │   │  │
│  │  │  │ Head Tracker │    │ Lens Distortion      │  │   │  │
│  │  │  │              │    │ Correction           │  │   │  │
│  │  │  │ - Gyroscope  │    │ - Barrel Distortion  │  │   │  │
│  │  │  │ - Accelerom. │    │ - Chromatic Aberr.   │  │   │  │
│  │  │  │ - Magnetom.  │    │ - IPD Config         │  │   │  │
│  │  │  │ - Sensor     │    │ - FOV Settings       │  │   │  │
│  │  │  │   Fusion     │    │                      │  │   │  │
│  │  │  │ - Drift      │    │ - Viewer Profiles    │  │   │  │
│  │  │  │   Correction │    │ - QR Code Detection  │  │   │  │
│  │  │  └──────────────┘    └──────────────────────┘  │   │  │
│  │  │                                                  │   │  │
│  │  │  ┌──────────────────────────────────────────┐   │   │  │
│  │  │  │     Stereoscopic Render Pipeline         │   │   │  │
│  │  │  │                                          │   │   │  │
│  │  │  │  ┌──────────┐         ┌──────────┐      │   │   │  │
│  │  │  │  │Left Eye  │         │Right Eye │      │   │   │  │
│  │  │  │  │Camera    │         │Camera    │      │   │   │  │
│  │  │  │  │          │         │          │      │   │   │  │
│  │  │  │  │ FOV: 80° │         │ FOV: 80° │      │   │   │  │
│  │  │  │  │ Offset:  │         │ Offset:  │      │   │   │  │
│  │  │  │  │ -31.5mm  │         │ +31.5mm  │      │   │   │  │
│  │  │  │  └────┬─────┘         └────┬─────┘      │   │   │  │
│  │  │  │       │                    │             │   │   │  │
│  │  │  │  ┌────▼────────────────────▼─────┐      │   │   │  │
│  │  │  │  │  Render-to-Texture (FBO)      │      │   │   │  │
│  │  │  │  │  - Left texture               │      │   │   │  │
│  │  │  │  │  - Right texture              │      │   │   │  │
│  │  │  │  └──────────────┬────────────────┘      │   │   │  │
│  │  │  │                 │                        │   │   │  │
│  │  │  │  ┌──────────────▼────────────────┐      │   │   │  │
│  │  │  │  │  Distortion Post-Processing   │      │   │   │  │
│  │  │  │  │  - Barrel distortion shader   │      │   │   │  │
│  │  │  │  │  - Chromatic aberration fix   │      │   │   │  │
│  │  │  │  │  - Side-by-side composition   │      │   │   │  │
│  │  │  │  └──────────────┬────────────────┘      │   │   │  │
│  │  │  │                 │                        │   │   │  │
│  │  │  │  ┌──────────────▼────────────────┐      │   │   │  │
│  │  │  │  │      Final Display Output     │      │   │   │  │
│  │  │  │  │  ┌─────────────┬────────────┐ │      │   │   │  │
│  │  │  │  │  │  LEFT HALF  │ RIGHT HALF │ │      │   │   │  │
│  │  │  │  │  │  (Left Eye) │ (Right Eye)│ │      │   │   │  │
│  │  │  │  │  └─────────────┴────────────┘ │      │   │   │  │
│  │  │  │  └───────────────────────────────┘      │   │   │  │
│  │  │  └──────────────────────────────────────────┘   │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  │                                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────▼──────────────────────────────┐  │
│  │            Input Handling Layer                          │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │  │
│  │  │ Cardboard    │  │ Screen Tap   │  │ Gaze         │  │  │
│  │  │ Button/      │  │ Detection    │  │ Selection    │  │  │
│  │  │ Magnetic     │  │ (Fallback)   │  │ (Dwell Time) │  │  │
│  │  │ Trigger      │  │              │  │              │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS / WebSocket
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND SERVICES                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Scene Service│  │ Asset Service│  │ Stream Svc   │      │
│  │ (Scene Graph)│  │ (3D/360)     │  │ (HLS/DASH)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Analytics    │  │ CDN          │  │ Auth Service │      │
│  │ (Gaze Heatmap│  │ (CloudFront) │  │ (JWT/OAuth)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Component Interaction Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  App     │────▶│  WebView │────▶│  AFrame  │────▶│  Scene   │
│  Launch  │     │  Init    │     │  Load    │     │  Render  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                      │                  │
                                      ▼                  ▼
                               ┌──────────┐     ┌──────────┐
                               │  Scene   │     │  Sensor  │
                               │  Graph   │     │  Polling │
                               │  Fetch   │     │  Start   │
                               └──────────┘     └──────────┘
                                                       │
                                                       ▼
                                                ┌──────────┐
                                                │  Cardboard│
                                                │  SDK     │
                                                │  Loop    │
                                                └──────────┘
                                                       │
                                                       ▼
                                                ┌──────────┐
                                                │  Stereo  │
                                                │  Render  │
                                                │  (60fps) │
                                                └──────────┘
```

---

## 3. Cardboard SDK Integration

### 3.1 Android Integration

#### 3.1.1 Dependency Configuration

```groovy
// build.gradle (app module)
dependencies {
    // Google Cardboard SDK
    implementation 'com.google.vr:sdk-base:1.190.0'
    implementation 'com.google.vr:sdk-audio:1.190.0'
    
    // WebXR Polyfill for WebView
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

#### 3.1.2 Session Management

```kotlin
// CardboardSessionManager.kt
package com.xrvista.cardboard

import com.google.vr.ndk.base.DaydreamApi
import com.google.vr.ndk.base.GvrLayout
import com.google.vr.ndk.base.GvrView

class CardboardSessionManager(
    private val activity: Activity,
    private val sceneCallback: SceneCallback
) {
    private var gvrLayout: GvrLayout? = null
    private var gvrView: GvrView? = null
    private var isSessionActive = false
    private val sensorManager = SensorFusionManager(activity)
    
    fun initSession(viewerParams: ViewerParams): Result<Unit> {
        return try {
            gvrLayout = GvrLayout(activity).apply {
                setSettingsButtonEnabled(false)
                setTransitionViewEnabled(false)
                setDistortionCorrectionEnabled(true)
                setFullscreenEnabled(true)
            }
            
            gvrView = GvrView(activity).apply {
                setRenderer(CardboardVRRenderer(sceneCallback))
                setOnCardboardBackButtonListener {
                    exitVR()
                }
            }
            
            gvrLayout?.addChildView(gvrView)
            
            // Configure viewer profile
            configureViewerProfile(viewerParams)
            
            // Start sensor fusion
            sensorManager.startListening()
            
            isSessionActive = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun enterVRMode() {
        gvrView?.requestPresent()
        sceneCallback.onVREntered()
    }
    
    fun exitVR() {
        gvrView?.exitPresentation()
        sensorManager.stopListening()
        isSessionActive = false
        sceneCallback.onVRExited()
    }
    
    fun getHeadOrientation(): Quaternion {
        return sensorManager.fusedOrientation
    }
    
    fun getHeadPosition(): Vector3 {
        return sensorManager.fusedPosition
    }
    
    private fun configureViewerProfile(params: ViewerParams) {
        val cardboardParams = com.google.vr.vrcore.common.Params().apply {
            setInterpupillaryDistance(params.ipdMeters)
            setFieldOfViewAngles(
                params.fovTop,
                params.fovBottom,
                params.fovLeft,
                params.fovRight
            )
        }
        gvrView?.setCardboardParams(cardboardParams)
    }
    
    fun onPause() {
        sensorManager.stopListening()
        gvrView?.onPause()
    }
    
    fun onResume() {
        sensorManager.startListening()
        gvrView?.onResume()
    }
    
    fun onDestroy() {
        exitVR()
        gvrLayout?.shutdown()
    }
}

// Viewer parameters data class
data class ViewerParams(
    val name: String = "Cardboard V2",
    val ipdMeters: Float = 0.064f,  // 64mm average IPD
    val fovTop: Float = 50f,
    val fovBottom: Float = 50f,
    val fovLeft: Float = 50f,
    val fovRight: Float = 50f,
    val lensRadius: Float = 0.023f,
    val barrelDistortion: Float = 0.35f,
    val interLensDistance: Float = 0.064f,
    val verticalDistanceToLensCenter: Float = 0.036f
)

interface SceneCallback {
    fun onVREntered()
    fun onVRExited()
    fun onSceneLoaded(sceneGraph: SceneGraph)
    fun onHotspotActivated(hotspotId: String)
    fun onError(error: String)
}
```

#### 3.1.3 Head Tracking Pipeline

```kotlin
// HeadTrackingPipeline.kt
package com.xrvista.cardboard.tracking

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import kotlin.math.sqrt

class HeadTrackingPipeline(
    private val sensorManager: SensorManager
) {
    // Sensor references
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magnetometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    // Orientation state
    private val orientation = Quaternion()
    private val angularVelocity = Vector3()
    private val linearAcceleration = Vector3()
    
    // Sensor data buffers
    private val accelData = FloatArray(3)
    private val gyroData = FloatArray(3)
    private val magData = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    // Complementary filter coefficient
    private val alpha = 0.98f
    
    // Timestamps for integration
    private var lastGyroTimestamp = 0L
    private var lastUpdateTimestamp = 0L
    
    // Drift correction
    private val driftCorrector = DriftCorrector()
    
    fun processSensorEvent(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelData[0] = event.values[0]
                accelData[1] = event.values[1]
                accelData[2] = event.values[2]
                lastUpdateTimestamp = event.timestamp
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroData[0] = event.values[0]
                gyroData[1] = event.values[1]
                gyroData[2] = event.values[2]
                lastGyroTimestamp = event.timestamp
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magData[0] = event.values[0]
                magData[1] = event.values[1]
                magData[2] = event.values[2]
            }
        }
    }
    
    fun update(): Quaternion {
        // Step 1: Integrate gyroscope for angular velocity
        val dt = (System.nanoTime() - lastGyroTimestamp) / 1_000_000_000f
        angularVelocity.set(
            gyroData[0].toDouble(),
            gyroData[1].toDouble(),
            gyroData[2].toDouble()
        )
        
        // Quaternion integration: q' = q * (0, ω*dt/2)
        val deltaAngle = angularVelocity.length() * dt
        if (deltaAngle > 0.0001) {
            val axis = angularVelocity.normalize()
            val halfAngle = deltaAngle * 0.5f
            val sinHalf = Math.sin(halfAngle.toDouble()).toFloat()
            val deltaQ = Quaternion(
                Math.cos(halfAngle.toDouble()).toFloat(),
                axis.x * sinHalf,
                axis.y * sinHalf,
                axis.z * sinHalf
            )
            orientation.multiply(deltaQ)
            orientation.normalize()
        }
        
        // Step 2: Compute accelerometer-based orientation
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magData)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val accelOrientation = Quaternion.fromEulerAngles(
            orientationAngles[0],
            orientationAngles[1],
            orientationAngles[2]
        )
        
        // Step 3: Complementary filter fusion
        orientation.lerp(accelOrientation, 1.0f - alpha)
        orientation.normalize()
        
        // Step 4: Drift correction
        driftCorrector.correctDrift(orientation, dt)
        
        return orientation
    }
    
    fun getLinearAcceleration(): Vector3 {
        // Remove gravity from accelerometer
        val gravity = FloatArray(3)
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magData)
        
        gravity[0] = accelData[0]
        gravity[1] = accelData[1]
        gravity[2] = accelData[2]
        
        linearAcceleration.set(
            accelData[0].toDouble(),
            accelData[1].toDouble(),
            accelData[2].toDouble()
        )
        return linearAcceleration
    }
}

// Drift correction using magnetometer reference
class DriftCorrector {
    private var referenceOrientation: Quaternion? = null
    private val correctionStrength = 0.01f
    
    fun correctDrift(orientation: Quaternion, dt: Float) {
        // Magnetometer provides absolute heading reference
        // Slowly correct gyro drift toward magnetometer heading
        referenceOrientation?.let { ref ->
            val error = Quaternion.dot(orientation, ref)
            if (error < 0.99f) {
                orientation.slerp(ref, correctionStrength * dt)
            }
        }
    }
    
    fun setReference(ref: Quaternion) {
        referenceOrientation = ref
    }
}
```

#### 3.1.4 Render Loop

```kotlin
// CardboardVRRenderer.kt
package com.xrvista.cardboard.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import com.google.vr.ndk.base.GvrView

class CardboardVRRenderer(
    private val sceneCallback: SceneCallback
) : GvrView.Renderer {
    
    private var headTracker: HeadTrackingPipeline? = null
    private var stereoRenderer: StereoRenderer? = null
    private var sceneRenderer: SceneRenderer? = null
    
    // Frame timing
    private var lastFrameTimeNanos = 0L
    private val targetFrameTimeMs = 16.67L  // 60fps
    
    // Frame buffer objects for offscreen rendering
    private var leftFBO: FrameBufferObject? = null
    private var rightFBO: FrameBufferObject? = null
    
    override fun onRenderer(surfaceChanged: Int, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        
        val halfWidth = width / 2
        
        // Create frame buffer objects for stereoscopic rendering
        leftFBO = FrameBufferObject(halfWidth, height)
        rightFBO = FrameBufferObject(halfWidth, height)
        
        // Initialize scene renderer
        sceneRenderer = SceneRenderer().apply {
            init()
        }
        
        // Initialize stereo renderer with distortion correction
        stereoRenderer = StereoRenderer(
            leftFBO = leftFBO!!,
            rightFBO = rightFBO!!,
            distortionShader = DistortionShader(),
            screenWidth = width,
            screenHeight = height
        )
        
        sceneCallback.onSceneLoaded(sceneRenderer!!.currentScene)
    }
    
    override fun onDrawFrame(headTransform: GvrHeadTransform) {
        val currentTimeNanos = System.nanoTime()
        val deltaTimeMs = (currentTimeNanos - lastFrameTimeNanos) / 1_000_000.0
        lastFrameTimeNanos = currentTimeNanos
        
        // Frame rate limiter
        if (deltaTimeMs < targetFrameTimeMs) {
            Thread.sleep((targetFrameTimeMs - deltaTimeMs).toLong())
        }
        
        // Update head tracking
        val headOrientation = headTransform.quaternion
        val headEulerAngles = FloatArray(3)
        headTransform.eulerPitchYawRoll(headEulerAngles)
        
        // Update scene with head orientation
        sceneRenderer?.updateHeadOrientation(
            Quaternion(
                headOrientation[3],
                headOrientation[0],
                headOrientation[1],
                headOrientation[2]
            )
        )
        
        // Render left eye
        leftFBO?.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        sceneRenderer?.renderForEye(
            eye = Eye.LEFT,
            eyeOffset = -IPD / 2.0f,
            projection = headTransform.getLeftProjectionMatrix(0.1f, 100f)
        )
        leftFBO?.unbind()
        
        // Render right eye
        rightFBO?.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        sceneRenderer?.renderForEye(
            eye = Eye.RIGHT,
            eyeOffset = IPD / 2.0f,
            projection = headTransform.getRightProjectionMatrix(0.1f, 100f)
        )
        rightFBO?.unbind()
        
        // Apply distortion correction and compose side-by-side
        stereoRenderer?.renderStereo()
        
        // Track analytics
        sceneCallback.onFrameRendered(deltaTimeMs)
    }
    
    override fun onSurfaceCreated() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        
        // Enable VSync for tear-free rendering
        EGL14.eglSwapInterval(display, 1)
    }
    
    companion object {
        const val IPD = 0.064f  // 63-65mm average interpupillary distance
    }
}
```

---

## 4. Lens Distortion Correction

### 4.1 Barrel Distortion Shader

```glsl
// distortion_vertex.glsl
#version 300 es
precision highp float;

in vec4 aPosition;
in vec2 aTexCoord;

out vec2 vTexCoord;
out vec2 vDistortedCoord;

uniform mat4 uModelViewProjection;
uniform vec2 uLensCenter;
uniform float uBarrelDistortion;
uniform float uChromaticAberration;

void main() {
    gl_Position = uModelViewProjection * aPosition;
    vTexCoord = aTexCoord;
    
    // Apply barrel distortion
    vec2 r = aTexCoord - uLensCenter;
    float rDotDot = dot(r, r);
    
    // Polynomial distortion coefficients
    float distortion = 1.0 + uBarrelDistortion * rDotDot 
                            + uBarrelDistortion * rDotDot * rDotDot;
    
    vDistortedCoord = uLensCenter + r * distortion;
}
```

```glsl
// distortion_fragment.glsl
#version 300 es
precision highp float;

in vec2 vTexCoord;
in vec2 vDistortedCoord;

out vec4 fragColor;

uniform sampler2D uTexture;
uniform float uChromaticAberration;
uniform float uVignetteIntensity;
uniform vec2 uLensCenter;

void main() {
    vec2 r = vDistortedCoord - uLensCenter;
    float rLength = length(r);
    
    // Chromatic aberration correction (separate RGB channels)
    float aberrationScale = 1.0 + uChromaticAberration * rLength * rLength;
    
    vec2 redCoord = uLensCenter + r * (aberrationScale + 0.001);
    vec2 greenCoord = vDistortedCoord;
    vec2 blueCoord = uLensCenter + r * (aberrationScale - 0.001);
    
    float red = texture(uTexture, redCoord).r;
    float green = texture(uTexture, greenCoord).g;
    float blue = texture(uTexture, blueCoord).b;
    
    // Vignette (darkening at edges)
    float vignette = 1.0 - uVignetteIntensity * rLength * rLength;
    
    fragColor = vec4(red * vignette, green * vignette, blue * vignette, 1.0);
}
```

### 4.2 IPD Configuration

```kotlin
// IPDManager.kt
package com.xrvista.cardboard.distortion

data class IPDConfig(
    val interpupillaryDistanceMm: Float = 63f,  // Average adult IPD
    val minIPDMm: Float = 55f,
    val maxIPDMm: Float = 75f,
    val eyeOffsetLeft: Float = 0f,
    val eyeOffsetRight: Float = 0f
) {
    val ipdMeters: Float get() = interpupillaryDistanceMm / 1000f
    val halfIPDMeters: Float get() = ipdMeters / 2f
    
    fun getProjectionMatrix(eye: Eye, near: Float, far: Float): FloatArray {
        val aspect = 0.5f  // Half-width for side-by-side
        val fov = when (eye) {
            Eye.LEFT -> 80f
            Eye.RIGHT -> 80f
        }
        return createPerspectiveMatrix(fov, aspect, near, far)
    }
    
    private fun createPerspectiveMatrix(
        fovDegrees: Float, 
        aspect: Float, 
        near: Float, 
        far: Float
    ): FloatArray {
        val fovRad = Math.toRadians(fovDegrees.toDouble()).toFloat()
        val f = 1.0f / Math.tan(fovRad / 2.0f)
        return floatArrayOf(
            f / aspect, 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, (far + near) / (near - far), -1f,
            0f, 0f, (2f * far * near) / (near - far), 0f
        )
    }
}

enum class Eye { LEFT, RIGHT }
```

### 4.3 FOV Settings Per Viewer Profile

```kotlin
// ViewerProfileManager.kt
package com.xrvista.cardboard.profile

data class ViewerProfile(
    val id: String,
    val name: String,
    val manufacturer: String,
    val interpupillaryDistanceMm: Float,
    val barrelDistortion: Float,
    val chromaticAberration: Float,
    val fieldOfView: FOV,
    val lensRadius: Float,
    val interLensDistance: Float,
    val screenToLensDistance: Float,
    val hasMagnetButton: Boolean,
    val hasNfc: Boolean
)

data class FOV(
    val top: Float,
    val bottom: Float,
    val left: Float,
    val right: Float
) {
    val totalHorizontal: Float get() = left + right
    val totalVertical: Float get() = top + bottom
}

object ViewerProfileManager {
    
    val CARDBOARD_V1 = ViewerProfile(
        id = "cardboard_v1",
        name = "Google Cardboard V1",
        manufacturer = "Google",
        interpupillaryDistanceMm = 60f,
        barrelDistortion = 0.4f,
        chromaticAberration = 0.02f,
        fieldOfView = FOV(50f, 50f, 40f, 40f),
        lensRadius = 0.022f,
        interLensDistance = 0.060f,
        screenToLensDistance = 0.042f,
        hasMagnetButton = true,
        hasNfc = true
    )
    
    val CARDBOARD_V2 = ViewerProfile(
        id = "cardboard_v2",
        name = "Google Cardboard V2",
        manufacturer = "Google",
        interpupillaryDistanceMm = 63f,
        barrelDistortion = 0.34f,
        chromaticAberration = 0.015f,
        fieldOfView = FOV(50f, 50f, 50f, 50f),
        lensRadius = 0.023f,
        interLensDistance = 0.064f,
        screenToLensDistance = 0.039f,
        hasMagnetButton = false,
        hasNfc = true
    )
    
    val CUSTOM_PROFILES = mutableMapOf<String, ViewerProfile>()
    
    fun getProfile(id: String): ViewerProfile {
        return when (id) {
            "cardboard_v1" -> CARDBOARD_V1
            "cardboard_v2" -> CARDBOARD_V2
            else -> CUSTOM_PROFILES[id] ?: CARDBOARD_V2
        }
    }
    
    fun registerCustomProfile(profile: ViewerProfile) {
        CUSTOM_PROFILES[profile.id] = profile
    }
    
    fun getProfileFromQRCode(qrData: String): ViewerProfile? {
        return try {
            val params = parseNFCParams(qrData)
            ViewerProfile(
                id = params["vendor"] ?: "unknown",
                name = params["model"] ?: "Custom Viewer",
                manufacturer = params["vendor"] ?: "Unknown",
                interpupillaryDistanceMm = params["ipd"]?.toFloatOrNull() ?: 63f,
                barrelDistortion = params["coefficients"]?.split(",")?.firstOrNull()?.toFloatOrNull() ?: 0.34f,
                chromaticAberration = 0.015f,
                fieldOfView = FOV(50f, 50f, 50f, 50f),
                lensRadius = params["lens_radius"]?.toFloatOrNull() ?: 0.023f,
                interLensDistance = params["inter_lens_distance"]?.toFloatOrNull() ?: 0.064f,
                screenToLensDistance = params["screen_to_lens"]?.toFloatOrNull() ?: 0.039f,
                hasMagnetButton = false,
                hasNfc = true
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseNFCParams(data: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        data.split("&").forEach { param ->
            val (key, value) = param.split("=")
            params[key] = value
        }
        return params
    }
}
```

---

## 5. Viewer Profiles

### 5.1 Supported Viewer Profiles

| Profile | IPD (mm) | Distortion | FOV | Button | NFC |
|---------|----------|------------|-----|--------|-----|
| Cardboard V1 | 60 | 0.40 | 80°×100° | Magnet | Yes |
| Cardboard V2 | 63 | 0.34 | 100°×100° | No | Yes |
| Custom | 55-75 | 0.2-0.5 | Variable | Variable | Variable |

### 5.2 QR Code Detection Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Camera     │────▶│  QR Scanner │────▶│  Profile    │
│  Preview    │     │  (ML Kit)   │     │  Lookup     │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
                                       ┌─────────────┐
                                       │  Apply      │
                                       │  Distortion │
                                       │  Params     │
                                       └─────────────┘
```

```kotlin
// QRCodeScanner.kt
package com.xrvista.cardboard.scanner

import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeScanner(
    private val onProfileDetected: (ViewerProfile) -> Unit
) {
    private val scanner = BarcodeScanning.getClient()
    
    fun scanFromCamera(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(
            mediaImage, 
            imageProxy.imageInfo.rotationDegrees
        )
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_URL) {
                        val url = barcode.url?.url ?: continue
                        if (url.contains("cardboard") || url.contains("viewer")) {
                            val profile = ViewerProfileManager.getProfileFromQRCode(url)
                            profile?.let { onProfileDetected(it) }
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
```

---

## 6. Gyroscope & Motion Sensors

### 6.1 Sensor Fusion Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SENSOR FUSION PIPELINE                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Accelerometer│  │  Gyroscope   │  │ Magnetometer │      │
│  │  1000 Hz     │  │  1000 Hz     │  │  100 Hz      │      │
│  │              │  │              │  │              │      │
│  │  - Gravity   │  │  - Angular   │  │  - Magnetic  │      │
│  │    vector    │  │    velocity  │  │    field     │      │
│  │  - Linear   │  │  - Orientation│  │  - Heading   │      │
│  │    accel     │  │    delta     │  │    reference │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │               │
│         ▼                 ▼                 ▼               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              COMPLEMENTARY FILTER                     │  │
│  │                                                       │  │
│  │  orientation = α × (gyro_integral)                   │  │
│  │              + (1-α) × (accel_magnet_estimate)       │  │
│  │                                                       │  │
│  │  where α = 0.98 (gyro weight)                        │  │
│  └──────────────────────┬───────────────────────────────┘  │
│                         │                                   │
│                         ▼                                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              DRIFT CORRECTION                         │  │
│  │                                                       │  │
│  │  - Magnetometer provides absolute heading             │  │
│  │  - Slow correction toward reference orientation       │  │
│  │  - Correction strength: 0.01 × Δt                    │  │
│  └──────────────────────┬───────────────────────────────┘  │
│                         │                                   │
│                         ▼                                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              OUTPUT                                   │  │
│  │                                                       │  │
│  │  - Quaternion orientation (x, y, z, w)               │  │
│  │  - Euler angles (pitch, yaw, roll)                   │  │
│  │  - Angular velocity (rad/s)                          │  │
│  │  - Linear acceleration (m/s²)                        │  │
│  │                                                       │  │
│  │  Latency: < 20ms end-to-end                          │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 Sensor Sampling Configuration

```kotlin
// SensorConfig.kt
package com.xrvista.cardboard.config

object SensorConfig {
    // Sampling rates (microseconds)
    const val ACCELEROMETER_INTERVAL_US = 1000L   // 1000 Hz
    const val GYROSCOPE_INTERVAL_US = 1000L       // 1000 Hz
    const val MAGNETOMETER_INTERVAL_US = 10000L   // 100 Hz
    
    // Sensor fusion
    const val COMPLEMENTARY_FILTER_ALPHA = 0.98f
    const val DRIFT_CORRECTION_STRENGTH = 0.01f
    
    // Performance
    const val SENSOR_BUFFER_SIZE = 100
    const val MAX_LATENCY_MS = 20L
    
    // Low-pass filter for noise reduction
    const val LOW_PASS_ALPHA = 0.8f
    
    // Calibration
    const val CALIBRATION_SAMPLES = 100
    const val GYROSCOPE_NOISE_THRESHOLD = 0.01f
}
```

### 6.3 Sensor Fusion Manager

```kotlin
// SensorFusionManager.kt
package com.xrvista.cardboard.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorFusionManager(context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) 
        as SensorManager
    
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    val fusedOrientation = Quaternion()
    val fusedPosition = Vector3()
    
    private val headTrackingPipeline = HeadTrackingPipeline(sensorManager)
    
    // Accelerometer low-pass filter
    private val accelFiltered = FloatArray(3)
    private val accelPrevious = FloatArray(3)
    
    fun startListening() {
        gyroscope?.let {
            sensorManager.registerListener(
                this, it, 
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        accelerometer?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        magnetometer?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Apply low-pass filter
                for (i in 0..2) {
                    accelFiltered[i] = accelPrevious[i] + SensorConfig.LOW_PASS_ALPHA *
                        (event.values[i] - accelPrevious[i])
                    accelPrevious[i] = accelFiltered[i]
                }
                event.values[0] = accelFiltered[0]
                event.values[1] = accelFiltered[1]
                event.values[2] = accelFiltered[2]
            }
        }
        
        headTrackingPipeline.processSensorEvent(event)
        val orientation = headTrackingPipeline.update()
        fusedOrientation.copyFrom(orientation)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                // Increase magnetometer weight
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                // Normal operation
            }
        }
    }
}
```

### 6.4 Latency Budget

| Component | Budget | Actual |
|-----------|--------|--------|
| Sensor read | 1ms | 0.8ms |
| Sensor fusion | 2ms | 1.5ms |
| Orientation update | 1ms | 0.5ms |
| Scene update | 3ms | 2.8ms |
| Render submission | 8ms | 7.2ms |
| GPU render | 10ms | 9.5ms |
| Display vsync | 16.7ms | 16.7ms |
| **Total** | **41.7ms** | **39ms** |

---

## 7. Stereoscopic Rendering Pipeline

### 7.1 Side-by-Side Rendering

```
┌─────────────────────────────────────────────────────────────┐
│                    STEREOSCOPIC RENDER PIPELINE               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  Scene Graph                         │    │
│  │  - 360 Background (equirectangular)                 │    │
│  │  - 3D Models (glTF/GLB)                             │    │
│  │  - Hotspot Sprites                                  │    │
│  │  - Audio Sources                                    │    │
│  │  - UI Overlays                                      │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                    │
│                         ▼                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │            Render-to-Texture (FBO)                   │    │
│  │                                                      │    │
│  │  ┌─────────────────┐    ┌─────────────────┐        │    │
│  │  │   Left FBO      │    │   Right FBO     │        │    │
│  │  │   (1920×1080)   │    │   (1920×1080)   │        │    │
│  │  │                 │    │                 │        │    │
│  │  │  Camera:        │    │  Camera:        │        │    │
│  │  │  pos.x = -IPD/2 │    │  pos.x = +IPD/2 │        │    │
│  │  │  fov = 80°      │    │  fov = 80°      │        │    │
│  │  │  near = 0.1     │    │  near = 0.1     │        │    │
│  │  │  far = 100      │    │  far = 100      │        │    │
│  │  └────────┬────────┘    └────────┬────────┘        │    │
│  └───────────┼──────────────────────┼──────────────────┘    │
│              │                      │                       │
│              ▼                      ▼                       │
│  ┌─────────────────────────────────────────────────────┐    │
│  │         Distortion Post-Processing                   │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │  Barrel Distortion Shader                     │  │    │
│  │  │  - Applies lens distortion to each eye       │  │    │
│  │  │  - Corrects for biconvex lens optics         │  │    │
│  │  │  - Parameters from viewer profile            │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │  Chromatic Aberration Correction              │  │    │
│  │  │  - Separates RGB channels                    │  │    │
│  │  │  - Adjusts per-channel offset               │  │    │
│  │  │  - Reduces color fringing                   │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │  Vignette (Optional)                         │  │    │
│  │  │  - Darkens edges of each eye                 │  │    │
│  │  │  - Reduces visual artifacts                  │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                    │
│                         ▼                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Side-by-Side Composition                 │    │
│  │                                                      │    │
│  │  ┌─────────────────┬─────────────────┐              │    │
│  │  │                 │                 │              │    │
│  │  │    LEFT HALF    │   RIGHT HALF    │              │    │
│  │  │   (Left Eye)    │   (Right Eye)   │              │    │
│  │  │                 │                 │              │    │
│  │  │   960×1080      │   960×1080      │              │    │
│  │  │                 │                 │              │    │
│  │  └─────────────────┴─────────────────┘              │    │
│  │                                                      │    │
│  │  Final Resolution: 1920×1080 (per eye)              │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 Single-Pass Stereo Rendering

```kotlin
// SinglePassStereoRenderer.kt
package com.xrvista.cardboard.renderer

import android.opengl.GLES30

class SinglePassStereoRenderer {
    
    private var stereoShaderProgram: Int = 0
    private var leftEyeTexture: Int = 0
    private var rightEyeTexture: Int = 0
    
    fun init() {
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, STEREO_VERTEX_SHADER)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, STEREO_FRAGMENT_SHADER)
        
        stereoShaderProgram = GLES30.glCreateProgram()
        GLES30.glAttachShader(stereoShaderProgram, vertexShader)
        GLES30.glAttachShader(stereoShaderProgram, fragmentShader)
        GLES30.glLinkProgram(stereoShaderProgram)
    }
    
    fun renderStereo(
        leftFBO: FrameBufferObject,
        rightFBO: FrameBufferObject,
        screenWidth: Int,
        screenHeight: Int
    ) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, screenWidth, screenHeight)
        
        GLES30.glUseProgram(stereoShaderProgram)
        
        // Bind left eye texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, leftFBO.textureId)
        GLES30.glUniform1i(
            GLES30.glGetUniformLocation(stereoShaderProgram, "uLeftTexture"), 0
        )
        
        // Bind right eye texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, rightFBO.textureId)
        GLES30.glUniform1i(
            GLES30.glGetUniformLocation(stereoShaderProgram, "uRightTexture"), 1
        )
        
        // Render full-screen quad
        drawFullScreenQuad()
        
        GLES30.glUseProgram(0)
    }
    
    companion object {
        private const val STEREO_VERTEX_SHADER = """
            #version 300 es
            in vec4 aPosition;
            in vec2 aTexCoord;
            out vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """
        
        private const val STEREO_FRAGMENT_SHADER = """
            #version 300 es
            precision highp float;
            
            in vec2 vTexCoord;
            out vec4 fragColor;
            
            uniform sampler2D uLeftTexture;
            uniform sampler2D uRightTexture;
            
            void main() {
                if (vTexCoord.x < 0.5) {
                    vec2 leftUV = vec2(vTexCoord.x * 2.0, vTexCoord.y);
                    fragColor = texture(uLeftTexture, leftUV);
                } else {
                    vec2 rightUV = vec2((vTexCoord.x - 0.5) * 2.0, vTexCoord.y);
                    fragColor = texture(uRightTexture, rightUV);
                }
            }
        """
    }
}
```

---

## 8. WebView Configuration

### 8.1 Android WebView

```kotlin
// WebViewConfig.kt (Android)
package com.xrvista.cardboard.webview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object WebViewConfigurator {
    
    @SuppressLint("SetJavaScriptEnabled")
    fun configureForVR(context: Context, webView: WebView): WebView {
        return webView.apply {
            settings.apply {
                // JavaScript for AFrame/ThreeJS
                javaScriptEnabled = true
                
                // Hardware acceleration for WebGL
                @Suppress("DEPRECATION")
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                
                // Enable hardware acceleration
                @Suppress("DEPRECATION")
                setHardwareAccelerated(true)
                
                // Cache settings for offline VR
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                
                // Media settings
                mediaPlaybackRequiresUserGesture = false
                loadWithOverviewMode = true
                useWideViewPort = true
                
                // WebXR support
                domStorageEnabled = true
                databaseEnabled = true
                
                // Disable scrollbars
                setVerticalScrollBarEnabled(false)
                setHorizontalScrollBarEnabled(false)
                
                // Allow mixed content (for development)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Enable WebGL
                @Suppress("DEPRECATION")
                setAllowFileAccess(true)
                setAllowContentAccess(true)
            }
            
            // WebViewClient for URL handling
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // Allow all URLs within the app
                    return false
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Inject sensor data bridge
                    view?.evaluateJavascript("""
                        window.xrvista = {
                            sensorData: { pitch: 0, yaw: 0, roll: 0 },
                            triggerPressed: false,
                            onTriggerPress: null,
                            
                            setOrientation: function(pitch, yaw, roll) {
                                this.sensorData.pitch = pitch;
                                this.sensorData.yaw = yaw;
                                this.sensorData.roll = roll;
                            },
                            
                            onTrigger: function(callback) {
                                this.onTriggerPress = callback;
                            }
                        };
                    """.trimIndent(), null)
                }
            }
            
            // WebChromeClient for media
            webChromeClient = WebChromeClient()
            
            // Load XR scene
            loadUrl("https://viewer.xrvista.com/vr/index.html")
        }
    }
}
```

### 8.2 iOS WebView (WKWebView)

```swift
// WebViewConfigurator.swift (iOS)
import WebKit

class WebViewConfigurator {
    
    static func configureForVR(webView: WKWebView) {
        let config = webView.configuration
        
        // Enable inline media playback
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        
        // Enable WebGL
        config.preferences.setValue(true, forKey: "allowFileAccessFromFileURLs")
        
        // Enable drag and drop
        if #available(iOS 11.0, *) {
            config.dataDetectorTypes = []
        }
        
        // JavaScript configuration
        let preferences = WKPreferences()
        preferences.setValue(true, forKey: "javaScriptEnabled")
        config.preferences = preferences
        
        // Process pool for session sharing
        let processPool = WKProcessPool()
        config.processPool = processPool
        
        // User agent for VR detection
        webView.customUserAgent = "XRVista-Cardboard-iOS"
        
        // Navigation delegate
        webView.navigationDelegate = VRNavigationDelegate()
        
        // Load VR scene
        if let url = URL(string: "https://viewer.xrvista.com/vr/index.html") {
            webView.load(URLRequest(url: url))
        }
    }
    
    // JavaScript bridge for sensor injection
    static func injectSensorBridge(webView: WKWebView) {
        let script = """
            window.xrvista = {
                sensorData: { pitch: 0, yaw: 0, roll: 0 },
                triggerPressed: false,
                
                setOrientation: function(pitch, yaw, roll) {
                    this.sensorData.pitch = pitch;
                    this.sensorData.yaw = yaw;
                    this.sensorData.roll = roll;
                    
                    // Notify native side
                    window.webkit.messageHandlers.sensorUpdate.postMessage({
                        pitch: pitch,
                        yaw: yaw,
                        roll: roll
                    });
                }
            };
        """
        
        let userScript = WKUserScript(
            source: script,
            injectionTime: .atDocumentEnd,
            forMainFrameOnly: true
        )
        webView.configuration.userContentController.addUserScript(userScript)
    }
}

// Message handler for sensor updates
class SensorMessageHandler: NSObject, WKScriptMessageHandler {
    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        if message.name == "sensorUpdate",
           let body = message.body as? [String: Any],
           let pitch = body["pitch"] as? Double,
           let yaw = body["yaw"] as? Double,
           let roll = body["roll"] as? Double {
            // Update scene with new orientation
            SceneRenderer.shared.updateOrientation(
                pitch: pitch, yaw: yaw, roll: roll
            )
        }
    }
}
```

---

## 9. Performance Optimization

### 9.1 Target Performance Metrics

| Metric | Target | Minimum | Measurement |
|--------|--------|---------|-------------|
| Frame Rate | 72 fps | 60 fps | GPU profiler |
| Frame Time | 13.9ms | 16.7ms | Render doc |
| Motion-to-Photon | < 20ms | < 30ms | End-to-end |
| GPU Memory | < 200MB | < 300MB | Memory profiler |
| App Memory | < 300MB | < 500MB | Android Studio |
| CPU Usage | < 40% | < 60% | Top command |
| Thermal | No throttling | Minimal | Device thermal |

### 9.2 Optimization Strategies

```kotlin
// PerformanceOptimizer.kt
package com.xrvista.cardboard.perf

class PerformanceOptimizer {
    
    // Texture atlas for hotspots
    fun createTextureAtlas(hotspots: List<Hotspot>): TextureAtlas {
        val atlas = TextureAtlas()
        
        // Pack all hotspot icons into single texture
        val images = hotspots.map { loadIcon(it.iconUrl) }
        atlas.pack(images, maxSize = 2048)
        
        // Use single draw call for all hotspots
        return atlas
    }
    
    // LOD (Level of Detail) management
    fun selectLOD(distance: Float, model: Model3D): LODLevel {
        return when {
            distance < 5f -> model.highDetail
            distance < 20f -> model.mediumDetail
            distance < 50f -> model.lowDetail
            else -> model.minimumDetail
        }
    }
    
    // Lazy loading of 3D assets
    suspend fun loadAssetsLazily(
        scene: SceneGraph,
        currentPose: Quaternion
    ): List<LoadedAsset> {
        val visibleNodes = scene.nodes.filter { node ->
            isNodeInFOV(node, currentPose, fov = 120f)
        }
        
        return visibleNodes.mapNotNull { node ->
            when (node.type) {
                NodeType.MODEL_3D -> loadModel(node.assetUrl)
                NodeType.VIDEO_360 -> loadVideo(node.assetUrl)
                NodeType.AUDIO -> loadAudio(node.assetUrl)
                else -> null
            }
        }
    }
    
    // Frame timing management
    class FrameTimer {
        private var frameCount = 0
        private var lastTime = System.nanoTime()
        private var averageFrameTime = 0.0
        
        fun measureFrame(): Double {
            frameCount++
            val currentTime = System.nanoTime()
            val elapsed = (currentTime - lastTime) / 1_000_000.0
            lastTime = currentTime
            
            averageFrameTime = (averageFrameTime * (frameCount - 1) + elapsed) / frameCount
            
            return averageFrameTime
        }
        
        fun shouldDropFrame(): Boolean {
            return averageFrameTime > 20.0  // > 20ms = drop to prevent jank
        }
    }
    
    // GPU memory management
    fun manageGPUMemory() {
        // Unload textures not in view frustum
        val textureCache = TextureCache(maxSize = 150 * 1024 * 1024) // 150MB
        
        // Compress textures based on device capability
        val textureFormat = when (getDeviceGPULevel()) {
            GPULevel.HIGH -> TextureFormat.ASTC_4x4
            GPULevel.MEDIUM -> TextureFormat.ETC2
            GPULevel.LOW -> TextureFormat.PVRTC_4bpp
        }
    }
}

// Device capability detection
class DeviceCapabilities(context: Context) {
    val gpuLevel: GPULevel = detectGPULevel()
    val maxTextureSize: Int = getMaxTextureSize()
    val supportsASTC: Boolean = checkASTCSupport()
    
    private fun detectGPULevel(): GPULevel {
        val renderer = getGLRenderer()
        return when {
            renderer.contains("Adreno 6") || renderer.contains("Mali-G7") -> GPULevel.HIGH
            renderer.contains("Adreno 5") || renderer.contains("Mali-G5") -> GPULevel.MEDIUM
            else -> GPULevel.LOW
        }
    }
    
    private fun getGLRenderer(): String {
        val glView = GLSurfaceView(Context())
        val info = StringBuilder()
        glView.setRenderer { gl ->
            info.append(GLES20.glGetString(GLES20.GL_RENDERER))
        }
        return info.toString()
    }
}

enum class GPULevel { LOW, MEDIUM, HIGH }
```

### 9.3 Render Quality Settings

```kotlin
// QualitySettings.kt
package com.xrvista.cardboard.quality

data class QualityPreset(
    val name: String,
    val renderScale: Float,
    val msaa: Int,
    val textureQuality: TextureQuality,
    val shadowEnabled: Boolean,
    val postProcessing: Boolean
)

object QualityPresets {
    
    val LOW = QualityPreset(
        name = "Low",
        renderScale = 0.75f,
        msaa = 0,
        textureQuality = TextureQuality.HALF,
        shadowEnabled = false,
        postProcessing = false
    )
    
    val MEDIUM = QualityPreset(
        name = "Medium",
        renderScale = 1.0f,
        msaa = 2,
        textureQuality = TextureQuality.FULL,
        shadowEnabled = false,
        postProcessing = true
    )
    
    val HIGH = QualityPreset(
        name = "High",
        renderScale = 1.0f,
        msaa = 4,
        textureQuality = TextureQuality.FULL,
        shadowEnabled = true,
        postProcessing = true
    )
    
    fun selectPreset(deviceCapabilities: DeviceCapabilities): QualityPreset {
        return when (deviceCapabilities.gpuLevel) {
            GPULevel.HIGH -> HIGH
            GPULevel.MEDIUM -> MEDIUM
            GPULevel.LOW -> LOW
        }
    }
}

enum class TextureQuality { HALF, FULL, DOUBLE }
```

---

## 10. User Interaction in Cardboard

### 10.1 Input Methods

```
┌─────────────────────────────────────────────────────────────┐
│                 INPUT HANDLING METHODS                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. MAGNETIC TRIGGER (Cardboard V1)                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Magnet sensor detects magnetic field change       │    │
│  │  - Single click: Select/Activate                     │    │
│  │  - Duration < 500ms = Click                          │    │
│  │  - Duration > 500ms = Long Press                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  2. SCREEN TAP (Fallback)                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Center screen tap                                │    │
│  │  - Touch duration < 300ms = Click                    │    │
│  │  - Touch duration > 500ms = Long Press               │    │
│  │  - Double tap = Menu toggle                          │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  3. GAZE-BASED SELECTION                                     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Reticle follows head movement                    │    │
│  │  - Dwell time: 1.5 seconds on target                │    │
│  │  - Visual feedback: Progress ring fills             │    │
│  │  - Audio feedback: Confirmation sound               │    │
│  │  - Cancellation: Look away for > 300ms             │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  4. AUTO-PLAY (360 Video)                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Video starts on scene load                       │    │
│  │  - Gaze direction controls playback speed           │    │
│  │  - Look down to see controls                        │    │
│  │  - Look center to resume                            │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Gaze Selection Implementation

```kotlin
// GazeSelector.kt
package com.xrvista.cardboard.input

class GazeSelector(
    private val onSelectionComplete: (String) -> Unit,
    private val onSelectionProgress: (Float) -> Unit
) {
    private var dwellTimeMs = 1500L
    private var lookAwayThresholdMs = 300L
    private var startTime = 0L
    private var isLookingAtTarget = false
    private var currentTargetId: String? = null
    
    fun update(
        headOrientation: Quaternion,
        hotspots: List<HotspotNode>,
        screenWidth: Int,
        screenHeight: Int
    ) {
        // Cast ray from head direction
        val ray = createRayFromOrientation(headOrientation)
        
        // Find intersected hotspot
        val intersectedHotspot = hotspots.firstOrNull { hotspot ->
            rayIntersectsSphere(ray, hotspot.boundingSphere)
        }
        
        val currentTime = System.currentTimeMillis()
        
        if (intersectedHotspot != null) {
            if (currentTargetId == intersectedHotspot.id) {
                // Still looking at same target
                val elapsed = currentTime - startTime
                val progress = (elapsed.toFloat() / dwellTimeMs).coerceIn(0f, 1f)
                onSelectionProgress(progress)
                
                if (elapsed >= dwellTimeMs) {
                    // Selection complete
                    onSelectionComplete(intersectedHotspot.id)
                    reset()
                }
            } else {
                // New target
                currentTargetId = intersectedHotspot.id
                startTime = currentTime
                isLookingAtTarget = true
            }
        } else {
            // Not looking at any target
            if (isLookingAtTarget) {
                val elapsed = currentTime - startTime
                if (elapsed > lookAwayThresholdMs) {
                    // Looked away long enough, cancel selection
                    reset()
                }
            }
        }
    }
    
    private fun reset() {
        startTime = 0L
        isLookingAtTarget = false
        currentTargetId = null
        onSelectionProgress(0f)
    }
    
    private fun createRayFromOrientation(orientation: Quaternion): Ray {
        val forward = orientation * Vector3(0f, 0f, -1f)
        return Ray(
            origin = Vector3(0f, 0f, 0f),
            direction = forward.normalize()
        )
    }
    
    private fun rayIntersectsSphere(ray: Ray, sphere: BoundingSphere): Boolean {
        val oc = ray.origin - sphere.center
        val a = ray.direction.dot(ray.direction)
        val b = 2.0f * oc.dot(ray.direction)
        val c = oc.dot(oc) - sphere.radius * sphere.radius
        val discriminant = b * b - 4 * a * c
        return discriminant >= 0
    }
}
```

### 10.3 Reticle Renderer

```kotlin
// ReticleRenderer.kt
package com.xrvista.cardboard.ui

class ReticleRenderer {
    
    private var reticleProgram: Int = 0
    private var progressUniform: Int = 0
    private var colorUniform: Int = 0
    
    private var currentProgress = 0f
    private var reticleColor = floatArrayOf(1f, 1f, 1f, 0.8f)
    
    fun init() {
        reticleProgram = compileShaderProgram(RETICLE_VERTEX, RETICLE_FRAGMENT)
        progressUniform = GLES20.glGetUniformLocation(reticleProgram, "uProgress")
        colorUniform = GLES20.glGetUniformLocation(reticleProgram, "uColor")
    }
    
    fun render(progress: Float, isHovering: Boolean) {
        GLES20.glUseProgram(reticleProgram)
        
        // Update progress ring
        GLES20.glUniform1f(progressUniform, progress)
        
        // Update color based on state
        if (isHovering) {
            reticleColor = floatArrayOf(0.2f, 0.8f, 1f, 0.9f) // Cyan
        } else {
            reticleColor = floatArrayOf(1f, 1f, 1f, 0.6f) // White
        }
        GLES20.glUniform4fv(colorUniform, 1, reticleColor, 0)
        
        // Draw reticle
        drawReticle()
        
        GLES20.glUseProgram(0)
    }
    
    companion object {
        private const val RETICLE_VERTEX = """
            #version 300 es
            in vec4 aPosition;
            uniform mat4 uModelViewProjection;
            void main() {
                gl_Position = uModelViewProjection * aPosition;
            }
        """
        
        private const val RETICLE_FRAGMENT = """
            #version 300 es
            precision highp float;
            
            in vec2 vTexCoord;
            out vec4 fragColor;
            
            uniform float uProgress;
            uniform vec4 uColor;
            
            void main() {
                vec2 center = vTexCoord - vec2(0.5);
                float dist = length(center);
                
                // Inner circle (solid)
                if (dist < 0.15) {
                    fragColor = vec4(uColor.rgb, uColor.a * 0.3);
                }
                // Progress ring
                else if (dist < 0.25) {
                    float angle = atan(center.y, center.x);
                    float normalizedAngle = (angle + 3.14159) / (2.0 * 3.14159);
                    if (normalizedAngle <= uProgress) {
                        fragColor = vec4(uColor.rgb, uColor.a);
                    } else {
                        discard;
                    }
                }
                // Outer ring (static)
                else if (dist < 0.3) {
                    fragColor = vec4(uColor.rgb, uColor.a * 0.5);
                }
                else {
                    discard;
                }
            }
        """
    }
}
```

---

## 11. Device Compatibility Matrix

### 11.1 Feature Support Matrix

| Feature | Android 8+ | Android 10+ | Android 12+ | iOS 14+ | iOS 16+ |
|---------|-----------|-------------|-------------|---------|---------|
| Basic VR | ✅ | ✅ | ✅ | ✅ | ✅ |
| Gyroscope | ✅ | ✅ | ✅ | ✅ | ✅ |
| Accelerometer | ✅ | ✅ | ✅ | ✅ | ✅ |
| Magnetometer | ✅ | ✅ | ✅ | ⚠️ | ⚠️ |
| WebXR | ❌ | ⚠️ | ✅ | ❌ | ✅ |
| WebXR Fallback | ✅ | ✅ | ✅ | ✅ | ✅ |
| ARCore | ❌ | ⚠️ | ✅ | ❌ | ❌ |
| ARKit | ❌ | ❌ | ❌ | ❌ | ⚠️ |
| Hand Tracking | ❌ | ❌ | ❌ | ❌ | ❌ |
| 6DOF | ❌ | ❌ | ❌ | ❌ | ❌ |
| Haptic Feedback | ✅ | ✅ | ✅ | ✅ | ✅ |
| 360 Video | ✅ | ✅ | ✅ | ✅ | ✅ |
| 3D Model Loading | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spatial Audio | ✅ | ✅ | ✅ | ✅ | ✅ |

✅ = Full Support, ⚠️ = Partial/Limited, ❌ = Not Supported

### 11.2 Performance by Device Tier

| Tier | Device Examples | GPU | Target FPS | Quality |
|------|----------------|-----|------------|---------|
| High | Galaxy S23, iPhone 14 | Adreno 740, A15 | 72fps | High |
| Medium | Pixel 7, iPhone 12 | Mali-G710, A14 | 60fps | Medium |
| Low | Galaxy A52, iPhone SE | Adreno 610, A13 | 60fps | Low |
| Minimum | Galaxy A12, iPhone 8 | Adreno 505, A11 | 30fps | Minimum |

### 11.3 Device Detection

```kotlin
// DeviceDetector.kt
package com.xrvista.cardboard.device

import android.os.Build
import android.content.Context

class DeviceDetector(context: Context) {
    
    fun detectCapabilities(): DeviceCapabilities {
        return DeviceCapabilities(
            hasGyroscope = checkGyroscope(context),
            hasAccelerometer = checkAccelerometer(context),
            hasMagnetometer = checkMagnetometer(context),
            screenResolution = getScreenResolution(context),
            gpuLevel = detectGPULevel(),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemoryMB = getTotalMemoryMB(context),
            sdkVersion = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL
        )
    }
    
    private fun checkGyroscope(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) 
            as android.hardware.SensorManager
        return sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE) != null
    }
    
    private fun detectGPULevel(): GPULevel {
        val renderer = getGLRenderer()
        return when {
            renderer.contains("Adreno 6") || 
            renderer.contains("Mali-G7") ||
            renderer.contains("Apple GPU") -> GPULevel.HIGH
            
            renderer.contains("Adreno 5") || 
            renderer.contains("Mali-G5") -> GPULevel.MEDIUM
            
            else -> GPULevel.LOW
        }
    }
    
    private fun getGLRenderer(): String {
        // Get OpenGL renderer string
        val glView = android.opengl.GLSurfaceView(context)
        return "" // Simplified for brevity
    }
    
    private fun getScreenResolution(context: Context): Pair<Int, Int> {
        val displayMetrics = context.resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    
    private fun getTotalMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) 
            as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / (1024 * 1024)
    }
}

data class DeviceCapabilities(
    val hasGyroscope: Boolean,
    val hasAccelerometer: Boolean,
    val hasMagnetometer: Boolean,
    val screenResolution: Pair<Int, Int>,
    val gpuLevel: GPULevel,
    val cpuCores: Int,
    val totalMemoryMB: Long,
    val sdkVersion: Int,
    val manufacturer: String,
    val model: String
) {
    val isVRReady: Boolean
        get() = hasGyroscope && hasAccelerometer && gpuLevel != GPULevel.LOW
    
    val recommendedQuality: QualityPreset
        get() = when {
            gpuLevel == GPULevel.HIGH -> QualityPresets.HIGH
            gpuLevel == GPULevel.MEDIUM -> QualityPresets.MEDIUM
            else -> QualityPresets.LOW
        }
}
```

---

## 12. Testing Strategy

### 12.1 Test Pyramid

```
                    ┌─────────────┐
                    │     E2E     │  10%
                    │  Device Farm│
                    ├─────────────┤
                    │ Integration │  20%
                    │   Tests     │
                    ├─────────────┤
                    │    Unit     │  70%
                    │   Tests     │
                    └─────────────┘
```

### 12.2 Unit Tests

```kotlin
// HeadTrackingPipelineTest.kt
package com.xrvista.cardboard.test

import org.junit.Test
import org.junit.Assert.*

class HeadTrackingPipelineTest {
    
    @Test
    fun `gyroscope integration produces correct orientation delta`() {
        val pipeline = HeadTrackingPipeline(mockSensorManager())
        
        // Simulate gyroscope rotation around Y axis
        val gyroEvent = createSensorEvent(
            Sensor.TYPE_GYROSCOPE,
            floatArrayOf(0f, 1.0f, 0f),  // 1 rad/s around Y
            timestamp = System.nanoTime()
        )
        pipeline.processSensorEvent(gyroEvent)
        
        val orientation = pipeline.update()
        
        // After 1 second, should rotate ~57 degrees around Y
        val euler = orientation.toEulerAngles()
        assertEquals(57.0, Math.toDegrees(euler.yaw.toDouble()), 5.0)
    }
    
    @Test
    fun `complementary filter fuses accelerometer and gyroscope`() {
        val pipeline = HeadTrackingPipeline(mockSensorManager())
        
        // Simulate static acceleration (gravity)
        val accelEvent = createSensorEvent(
            Sensor.TYPE_ACCELEROMETER,
            floatArrayOf(0f, 9.81f, 0f),
            timestamp = System.nanoTime()
        )
        pipeline.processSensorEvent(accelEvent)
        
        val orientation = pipeline.update()
        
        // Should converge toward gravity direction
        assertNotNull(orientation)
        assertTrue(orientation.length() > 0.99)
    }
    
    @Test
    fun `drift correction reduces error over time`() {
        val pipeline = HeadTrackingPipeline(mockSensorManager())
        val driftCorrector = DriftCorrector()
        
        // Set reference orientation
        driftCorrector.setReference(Quaternion.IDENTITY)
        
        // Simulate drift
        val driftedOrientation = Quaternion.fromEulerAngles(0f, 0.1f, 0f)
        
        // Apply correction
        repeat(100) {
            driftCorrector.correctDrift(driftedOrientation, 0.01f)
        }
        
        // Error should be reduced
        val error = driftedOrientation.angleTo(Quaternion.IDENTITY)
        assertTrue(error < 0.05f)  // Less than 3 degrees error
    }
}

// DistortionShaderTest.kt
class DistortionShaderTest {
    
    @Test
    fun `barrel distortion moves pixels outward from center`() {
        val shader = DistortionShader()
        
        val center = floatArrayOf(0.5f, 0.5f)
        val testPoint = floatArrayOf(0.6f, 0.5f)
        
        val distorted = shader.applyDistortion(
            testPoint, center, 
            barrelDistortion = 0.34f
        )
        
        // Distorted point should be further from center
        val originalDist = Math.abs(testPoint[0] - center[0])
        val distortedDist = Math.abs(distorted[0] - center[0])
        
        assertTrue(distortedDist > originalDist)
    }
    
    @Test
    fun `chromatic aberration separates RGB channels`() {
        val shader = DistortionShader()
        
        val uv = floatArrayOf(0.5f, 0.5f)
        val aberration = 0.015f
        
        val (redUV, greenUV, blueUV) = shader.separateChannels(uv, aberration)
        
        // Red should be slightly outward
        assertTrue(redUV[0] > greenUV[0])
        // Blue should be slightly inward
        assertTrue(blueUV[0] < greenUV[0])
    }
}

// GazeSelectorTest.kt
class GazeSelectorTest {
    
    @Test
    fun `gaze selection completes after dwell time`() {
        var completed = false
        val selector = GazeSelector(
            onSelectionComplete = { completed = true },
            onSelectionProgress = { }
        )
        
        // Simulate looking at hotspot
        val hotspots = listOf(
            HotspotNode(
                id = "hotspot-1",
                position = Vector3(0f, 0f, -5f),
                boundingSphere = BoundingSphere(Vector3(0f, 0f, -5f), 0.5f)
            )
        )
        
        val headOrientation = Quaternion.IDENTITY
        
        // Simulate 1.5 seconds of looking
        repeat(150) {
            selector.update(headOrientation, hotspots, 1920, 1080)
            Thread.sleep(10)
        }
        
        assertTrue(completed)
    }
    
    @Test
    fun `gaze selection cancels when looking away`() {
        var cancelled = false
        var progress = 0f
        val selector = GazeSelector(
            onSelectionComplete = { },
            onSelectionProgress = { progress = it }
        )
        
        val hotspots = listOf(
            HotspotNode(
                id = "hotspot-1",
                position = Vector3(0f, 0f, -5f),
                boundingSphere = BoundingSphere(Vector3(0f, 0f, -5f), 0.5f)
            )
        )
        
        // Start looking at hotspot
        repeat(50) {
            selector.update(Quaternion.IDENTITY, hotspots, 1920, 1080)
            Thread.sleep(10)
        }
        
        // Look away (empty hotspots)
        selector.update(Quaternion.IDENTITY, emptyList(), 1920, 1080)
        Thread.sleep(400)  // Wait for look-away threshold
        
        // Progress should reset
        assertEquals(0f, progress, 0.01f)
    }
}
```

### 12.3 Integration Tests

```kotlin
// CardboardSessionIntegrationTest.kt
package com.xrvista.cardboard.test.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardboardSessionIntegrationTest {
    
    @Test
    fun `session initializes and enters VR mode`() {
        val activity = launchActivity(VRActivity::class.java)
        val sessionManager = CardboardSessionManager(activity, mockCallback)
        
        val result = sessionManager.initSession(ViewerProfileManager.CARDBOARD_V2)
        
        assertTrue(result.isSuccess)
        
        activity.runOnUiThread {
            sessionManager.enterVRMode()
        }
        
        // Verify VR mode is active
        Thread.sleep(1000)
        assertTrue(sessionManager.isVRActive)
    }
    
    @Test
    fun `scene loads in WebView and receives sensor data`() {
        val webView = configureWebViewForVR()
        
        // Load scene
        webView.loadUrl("https://viewer.xrvista.com/test-scene")
        
        // Wait for page load
        Thread.sleep(3000)
        
        // Inject sensor data
        webView.evaluateJavascript("""
            window.xrvista.setOrientation(0.5, 1.0, 0.0);
        """, null)
        
        // Verify scene rendered
        val result = webView.evaluateJavascript(
            "document.querySelector('a-scene').object3D !== null"
        ) { value ->
            assertEquals("true", value)
        }
    }
    
    @Test
    fun `stereoscopic rendering produces correct side-by-side output`() {
        val renderer = SinglePassStereoRenderer()
        renderer.init()
        
        val leftFBO = FrameBufferObject(960, 1080)
        val rightFBO = FrameBufferObject(960, 1080)
        
        // Render test pattern to each FBO
        renderTestPattern(leftFBO, color = floatArrayOf(1f, 0f, 0f))  // Red
        renderTestPattern(rightFBO, color = floatArrayOf(0f, 0f, 1f))  // Blue
        
        // Compose side-by-side
        renderer.renderStereo(leftFBO, rightFBO, 1920, 1080)
        
        // Read back pixels and verify
        val pixels = readFrameBuffer()
        
        // Left half should be red
        assertEquals(255, pixels[0])   // R
        assertEquals(0, pixels[1])     // G
        assertEquals(0, pixels[2])     // B
        
        // Right half should be blue
        val rightOffset = 1920 * 4
        assertEquals(0, pixels[rightOffset])      // R
        assertEquals(0, pixels[rightOffset + 1])  // G
        assertEquals(255, pixels[rightOffset + 2]) // B
    }
}
```

### 12.4 Device Farm Testing

```yaml
# browserstack-config.yml
devices:
  android:
    - name: "Samsung Galaxy S23"
      os_version: "13.0"
      category: "high"
    - name: "Google Pixel 7"
      os_version: "13.0"
      category: "medium"
    - name: "Samsung Galaxy A52"
      os_version: "12.0"
      category: "low"
    - name: "OnePlus 9"
      os_version: "12.0"
      category: "medium"
  ios:
    - name: "iPhone 14"
      os_version: "16.0"
      category: "high"
    - name: "iPhone 12"
      os_version: "15.0"
      category: "medium"
    - name: "iPhone SE (3rd gen)"
      os_version: "15.0"
      category: "low"

tests:
  - name: "VR Session Init"
    script: tests/vr_session_init.sh
    timeout: 60s
    
  - name: "Head Tracking Latency"
    script: tests/head_tracking_latency.sh
    timeout: 30s
    
  - name: "Frame Rate Stability"
    script: tests/frame_rate_stability.sh
    timeout: 120s
    
  - name: "Gaze Selection Accuracy"
    script: tests/gaze_selection.sh
    timeout: 60s
    
  - name: "Memory Leak Detection"
    script: tests/memory_leak.sh
    timeout: 300s

reporting:
  format: json
  artifacts:
    - screenshots
    - performance_metrics
    - crash_logs
```

### 12.5 Performance Profiling

```kotlin
// PerformanceProfiler.kt
package com.xrvista.cardboard.perf

class PerformanceProfiler {
    
    private val frameMetrics = mutableListOf<FrameMetric>()
    private var isProfiling = false
    
    fun startProfiling() {
        isProfiling = true
        frameMetrics.clear()
    }
    
    fun recordFrame(
        frameTimeMs: Float,
        gpuTimeMs: Float,
        cpuTimeMs: Float,
        memoryUsageMB: Float
    ) {
        if (!isProfiling) return
        
        frameMetrics.add(FrameMetric(
            timestamp = System.nanoTime(),
            frameTimeMs = frameTimeMs,
            gpuTimeMs = gpuTimeMs,
            cpuTimeMs = cpuTimeMs,
            memoryUsageMB = memoryUsageMB
        ))
    }
    
    fun stopProfiling(): ProfilingReport {
        isProfiling = false
        
        val avgFrameTime = frameMetrics.map { it.frameTimeMs }.average()
        val maxFrameTime = frameMetrics.maxOf { it.frameTimeMs }
        val minFrameTime = frameMetrics.minOf { it.frameTimeMs }
        val p95FrameTime = frameMetrics.sortedBy { it.frameTimeMs }
            [frameMetrics.size * 0.95].frameTimeMs
        val avgFps = 1000.0 / avgFrameTime
        val avgMemory = frameMetrics.map { it.memoryUsageMB }.average()
        
        return ProfilingReport(
            totalFrames = frameMetrics.size,
            avgFrameTimeMs = avgFrameTime,
            maxFrameTimeMs = maxFrameTime,
            minFrameTimeMs = minFrameTime,
            p95FrameTimeMs = p95FrameTime,
            avgFps = avgFps,
            avgMemoryMB = avgMemory,
            droppedFrames = frameMetrics.count { it.frameTimeMs > 20f },
            droppedFramePercent = 
                frameMetrics.count { it.frameTimeMs > 20f }.toFloat() / frameMetrics.size * 100
        )
    }
}

data class FrameMetric(
    val timestamp: Long,
    val frameTimeMs: Float,
    val gpuTimeMs: Float,
    val cpuTimeMs: Float,
    val memoryUsageMB: Float
)

data class ProfilingReport(
    val totalFrames: Int,
    val avgFrameTimeMs: Double,
    val maxFrameTimeMs: Float,
    val minFrameTimeMs: Float,
    val p95FrameTimeMs: Float,
    val avgFps: Double,
    val avgMemoryMB: Double,
    val droppedFrames: Int,
    val droppedFramePercent: Float
) {
    fun isAcceptable(): Boolean {
        return avgFps >= 60.0 && droppedFramePercent < 5.0
    }
}
```

---

## 13. API Integration

### 13.1 Scene Loading API

```kotlin
// SceneRepository.kt
package com.xrvista.cardboard.data

class SceneRepository(private val apiService: SceneApiService) {
    
    suspend fun loadScene(sceneId: String, token: String): Result<SceneGraph> {
        return try {
            val response = apiService.getScene(
                sceneId = sceneId,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val sceneData = response.body()!!
                Result.success(parseSceneGraph(sceneData))
            } else {
                Result.failure(Exception("Failed to load scene: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseSceneGraph(data: SceneResponse): SceneGraph {
        return SceneGraph(
            id = data.id,
            name = data.name,
            background360 = data.background360Url,
            hotspots = data.hotspots.map { hotspot ->
                HotspotNode(
                    id = hotspot.id,
                    type = HotspotType.valueOf(hotspot.type),
                    position = Vector3(
                        hotspot.position.x,
                        hotspot.position.y,
                        hotspot.position.z
                    ),
                    title = hotspot.title,
                    description = hotspot.description,
                    targetSceneId = hotspot.targetSceneId,
                    mediaUrl = hotspot.mediaUrl
                )
            },
            models = data.models.map { model ->
                ModelNode(
                    id = model.id,
                    url = model.url,
                    position = Vector3(model.position.x, model.position.y, model.position.z),
                    rotation = Quaternion(model.rotation.x, model.rotation.y, model.rotation.z, model.rotation.w),
                    scale = Vector3(model.scale.x, model.scale.y, model.scale.z)
                )
            }
        )
    }
}

interface SceneApiService {
    @GET("api/v1/scenes/{id}")
    suspend fun getScene(
        @Path("id") sceneId: String,
        @Header("Authorization") authorization: String
    ): Response<SceneResponse>
    
    @GET("api/v1/scenes/{id}/graph")
    suspend fun getSceneGraph(
        @Path("id") sceneId: String,
        @Header("Authorization") authorization: String
    ): Response<SceneGraphResponse>
}
```

### 13.2 Analytics Events

```kotlin
// AnalyticsTracker.kt
package com.xrvista.cardboard.analytics

class AnalyticsTracker(private val apiService: AnalyticsApiService) {
    
    fun trackSessionStart(sceneId: String, deviceId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            apiService.trackEvent(
                AnalyticsEvent(
                    type = "xr.session.start",
                    sceneId = sceneId,
                    deviceId = deviceId,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf(
                        "platform" to "cardboard",
                        "os" to Build.VERSION.RELEASE,
                        "device" to Build.MODEL
                    )
                )
            )
        }
    }
    
    fun trackGaze(sceneId: String, hotspotId: String, durationMs: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            apiService.trackEvent(
                AnalyticsEvent(
                    type = "xr.gaze",
                    sceneId = sceneId,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf(
                        "hotspot_id" to hotspotId,
                        "duration_ms" to durationMs.toString()
                    )
                )
            )
        }
    }
    
    fun trackNavigation(sceneId: String, fromScene: String, toScene: String) {
        CoroutineScope(Dispatchers.IO).launch {
            apiService.trackEvent(
                AnalyticsEvent(
                    type = "xr.navigation",
                    sceneId = sceneId,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf(
                        "from_scene" to fromScene,
                        "to_scene" to toScene
                    )
                )
            )
        }
    }
    
    fun trackPerformance(sceneId: String, metrics: ProfilingReport) {
        CoroutineScope(Dispatchers.IO).launch {
            apiService.trackEvent(
                AnalyticsEvent(
                    type = "xr.performance",
                    sceneId = sceneId,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf(
                        "avg_fps" to metrics.avgFps.toString(),
                        "dropped_frames" to metrics.droppedFrames.toString(),
                        "avg_memory_mb" to metrics.avgMemoryMB.toString()
                    )
                )
            )
        }
    }
}

data class AnalyticsEvent(
    val type: String,
    val sceneId: String,
    val deviceId: String? = null,
    val timestamp: Long,
    val metadata: Map<String, String>
)
```

---

## 14. Security Considerations

### 14.1 Content Protection

| Threat | Mitigation |
|--------|-----------|
| Scene content theft | DRM watermarking, encrypted asset URLs |
| Session hijacking | JWT token with short expiry |
| Man-in-the-middle | TLS 1.3, certificate pinning |
| Reverse engineering | ProGuard/R8 obfuscation |
| Unauthorized access | OAuth 2.0 + tenant isolation |

### 14.2 Data Privacy

- No PII stored on device (except auth token)
- Sensor data not transmitted (processed locally)
- Analytics are anonymized
- GDPR/CCPA compliant data handling
- Automatic session timeout (30 minutes)

---

## 15. Deployment

### 15.1 Build Configuration

```groovy
// build.gradle (Android)
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 26  // Android 8.0
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
            signingConfig signingConfigs.release
        }
        debug {
            debuggable true
            applicationIdSuffix ".debug"
        }
    }
    
    flavorDimensions "distribution"
    productFlavors {
        googlePlay {
            dimension "distribution"
            applicationId "com.xrvista.cardboard"
        }
        enterprise {
            dimension "distribution"
            applicationId "com.xrvista.cardboard.enterprise"
        }
    }
}
```

### 15.2 Distribution Channels

| Channel | Status | Notes |
|---------|--------|-------|
| Google Play Store | Primary | Consumer-facing |
| Enterprise MDM | Secondary | Android Enterprise |
| Apple App Store | Primary | Consumer-facing |
| Enterprise MDM | Secondary | Apple Business Manager |
| Direct APK | Internal | Testing purposes |
| Firebase App Distribution | Beta | Internal testing |

---

## 16. Migration from Native to WebXR

### 16.1 Fallback Strategy

```
┌─────────────────────┐
│  Check WebXR        │
│  Support            │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐     ┌──────────────┐
    │ WebXR        │ YES │ Native VR    │
    │ Supported?   ├────▶│ Not Needed   │
    └──────┬───────┘     └──────────────┘
           │ NO
           ▼
    ┌──────────────┐     ┌──────────────┐
    │ Cardboard    │ YES │ Use Cardboard│
    │ Device?      ├────▶│ SDK Native   │
    └──────┬───────┘     └──────────────┘
           │ NO
           ▼
    ┌──────────────┐
    │ 360 Fallback │
    │ (No VR)      │
    └──────────────┘
```

---

## Appendix A: Glossary

| Term | Definition |
|------|-----------|
| IPD | Interpupillary Distance - distance between eyes |
| FOV | Field of View - viewing angle |
| FBO | Frame Buffer Object - offscreen render target |
| DOF | Degrees of Freedom - tracking axes |
| SLAM | Simultaneous Localization and Mapping |
| HMD | Head-Mounted Display |
| MTP | Motion-to-Photon latency |

## Appendix B: References

1. Google Cardboard SDK Documentation
2. WebXR Device API Specification
3. AFrame Documentation
4. ThreeJS Documentation
5. Android Sensor Framework
6. iOS Core Motion Framework

---

*Document maintained by XR Platform Team*
*Last updated: 2026-06-05*
