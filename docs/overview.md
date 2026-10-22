# How it works

The EveryCord SDK is the technology behind EveryCord, the free screen recorder and live streaming app on the App Store
since 2018, offered to other apps. You ship the features in your app; EveryCord runs the servers behind them.

> **Early access.** The SDKs and the Partner API are in early access. Code samples are illustrative until the 1.0.0
> release. Write to [services@symaro.com](mailto:services@symaro.com) to join.

## What you get

| Piece | What it is | Where it runs |
|---|---|---|
| iOS SDK | A compiled XCFramework for iOS 16 or later, installed with Swift Package Manager or CocoaPods | Your iOS app and its broadcast upload extension |
| Android SDK | A compiled AAR for Android 10 (API 29) or later, installed with Gradle | Your Android app |
| Partner API | HTTPS routes under `https://everycord.net/v1/partner/` | Your backend, with your secret key |
| Webhooks | Signed HTTPS calls when a stream starts or ends, a clip is made, Sparks arrive or a user is restricted | Your backend |
| Partner dashboard | Sign-up, apps, keys, store connections, earnings, usage and webhooks at `everycord.net/partners` | Your browser |

macOS and visionOS builds of the iOS SDK follow the iOS release; see [Mac and Apple Vision Pro](mac-vision-pro.md).

## Keys

Every partner app gets two kinds of key, in a live and a test version:

| Key | Looks like | Lives in | Can do |
|---|---|---|---|
| Publishable key | `ecpk_live_` or `ecpk_test_` and 32 letters and digits | Your app | Read your configuration and start sign-in, only from the app (bundle ID or application ID) it was issued for |
| Secret key | `ecsk_live_` or `ecsk_test_` and 40 letters and digits | Your backend only | Create and delete partner users, mint sign-in tokens, read usage and live sessions, manage webhooks, report revenue |

A secret key never goes in an app. It is shown once when you create it; EveryCord keeps only a hash of it.

**Test mode.** Test keys work end to end, but test live sessions are never listed on EveryCord's Live Now, Sparks are
test Sparks (never money), and usage is metered apart and never billed. Test keys never see live-mode data.

## Partner users

Your users get EveryCord accounts. Your backend links each one to your own user ID (`externalUserId`) with the secret
key, then gives your app a short-lived sign-in token; the app hands it to the SDK. People can also sign in with Apple,
Google or an email code. Profiles, followers, blocking, suspensions and bans come with the accounts, so chat, direct
messages and Sparks work from day one. See [Accounts and sign-in](accounts.md).

## How a live stream travels

1. Your app calls `EveryCord.live.start(...)`. The SDK asks the API for a live session and upload URLs.
2. On iOS, your broadcast upload extension (a subclass of `EveryCordBroadcastHandler`) captures the screen, app audio
   and microphone with ReplayKit. On Android, the SDK's `EveryCordCaptureService` captures with MediaProjection.
3. The hardware encoder makes H.264 video and AAC audio. Facecam, co-hosts, overlays, captions and the EveryCord mark
   (when your tier draws it) are drawn into the picture before it is encoded, so every destination shows the same
   picture.
4. The SDK cuts the stream into 2-second HLS segments and uploads each one with a presigned HTTPS PUT straight to
   Cloudflare R2. No media server sits in the live path; the API only starts sessions, issues upload URLs and records
   each segment.
5. Viewers anywhere play the stream through Cloudflare's network, in your app (`EveryCordPlayerView`) and on the web.
6. At the same time, the SDK can send the stream to up to three RTMP, RTMPS or SRT destinations (YouTube, Twitch, Kick,
   your own server), and co-hosts join on camera through a WebRTC room.

**One upload, any audience size.** Segments are written once and served by Cloudflare, so a bigger audience adds no
load on the streamer's phone.

**A live deadline.** A segment must land within 2.5 seconds of being cut to play live. A late segment is kept for the
replay and skipped live, so viewers never fall behind a slow connection.

**Adaptive upload.** The bitrate follows the streamer's connection with no encoder restart: when 3 of the last 6
segments miss the deadline, the bitrate halves (and goes down to what the late uploads actually carried), never under
800 kbps. After 10 on-time segments in a row, a quarter of the maximum comes back, one step at a time.

**Recording and live together.** The same segments make the cloud copy, so the replay and clips are ready as soon as
the stream ends. See [Clips and replays](clips.md).

## Tiers

What your app may use depends on your tier: Extras (10%), Engage (20%) or Live (30%), cumulative. The SDK reads your
tier from `GET /v1/partner/config` and hides what it does not include; the server refuses it anyway with
`403 partner_tier`. See [Tiers, feature gates and branding](tiers-branding.md).

## Requests the SDK sends

Every SDK request carries these headers, so EveryCord can attribute usage, live sessions and Sparks to your app:

```
EveryCord-Partner-Key: ecpk_live_...
EveryCord-App-Id: com.example.app
EveryCord-SDK: ios/1.0.0
Authorization: Bearer <accessToken>
```

`Authorization` is sent once a partner user is signed in. The SDK reads the bundle ID (iOS) or application ID (Android)
from your app; a key used from another app is refused with `403 partner_app`.

## Next

- [Quickstart for iOS](quickstart-ios.md)
- [Quickstart for Android](quickstart-android.md)
- [Partner API reference](api-reference.md)
