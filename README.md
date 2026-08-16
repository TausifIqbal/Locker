# AppLocker Lite

AppLocker Lite is a Kotlin Android application that lets users lock installed apps with a 4-digit PIN. It uses an Accessibility Service to detect the foreground app and presents a full-screen PIN gate before access is allowed.

## Project tech stack

- Kotlin + Android Gradle Plugin
- Android Studio (latest stable recommended)
- Material 3 views + ViewBinding
- `RecyclerView`, `TabLayout`, and `ViewPager2`
- `SharedPreferences` for the PIN hash and locked package names
- SHA-256 hashing for PIN storage

## Minimum local setup

### 1) Install Android Studio
Use the latest stable Android Studio release. The project is configured as a standard Android application module and opens directly in Android Studio.

### 2) Use JDK 17 for Gradle
This project targets Java 17 for both source and bytecode compatibility.

If Android Studio is using a newer runtime for Gradle, switch it to JDK 17:

1. Open **File > Settings** (or **Android Studio > Settings** on macOS).
2. Go to **Build, Execution, Deployment > Build Tools > Gradle**.
3. Set **Gradle JDK** to **JDK 17**.

You can also build from the terminal with:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew :app:assembleDebug
```

### 3) Install the Android SDK components
Open **SDK Manager** in Android Studio and make sure these are installed:

- Android SDK Platform 35
- Android SDK Build-Tools (latest available)
- Android SDK Platform-Tools
- Android SDK Command-line Tools

### 4) Ensure `local.properties` points at your SDK
Android Studio usually creates this automatically. If it does not, create a `local.properties` file in the repo root:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Examples:

```properties
# macOS
sdk.dir=/Users/your-user/Library/Android/sdk

# Linux
sdk.dir=/home/your-user/Android/Sdk

# Windows
sdk.dir=C:\\Users\\your-user\\AppData\\Local\\Android\\Sdk
```

> `local.properties` is intentionally ignored by Git.

## Open and run in Android Studio

1. Open this repository in Android Studio.
2. Allow Gradle sync to complete.
3. Create or choose an Android 10+ emulator/device.
4. Click **Run**.

## Build from terminal

After JDK 17 and the Android SDK are configured:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew :app:assembleDebug
```

To inspect Gradle itself without compiling the Android app:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew help
```

## First-run app setup

After the app launches:

1. Create a 4-digit PIN.
2. Open the Accessibility settings from the main screen.
3. Enable **AppLocker Lite Accessibility**.
4. Return to the app and lock/unlock installed apps from the tabs.

## Manual permissions required

### Accessibility Service
Users must enable the Accessibility Service manually. This is required so the app can monitor foreground app changes and trigger the lock screen.

### Overlay permission
No overlay permission is required in the current implementation because the app uses a full-screen `Activity` for the lock screen instead of drawing over other apps.

## Troubleshooting

### `25.0.1` or another Java version parsing/build error
Gradle is likely running with an unsupported JDK for this project. Switch the Gradle runtime to JDK 17 and try again.

### `SDK location not found`
Create/update `local.properties` with `sdk.dir=...`, or configure the Android SDK in Android Studio so the file is generated automatically.

### Accessibility service seems inactive
Make sure the service is enabled in system settings. Some OEM Android builds also aggressively stop background services, so test on a standard emulator or a stock Android device first.
