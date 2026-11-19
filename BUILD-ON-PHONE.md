# Build the APK using only an Android phone

This project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.
You do not need Android Studio or a PC. GitHub builds the APK in the cloud.

1. Sign in to GitHub in your phone browser and create a repository (Private is recommended).
2. Extract this ZIP on your phone.
3. Upload the *contents* of the `KuruX_Companion_Android_Studio` folder to the repository root. Make sure `.github/workflows/build-apk.yml` is included. If the GitHub mobile upload page hides dot-folders, use the browser's Desktop site or GitHub's web editor to create `.github/workflows/build-apk.yml` and paste the workflow from this ZIP.
4. Open the repository's **Actions** tab. If prompted, enable Actions.
5. Select **Build KuruX Companion APK** → **Run workflow**.
6. Wait for the run to finish with a green check. Open that run, scroll to **Artifacts**, and download `KuruX-Companion-debug-APK`.
7. Extract the downloaded artifact ZIP; install `app-debug.apk`. If Android blocks it, allow **Install unknown apps** for the browser/file manager you used.
8. Open KuruX Companion and grant **Display over other apps**, then start the floating panel.

Notes:
- Keep the GitHub repository Private if you do not want to publish your source publicly.
- This creates a debug APK for testing, not a Play Store release.
- The panel's profile switches currently save local preferences only; they do not independently change CPU/GPU clocks, clear other apps' RAM, modify touch drivers, or unlock FPS.
