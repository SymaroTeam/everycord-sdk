# Facecam and captions

The host's camera as a bubble over the stream, and automatic captions drawn into the video. Both are drawn on the phone
before the video is encoded, so every destination shows them.

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Facecam

Feature `capture`, Live tier. Your app holds the camera permission (`NSCameraUsageDescription` on iOS, `CAMERA` on
Android).

| | iOS | Android |
|---|---|---|
| Shapes | `.circle`, `.roundedRectangle` | `FacecamShape.CIRCLE`, `FacecamShape.ROUNDED_RECTANGLE` |
| Corners | `.topLeading`, `.topTrailing`, `.bottomLeading`, `.bottomTrailing` | `Corner.TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT` |
| Default | circle, bottom leading | circle, bottom left |

Turn it on when the stream starts, then move or reshape it while live:

```swift
let session = try await EveryCord.live.start(LiveOptions(
    title: "Reacting to your clips",
    facecam: FacecamOptions(shape: .circle, corner: .bottomTrailing)
))
session.setFacecam(shape: .roundedRectangle, corner: .topTrailing)
```

```kotlin
val session = EveryCord.live.start(LiveOptions(title = "Reacting to your clips", facecam = Facecam()), consent)
session.setFacecam(FacecamShape.ROUNDED_RECTANGLE, Corner.TOP_RIGHT)
session.hideFacecam()
session.setMicrophoneMuted(true)
```

On iPhone, the camera keeps running while the host is in another app through
[Picture in Picture](cohosts.md#picture-in-picture-ios-18-and-later) (iOS 18 and later). When the stream has
[co-hosts](cohosts.md), their bubbles sit beside the host's.

## Captions

Feature `captions`, Engage tier and up. With `captions: true`, the SDK transcribes the speech in the stream and draws
the captions into the video, so viewers can follow along with the sound off.

```swift
let session = try await EveryCord.live.start(LiveOptions(title: "Patch notes", captions: true))
```

```kotlin
val session = EveryCord.live.start(LiveOptions(title = "Patch notes", captions = true), consent)
```

- Speech is recognized on the device and never leaves it for captioning.
- iOS uses Apple's speech recognition in the device's language, falling back to US English. When the device has no
  on-device model for the language, captions stay off and the stream goes on.
- Android needs Android 13 or later with on-device speech recognition.

## Facecam on recordings

EveryCord's app also adds a facecam to a saved recording: it plays the recording while it films the person, then saves
a new video with the camera in a bubble. A recording facecam helper for partner apps is in early access.
