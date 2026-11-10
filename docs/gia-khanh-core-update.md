# Gia-Khanh core update — 2026-11-10

This merge preserves the existing Gia-Khanh branch and incorporates main. It does not merge the PR into main. No new remote branch is needed.

## Scope and compatibility

- Keep Nam Khánh's Main/Category/Detail/Search and Tường Long's Auth/Market/Profile source and layouts exactly as on main.
- Base integration only: MainActivity/DrawerLayout, shared GestureHelper entry points, resources/navigation aliases, repositories, Room, API contracts and backend.
- Keep the existing compile/target SDK 37 and dependency versions; downgrading the toolchain to the old specification is not necessary for this merge and could break the other modules.
- Keep existing navigation argument/action names used by the other modules, while adding the standard action names. Do not rename their categoryId or login/register screens.
- AppDatabase is a source-compatible alias for NewsDatabase, preserving the existing Room schema history. New installations use news_app_local.db; existing news.db installations retain their path and migrate in place.

## Implemented

- Saved articles read locally before accessing the remote API, including after process restart. Main-feed cached article content is also available on network failure; cancellation still propagates.
- Room version 3: saved_articles uses specification column names; tracked_charts stores price metadata and separate saved/top flags. Migration preserves saved content and favourite ordering. Legacy favourite_chart remains for schema compatibility.
- NewsLocalDao provides the specified reads/writes; promotion clears previous top status and promotes exactly one chart atomically. Price refresh preserves local preferences. SeedDatabaseCallback initializes chart identities with zero/unfetched values, not fabricated live prices.
- Existing Market/Profile repository interface remains unchanged. Their mock data sources are intentionally not replaced by this core change.
- Singleton database construction, encrypted session preferences with one-time migration, backup exclusions for device-bound encrypted preferences.
- Drawer host with a private navigation stack: existing Market toolbar/back/swipe behavior can close it without editing MarketDrawerFragment.
- Standard base colors/dimens/theme alias and GestureHelper entry points.
- Retrofit contracts for six GET endpoints and authenticated users/sync, available for Long's later integration.
- Backend featured-only main grids; all four requested RSS publishers; summary minimum lengths checked before accepting AI output.
- Gemini 3.8 Flash default and environment override, plus matching .env example and Render blueprint.
- Synthetic market updates disabled by default. This does not convert existing seed prices into live quotes.

## Deployment and release prerequisites

Render: use the repository-root render.yaml, whose rootDir is newsapi-backend. Supply MONGODB_URI, GEMINI_API_KEY, ADMIN_KEY, and Firebase Admin credentials securely. If the existing Render service explicitly sets GEMINI_MODEL, set it to gemini-3.8-flash and restart; code defaults cannot override an explicit environment value.

Android release signing reads ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD from the environment. Do not commit signing material. Use the project's existing release identity; no replacement keystore is generated automatically.

Build with `gradlew :app:bundleRelease -PnewsApiBaseUrl=https://YOUR-SERVICE.onrender.com/api/v1/` after supplying signing variables. The debug default points at the emulator host, not a deployed service. Verify the release artifact's signature before distribution.

Not certified by this change: real Firebase login/UI integration, live RON95/E5/SJC price providers, live cloud credentials/deployment, leak profiling on device, or a production signed release. These require the relevant integration or operator-owned configuration; no credentials or fake production endpoints are added.

## Verification commands

- Backend: `cd newsapi-backend && npm ci && npm test`
- Android: `gradlew :app:assembleDebug :app:testDebugUnitTest`
- Emulator/device: `gradlew :app:connectedDebugAndroidTest`
- Scope: compare ui/auth, ui/market, ui/profile, ui/main, ui/category, ui/detail, ui/search and their layouts against the merged main commit; they must remain unchanged.

Reference: https://aistudio.google.com/docs/models/gemini-3.8-flash

## Recorded verification

- Debug APK built successfully; 12 Android JVM tests passed.
- Backend: 10 tests passed, including API authorization and new core behavior.
- Instrumentation APK compiled, but connected tests could not run: no connected device; Pixel_10a emulator reported that hardware acceleration driver is not installed.
- Additional host SQLite check executed the actual migration SQL against Room v2 schema and compared resulting columns/types/nullability to exported v3 schema. Saved article content/date and favourite ordering were preserved. This does not replace Room instrumentation tests.
- Auth/Market/Profile/Main/Category/Detail/Search implementation directories have no diff against merged main. Their layouts are unchanged; only activity_main.xml changed in the layout directory.
- npm audit reports two moderate findings through existing node-cron/uuid dependencies. No forced major dependency upgrade was applied in this scoped change.
