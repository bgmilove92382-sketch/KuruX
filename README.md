# KuruX Companion — Android Studio project

This is the source project for the KuruX real floating overlay companion.

## Build an installable APK
1. Open this folder in Android Studio (Hedgehog or newer).
2. Let Gradle sync and install Android SDK Platform 35 if prompted.
3. Choose **Build > Build APK(s)**.
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.
5. Install it, open KuruX Companion, grant **Display over other apps**, then start the floating panel.

## Included
- Draggable KX bubble displayed over other apps.
- Tap bubble to open/close a compact panel.
- Adjustable bubble size.
- Free Fire, Free Fire MAX and BGMI launch buttons (only launch if installed).
- Four locally saved profile switches.

## Important limitations
The four switches are currently local profile flags only. An ordinary non-root Android app cannot safely/legitimately force CPU/GPU clocks, clear other apps' RAM, change touch-driver behavior, or unlock FPS. To make those switches alter system behavior, integrate a documented AxManager/Shizuku privileged command bridge and validate each command on the target device. This project deliberately does not claim fake performance changes or force unsupported FPS.
