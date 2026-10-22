# EveryCord SDK sample for Android

A small Compose app that uses the EveryCord Android SDK the way a partner app does: it configures the SDK with a test
publishable key, shows the partner config and tier, signs a user in with a `userToken` from your server, goes live with
`ScreenCaptureContract` and `EveryCord.live.start` (a destination, co-hosts, facecam, overlays), and shows chat, Sparks
and the player for what the tier includes. `Billing.kt` shows the two calls for the earnings cross-check
(`EveryCord.earnings`).

1. Put your test publishable key (`ecpk_test_...`, issued for the application ID `net.everycord.sample`, or change
   `applicationId` in `app/build.gradle.kts` to your own app's) in `gradle.properties` as `everycord.publishableKey`.
   Never put a secret key (`ecsk_...`) in an app.
2. The SDK comes from GitHub Packages: add `gpr.user` and `gpr.key` (a token with `read:packages`) to
   `~/.gradle/gradle.properties`, as in [ANDROID.md](../../ANDROID.md). Or unpack the release's AAR and POM into a Maven
   folder and pass it with `-Peverycord.sdkRepo=/path/to/maven`.
3. Build and install with JDK 17 or later and the Android SDK (API 37):

```bash
./gradlew :app:installDebug
```

Sign-in needs a `userToken`: your server creates it with `POST /v1/partner/users/:externalUserId/token` and your secret
key, and passes it to the app (paste one into the sample to try).
