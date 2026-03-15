# StorePulse Android

Android companion app scaffold for the `clients/android` role in `docs/architecture.md`.

## Scope

- authentication
- dashboard summary
- alerts viewing and acknowledgment
- import monitoring
- CSV upload from device storage

## Notes

- The app is built with Jetpack Compose.
- `BuildConfig.API_BASE_URL` defaults to `http://10.0.2.2:8080`, which is the Android emulator bridge to the host machine.
- If you run the backend with Docker locally, keep the API exposed on port `8080`.

## What is still missing

- file upload from device
- instrumentation and UI tests
- release configuration

## Current state

- auth token is persisted with DataStore and restored on launch
- the app validates the restored session against `/auth/me`
- the UI now uses Compose Navigation for dashboard, alerts, and imports
- the imports screen can pick a CSV from device storage and submit it to `/imports/sales`
- JVM view-model tests cover session restore, upload state, and logout clearing
