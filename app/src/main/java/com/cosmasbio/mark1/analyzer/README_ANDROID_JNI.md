# Android JNI integration for ivd_strip_analyzer

## What is included
- C++ analyzer core copied under `app/src/main/cpp/android_jni`
- JNI bridge: `native_bridge.cpp`
- Kotlin wrapper: `NativeIvdAnalyzer.kt`
- Simple repository + ViewModel examples

## Required project changes

### 1) app/build.gradle.kts
Add externalNativeBuild + NDK support. Example:

```kotlin
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += listOf(
                    // Change this to your real OpenCV Android SDK jni directory
                    "-DOpenCV_DIR=${'$'}projectDir/src/main/cpp/OpenCV-android-sdk/sdk/native/jni"
                )
            }
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}
```

### 2) OpenCV Android SDK
This code expects OpenCV C++ SDK for Android.
Place it in a stable path and point `OpenCV_DIR` to:

`sdk/native/jni`

### 3) How to call from your app
```kotlin
val vm = IvdAnalysisViewModel()
vm.analyze(
    imagePath = capturedImagePath,
    outputDir = context.filesDir.resolve("ivd_reports").absolutePath,
    manualRoi = null,
)
```

### 4) Native output
The native analyzer returns JSON string from C++ and Kotlin converts it into `JSONObject`.

## Important
- I did not validate build in this environment because Android NDK + OpenCV Android SDK are not installed here.
- You may need to remove the CLI `main.cpp`; it is not included in CMake for Android.
- If your app uses AGP 8+, keep NDK/CMake versions aligned with your local Android Studio.
