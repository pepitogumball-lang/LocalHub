# LocalHub

LocalHub is an advanced Android local server and launcher for HTML/CSS/JS projects. It allows you to serve files directly from your device's storage using a local HTTP server.

## Features
- **Local HTTP Server**: Powered by NanoHTTPD, serving files on `localhost:8080`.
- **SAF Integration**: Select any folder on your device using the Storage Access Framework.
- **Persistent Permissions**: Remember the selected folder even after app restarts.
- **WebView Browser**: Built-in browser to view your local projects.
- **Offline Ready**: Works perfectly without an internet connection.
- **MIME Type Support**: Handles CSS, JS, Images, Video, WASM, and more.
- **Foreground Service**: The server keeps running even when the app is in the background.

## How to Install
1. Clone this repository.
2. Open the project in Android Studio.
3. Build the APK using `./gradlew assembleDebug`.
4. Install the `app-debug.apk` on your Android device.

## How to Use
1. Open LocalHub.
2. Grant necessary permissions (Notifications, Battery Optimization).
3. Tap **Folder** to select the root directory of your web project.
4. Tap **Start** to launch the server.
5. Tap **Browse** to open the integrated WebView and see your site.

## Configuration
- **Port**: Default is 8080. You can change this in `AppSettings.kt`.
- **Root File**: The server looks for `index.html` by default when accessing `/`.

## Development
This project uses:
- **Kotlin** for logic.
- **NanoHTTPD** for the HTTP server.
- **ViewBinding** for UI interaction.
- **GitHub Actions** for CI/CD.

## GitHub Actions
The included workflow (`.github/workflows/android.yml`) automatically builds the debug APK on every push to `main`. You can download the APK from the "Actions" tab in your GitHub repository.
