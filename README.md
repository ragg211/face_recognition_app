# Face Recognition App

An Android application that performs real-time face detection using the device camera. The app uses Google's ML Kit to detect faces and draws green bounding boxes around them as you point the camera at people.

## Features

- **Real-time face detection** powered by Google ML Kit
- **Live camera preview** using AndroidX CameraX
- **Visual overlays** — green bounding boxes drawn around each detected face
- **Multi-face support** — detects and highlights multiple faces simultaneously
- **On-device processing** — no internet connection required
- **Runtime permission handling** for camera access

## Screenshots

> Point the camera at a person and the app will immediately highlight detected faces with green rectangles overlaid on the live preview.

## Tech Stack

| Component | Library / Version |
|-----------|-------------------|
| Language | Kotlin |
| Camera | AndroidX CameraX 1.3.0-rc01 |
| Face Detection | Google ML Kit 16.1.5 |
| UI | AndroidX + Material Design 3 |
| Build System | Gradle with Kotlin DSL |

## Requirements

- Android Studio (latest stable release)
- Android device or emulator running **Android 7.0 (API 24)** or higher
- A working front or back camera

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ragg211/face_recognition_app.git
cd face_recognition_app
```

### 2. Open in Android Studio

1. Launch Android Studio.
2. Select **Open an Existing Project** and navigate to the `face_recognition_app` folder.
3. Wait for the Gradle sync to finish (this may take a few minutes the first time).

### 3. Run on a device

**Physical device (recommended):**
1. Enable **Developer Options** on your Android device (tap *Build Number* seven times in *Settings → About Phone*).
2. Enable **USB Debugging** in *Settings → Developer Options*.
3. Connect the device via USB and authorize the connection when prompted.
4. Press **Run** (Shift+F10) in Android Studio, or use the Gradle CLI:

```bash
./gradlew installDebug
```

**Emulator:**
1. Create an AVD with API 24+ and a virtual camera in the AVD Manager.
2. Start the emulator, then press **Run** in Android Studio.

### 4. Grant camera permission

The app will request camera permission on first launch. Tap **Allow** to enable the live camera feed and face detection.

## Project Structure

```
face_recognition_app/
├── app/
│   └── src/main/
│       ├── java/com/example/camerax/
│       │   ├── MainActivity.kt        # Camera setup, ML Kit integration
│       │   └── FaceBoxOverlay.kt      # Custom view that draws face bounding boxes
│       ├── res/
│       │   ├── layout/activity_main.xml   # Camera preview + overlay layout
│       │   └── values/                    # Strings, colors, themes
│       └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

### Key files

- **`MainActivity.kt`** — Initializes CameraX, configures the ML Kit face detector in fast-performance mode, and pipes each camera frame through the detector.
- **`FaceBoxOverlay.kt`** — A custom `View` that receives the list of detected faces and scales their bounding boxes to screen coordinates before drawing them as green rectangles on a canvas.

## Gradle Commands

```bash
./gradlew assembleDebug          # Build a debug APK
./gradlew assembleRelease        # Build a release APK
./gradlew installDebug           # Build and install on a connected device
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on a device
./gradlew clean                  # Remove build outputs
```

## Permissions

| Permission | Reason |
|------------|--------|
| `android.permission.CAMERA` | Required to access the device camera for face detection |

## How It Works

1. On launch, `MainActivity` requests the `CAMERA` permission.
2. Once granted, CameraX binds a `Preview` (live viewfinder) and an `ImageAnalysis` use-case to the activity lifecycle.
3. For every camera frame, an `InputImage` is created and passed to the ML Kit `FaceDetector`.
4. The list of detected `Face` objects (each containing a bounding box) is forwarded to `FaceBoxOverlay`.
5. `FaceBoxOverlay` scales the bounding-box coordinates from camera-frame space to screen space and redraws itself, producing the green rectangles seen on screen.

## License

This project is provided as-is for educational and demonstration purposes.
