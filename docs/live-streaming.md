# Live streaming

One call starts a live stream that plays worldwide in your app and on the web, and also goes to up to three RTMP, RTMPS
or SRT destinations. The phone uploads straight to the cloud; nothing needs a server of yours.

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

| Feature key | Tier | What it covers |
|---|---|---|
| `capture` | Live | screen capture and the facecam |
| `live_delivery` | Live | EveryCord's own delivery: viewers in your app (`EveryCordPlayerView`) and on the web |
| `multistream` | Live | RTMP, RTMPS and SRT destinations, up to 3 |
| `bonding` | Engage | SRTLA bonding and the EveryCord Relay |

## Start a stream

`LiveOptions` describes the stream. Every field has a default.

| Option | Default | Meaning | Feature |
|---|---|---|---|
| `title` | none (iOS), empty (Android) | 1 to 100 characters | |
| `orientation` | landscape (iOS), automatic (Android) | 1280 x 720 or 720 x 1280 canvas; the screen is fitted into it | |
| `destinations` | none | up to 3 RTMP, RTMPS, SRT or Relay destinations | `multistream` |
| `bonding` | false | allows `srtla://` destinations and the EveryCord Relay, sent over every network at once | `bonding` |
| `cohostsEnabled` | false | a co-host room; co-hosts' cameras are drawn into the video | `cohosts` |
| `facecam` | none | the host's camera, a shape and a corner | `capture` |
| `overlays` | false | the creator's overlay widgets drawn into the video | `overlays` |
| `captions` | false | automatic captions drawn into the video | `captions` |
| `postToLiveNow` | false | list the stream on EveryCord's Live Now and tell followers | |
| `everycordPlayback` (Android) | true | also send the stream to EveryCord for the player and clips | `live_delivery` |
| `audio` (Android) | app and microphone | `APP`, `MICROPHONE`, `APP_AND_MICROPHONE` or `NONE` | |

The SDK checks the options against your tier and limits before anything is sent (`LiveOptions.validate` on iOS,
`EveryCord.live.problems(options)` on Android) and says what to change in one sentence.

```swift
let session = try await EveryCord.live.start(LiveOptions(
    title: "Friday speedruns",
    orientation: .landscape,
    destinations: [
        .rtmp(name: "Twitch", url: URL(string: "rtmp://live.twitch.tv/app")!, streamKey: twitchKey),
        .srt(name: "Studio", url: "srt://ingest.example.com:9000", streamID: "studio", passphrase: passphrase),
    ],
    captions: true
))
```

```kotlin
val session = EveryCord.live.start(
    LiveOptions(
        title = "Friday speedruns",
        orientation = Orientation.LANDSCAPE,
        destinations = listOf(
            Destination.Rtmp("rtmp://live.twitch.tv/app", twitchKey, name = "Twitch"),
            Destination.Srt("srt://ingest.example.com:9000", streamId = "studio", passphrase = passphrase, name = "Studio"),
        ),
        captions = true,
    ),
    consent,
)
```

On iOS the session is picked up by your broadcast upload extension once the person starts the broadcast
([Quickstart for iOS](quickstart-ios.md)); on Android the SDK's capture service starts at once
([Quickstart for Android](quickstart-android.md)). One session runs at a time per app.

## Session state

| State | Meaning |
|---|---|
| idle | started, waiting for the broadcast extension (iOS) |
| connecting | the outputs are connecting |
| live | at least one output is live |
| reconnecting | an output lost its connection and is trying again |
| ended | the stream is over |

The session also reports `viewerCount` (people watching on EveryCord's delivery, your player and EveryCord's apps),
`bitrate`, and each output's status. Output messages never contain a URL or a stream key.

`addDestination` adds a destination while live (still at most 3 in all). `stop()` ends every output, the capture and
the EveryCord session.

## EveryCord delivery

With `live_delivery`, the stream plays in your app and on the web through EveryCord's delivery:

- The phone cuts 2-second HLS segments and uploads each one with a presigned HTTPS PUT straight to Cloudflare R2. Upload
  URLs are short-lived and scoped to one stream.
- A segment that lands within 2.5 seconds of being cut plays live; a late one is kept for the replay and skipped live,
  so viewers never fall behind a slow connection.
- **Adaptive upload.** When 3 of the last 6 segments miss the deadline, the bitrate halves (never under 800 kbps); after
  10 on-time segments in a row, a quarter of the maximum comes back. The encoder keeps running, so viewers see no
  restart.
- The same segments make the cloud copy, so the replay and [clips](clips.md) are ready when the stream ends.

`limits.maxBitrateKbps` in your configuration is the encoder's ceiling (4,500 kbps on the Live tier).

**Live Now.** Partner streams are kept off EveryCord's Live Now unless `postToLiveNow` is true and the key is a live
key. People who follow the streamer on EveryCord still see the stream in their following list. Test-mode streams are
never listed.

## Destinations

Up to three, in any mix (`limits.maxDestinations`).

| Kind | URL | Notes |
|---|---|---|
| RTMP | `rtmp://host/app` and a stream key | YouTube, Twitch, Facebook, any RTMP server |
| RTMPS | `rtmps://host/app` and a stream key | YouTube, Kick, X, any RTMPS server |
| SRT | `srt://host:port` | stream ID, passphrase (10 to 79 characters) and latency; given values win over the URL's query |
| SRTLA | `srtla://host:port` | an SRTLA receiver (BELABOX cloud and others); sent over every network the phone has; needs `bonding` |
| EveryCord Relay | the relay input's `rk_` key | see below; needs `bonding` |

An SRT URL has a host and a port, no path, and only `streamid`, `latency`, `passphrase` and `pbkeylen` in its query.
Stream keys and passphrases stay on the device and go only to that server.

## Bonding

With `bonding` (Engage tier and up), one stream goes over every connection at once: the phone's cellular plan, Wi-Fi
and Ethernet (USB tethering). A weak connection never drags the strong ones down. On Android, the SDK requests the
cellular, Wi-Fi and Ethernet networks at the same time (`CHANGE_NETWORK_STATE` is in the SDK's manifest).

Bonding applies to `srtla://` destinations and to the EveryCord Relay.

## The EveryCord Relay

A cloud relay owned by the streamer's account. The phone sends one bonded stream to the relay; the relay restreams it to
up to five RTMP or RTMPS platforms and keeps them fed with a standby picture ("Be right back") while the phone
reconnects, so the platforms never see the stream end. No PC is needed.

- A relay has inputs (by default "Phone" and "PC"), each with its own `rk_` key, and up to five destinations.
- It encodes once at the relay's size (1920 x 1080, 1280 x 720 or 854 x 480, landscape or portrait), 30 or 60 fps,
  1,000 to 12,000 kbps.
- When the playing input stops, the last frame stays for 3 seconds, then the standby picture with silence until an
  input sends again; after the relay's standby minutes (10 by default) it stops.

Relays are set up through the `/v1/relays` routes with the user's access token (feature `bonding`). In a stream, add
the relay as a destination:

```swift
.everycordRelay(relayKey: "rk_...")
```

```kotlin
Destination.Relay(relayKey = "rk_...")
```

## Playback in your app

`EveryCordPlayerView` plays a live session through EveryCord's delivery, or its replay once the stream has ended. It
counts the viewer's presence while they watch, and shows "Powered by EveryCord" when your tier's branding requires it.

```swift
EveryCordPlayerView(sessionID: sessionID)
```

```kotlin
playerView.play(sessionId)
```

Viewers on the web open the stream at its `shareURL` (`everycord.net/live/<id>`).

## Test mode

With a test key, sessions work end to end but are never listed on Live Now, and their usage is metered apart and never
billed. Use test mode for development and App Review builds you do not want listed.
