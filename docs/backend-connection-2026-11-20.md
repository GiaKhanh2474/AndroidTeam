# Backend connection verification — 2026-11-20

## Confirmed cause and fix

The Pixel_10a emulator runs Android 17 (API 37). The app targets API 37.
The installed app timed out connecting to `http://10.0.2.2:5000/api/v1/news/main-grids` after 10 seconds. AppOps reported `ACCESS_LOCAL_NETWORK: ignore` with rejected accesses. Backend availability alone was insufficient.

The debug manifest now declares `android.permission.ACCESS_LOCAL_NETWORK`. MainActivity requests it on API 37+ before starting screens when the debug backend URL is local. Public backend URLs do not trigger this prompt. Denial still opens the app for offline use. Release manifest is unchanged.

Official documentation: https://developer.android.com/privacy-and-security/local-network-permission

## Actual verification

- Started the configured Node backend; MongoDB connected.
- Health, news grids, categories, search, sideline, market and article detail requests returned HTTP 200.
- Reproduced the connection timeout in the installed Android app.
- Installed the fixed debug APK, accepted the permission dialog, and observed the app request return HTTP 200 in 677 ms. The home screen displayed nine article summaries across three sections.
- Debug build and Android test APK compilation succeeded.
- JVM tests: 14/14 passed.
- Emulator instrumentation: 5/6 passed. `CoreMigrationTest.upgradePreservesSavedContentAndChartOrder` failed with `AbstractMethodError` for `GeneratedSerializer.typeParametersSerializers()` on Room's `FieldBundle$$serializer`. This requires separate test dependency compatibility investigation; migration correctness is not confirmed by this run.
- The instrumentation runner removed the app after testing. The debug APK was reinstalled. Local emulator app data may have been reset by this test cycle.

## Running locally

1. In `newsapi-backend`, run `npm start` and keep the terminal open. Check `http://localhost:5000/health`.
2. Open this project in Android Studio, select Debug and run on the emulator.
3. Allow the nearby-device/local-network permission when prompted on Android 17+.
4. Debug API base URL is `http://10.0.2.2:5000/api/v1/`. This address maps to the development computer from the Android emulator; it is not a physical-phone URL.

## Remaining configuration issue

The configured Firebase Admin credential file was missing. The backend's untracked `google-services.json` is Android client configuration, not a server service-account credential. Firebase-dependent operations remain unverified. No credential files or keys are committed by this fix.

The backend was not running at the start of this inspection. A running backend and the local-network permission are both needed. This report does not establish full production readiness or verification of every feature.
