# ResearchAndDevelopment - Android R&D Project

## Project Overview

An Android R&D playground for exploring advanced technologies including AR, ML, camera processing, real-time communication, and location-based services.

- **Package:** `com.app.researchanddevelopment`
- **Base code package:** `com.app.research`
- **Min SDK:** 26 | **Target/Compile SDK:** 36
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3) + some legacy XML screens

## Build & Run

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew test

# Lint
./gradlew lint
```

- **Build system:** Gradle Kotlin DSL (KTS)
- **Version catalog:** `gradle/libs.versions.toml`
- **Kotlin:** 2.3.0 | **AGP:** 8.11.2 | **Compose BOM:** 2026.01.00

## Architecture

- **Pattern:** Modular feature packages + MVVM + Clean Architecture (per feature)
- **State management:** Kotlin `StateFlow` / `MutableStateFlow` (no LiveData)
- **DI:** Manual/lazy initialization (no DI framework)
- **Navigation:** Jetpack Navigation Compose (`NavHost`) + some Activity-based navigation
- **Logging:** Timber

## Project Structure

```
app/src/main/java/com/app/research/
├── areatag/              # Zone-based geospatial tagging (H3 indexing, GPS, compass)
│   ├── data/             # Zone, AreaTag models, AreaTagStore (SharedPrefs)
│   ├── location/         # LocationProvider (GPS flow)
│   ├── sensor/           # OrientationManager (compass/gyro)
│   └── ui/               # AreaTagScreen, AROverlay, AreaTagViewModel
├── arsample/             # 360-degree image AR viewer (ARCore + SceneView)
├── artagging/            # AR-based object tagging
├── camoverlaypointsmapping/ # Camera preview with graphic overlay + OpenCV
│   ├── processor/        # Image processing pipeline
│   └── scanner/          # QR/barcode scanning
├── chatpaging/           # Paginated chat UI (Paging 3 + Compose)
├── deeplink/             # Deep link handling
├── faceml/               # Face detection & mesh (ML Kit)
├── good_gps/             # GIS for golf (Google Maps, GPS, polylines)
├── health_connect/       # Health Connect API (heart rate, steps, exercises)
├── slidingTransition/    # Transition animations
├── tensorflow/           # TensorFlow Lite object detection
├── webrtcaudiocalling/   # WebRTC P2P audio calling (Clean Architecture)
│   ├── domain/           # CallRepository, CallState, Use Cases
│   ├── data/             # CallRepositoryImpl
│   ├── presentation/     # CallViewModel, CallScreen
│   ├── signaling/        # SignalingClient (Socket.IO)
│   └── webrtc/           # WebRTCManager
├── ui/theme/             # Compose Material Design 3 theme
├── utils/                # Shared utilities, extensions, permissions
├── MainActivity.kt       # Main Compose host (entry point)
├── MainHostScreen.kt     # Home screen grid launcher
├── ComposeHostActivity.kt # Compose navigation host
└── ResearchApplication.kt # App initialization (H3, Location, Timber)
```

## Key Technologies

| Domain | Libraries |
|--------|-----------|
| Camera | CameraX 1.5.2 |
| ML/Vision | ML Kit Face Detection 16.1.7, TensorFlow Lite 2.17.0 |
| AR | ARCore 1.44.0, ARSceneView 2.2.1 |
| Maps/Location | Google Maps Compose 7.0.0, Play Services Location 21.3.0 |
| Spatial | H3 (Uber) 4.4.0 — hexagonal indexing |
| Media | Media3/ExoPlayer 1.9.0 |
| Networking | Socket.IO (Moko 0.6.0 + Client 2.1.0), Retrofit |
| WebRTC | WebRTC SDK 144.7559.01 |
| Health | Health Connect 1.1.0 |
| Images | Coil 2.7.0 |
| Paging | Paging 3.3.6 |

## Feature Modules (14 screens)

Launched from `MainHostScreen` grid:
TensorFlow, ChatCustomPaging, ForGolfRND, SkaiRAndD, HealthConnect, DeepLink, FaceMl, ScreenSlidTransition, VerticalHorizontalPager, CamPointsMappingOverlay, WebRTCAudioCalling, ARSample360, AreaTag, ArTagging

## Navigation

- **Compose routes** (via `ComposeHostActivity`): ChatCustomPaging, TensorFlow, CamPointsMappingOverlay, WebRTCAudioCalling, ARSample360, AreaTag, ArTagging
- **Activity-based**: FaceML, ForGolf, DeepLink, HealthConnect, SlidingTransition, Pager
- **XML NavGraphs**: `nav_graph.xml` (FaceML), `nav_graph2.xml` (ForGolf)

## Notable Details

- Native JNI libs included for H3: arm64-v8a, armeabi-v7a, x86_64
- `.tflite` files excluded from AAPT compression
- Cleartext traffic allowed (`usesCleartextTraffic="true"`)
- Google Maps API key in AndroidManifest
- ARCore configured as optional
- AreaTag uses H3 resolution 13 for zone-based spatial partitioning
