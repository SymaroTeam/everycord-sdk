# Quickstart for Android

Install the EveryCord SDK with Gradle, sign a user in, and go live with Android's screen capture.

> **Early access.** Release builds of the SDK go to early-access partners first. Code samples are illustrative until
> the 1.0.0 release and follow the SDK's current public names. Write to
> [services@symaro.com](mailto:services@symaro.com) for access and your keys.

## Requirements

- Android 10 (API 29) or later; compile with API 37 (the SDK's AndroidX libraries need it) and Java 17.
- Kotlin with coroutines (the SDK's API uses `suspend` functions and `StateFlow`). Java callers can use the
  `@JvmStatic` entry points.
- A publishable key for your application ID (`ecpk_test_...` while you integrate), from the
  [partner dashboard](dashboard.md).

The SDK is an AAR, `net.everycord:sdk`, compiled and shrunk with R8. It brings its own consumer R8 rules, so your
release build needs no extra keep rules.

## Install with Gradle

The SDK is published to GitHub Packages. GitHub Packages asks for a sign-in even for public packages, so use a GitHub
personal access token (classic) with the `read:packages` scope, kept out of source control (for example in
`~/.gradle/gradle.properties`):

```
gpr.user=your-github-username
gpr.key=ghp_your_token_with_read_packages
```

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // RootEncoder, which the SDK uses to send RTMP, is published on JitPack.
        maven("https://jitpack.io")
        maven("https://maven.pkg.github.com/SymaroTeam/everycord-sdk") {
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("net.everycord:sdk:1.0.0")
}
```

**Without GitHub Packages.** Each GitHub release also carries the AAR and its POM. You can put both in a local Maven
folder in your project (`maven { url = uri("libs/maven") }`) at the path `net/everycord/sdk/1.0.0/`, so Gradle still
reads the SDK's dependencies from the POM.

## Permissions

The SDK's manifest declares what its own service needs: `INTERNET`, `ACCESS_NETWORK_STATE`, `CHANGE_NETWORK_STATE`
(bonding), `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `FOREGROUND_SERVICE_MICROPHONE`,
`FOREGROUND_SERVICE_CAMERA`, `POST_NOTIFICATIONS` and `WAKE_LOCK`, and the `EveryCordCaptureService` foreground service.

Your app declares and asks for `RECORD_AUDIO` (microphone) and `CAMERA` (facecam and co-hosts) when it uses them, and
asks for `POST_NOTIFICATIONS` on Android 13 and later so the stream's ongoing notification shows.

## Configure the SDK

Call `configure` once, in `Application.onCreate`. It loads your partner configuration in the background.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EveryCord.configure(this, "ecpk_test_...")
    }
}
```

`EveryCord.config` (and `EveryCord.configUpdates`, a `StateFlow`) holds your tier, features, branding and limits. Hide
what your tier does not include:

```kotlin
if (EveryCord.isEnabled(Feature.COHOSTS)) {
    // Show the co-host button.
}
```

## Sign a user in

Your backend links your user to an EveryCord account and mints a single-use token
([Accounts and sign-in](accounts.md)). Pass it to the SDK:

```kotlin
lifecycleScope.launch {
    val token = myBackend.everyCordUserToken()   // from POST /v1/partner/users/:externalUserId/token
    val user = EveryCord.auth.signIn(token)
    Log.i("App", "Signed in as ${user.username}")
}
```

## Go live

Android asks the person every time before an app captures the screen. Register the SDK's contract, then start the
stream with the consent it returns:

```kotlin
class StreamActivity : ComponentActivity() {
    private val options = LiveOptions(
        title = "Ranked grind",
        orientation = Orientation.PORTRAIT,
        destinations = listOf(Destination.Rtmp("rtmps://a.rtmps.youtube.com/live2", youTubeKey, name = "YouTube")),
        facecam = Facecam(FacecamShape.CIRCLE, Corner.BOTTOM_RIGHT),
    )

    private val capture = registerForActivityResult(ScreenCaptureContract()) { consent ->
        if (consent != null) {
            lifecycleScope.launch {
                val session = EveryCord.live.start(options, consent)
                observe(session)
            }
        }
    }

    fun goLive() {
        val problems = EveryCord.live.problems(options)
        if (problems.isEmpty()) capture.launch(Unit) else showProblems(problems)
    }
}
```

`EveryCord.live.start` starts an EveryCord live session for the signed-in user, then starts the SDK's
`EveryCordCaptureService` (a foreground service of type `mediaProjection`, with a Stop action in its notification). Your
app never starts the service itself.

```kotlin
fun observe(session: LiveSession) {
    lifecycleScope.launch { session.state.collect { state -> statusText.text = state.name } }
    lifecycleScope.launch { session.viewerCount.collect { count -> viewersText.text = "$count watching" } }
    lifecycleScope.launch { session.destinations.collect { outputs -> showOutputs(outputs) } }
    lifecycleScope.launch { session.events.collect { event -> if (event.isError) showError(event.message) } }
}

// Later:
lifecycleScope.launch { session.stop() }
```

`LiveState` is `IDLE`, `CONNECTING`, `LIVE`, `RECONNECTING` or `ENDED`. `session.bitrate` is the video bitrate in kbps.

## Watch and chat

```kotlin
val player = EveryCordPlayerView(context)
player.play(sessionId)          // a live session, or its replay once it ended

val chat = EveryCord.chat(sessionId)
chat.connect()
lifecycleScope.launch { chat.messages.collect { messages -> adapter.submitList(messages) } }
chat.send("GG")
// When done:
chat.close()
```

In Jetpack Compose, wrap `EveryCordPlayerView` in `AndroidView`. It shows "Powered by EveryCord" when your tier's
branding requires it, and releases itself when it leaves the window.

## Errors

The SDK throws `EveryCordException` with the HTTP `status`, the API's `code` and a `message` to show. For
`partner_tier`, `feature` is the feature key and `tier` the tier that includes it. `invalid_options` is the SDK's own
code: the message says what to change. The other codes are in the [Partner API reference](api-reference.md#errors).

`configure` throws `IllegalArgumentException` for anything that is not a publishable key: a secret key (`ecsk_`)
never belongs in an app.

## Next

- [Live streaming](live-streaming.md): destinations, bonding and the EveryCord Relay.
- [Co-hosts and Picture in Picture](cohosts.md)
- [Sparks, video requests and Boosts](sparks.md)
