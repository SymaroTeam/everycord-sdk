# Co-hosts and Picture in Picture

Up to three friends join a stream on camera from their own phones, and the host talks with them while playing. On
iPhone, the host's camera keeps running in a Picture in Picture window while they use other apps.

Feature `cohosts`, Live tier. `limits.maxCohosts` in your configuration is 3 on the Live tier.

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## How it works

- The host starts the stream with `cohostsEnabled: true`. The SDK joins a WebRTC room (LiveKit) for the session as the
  host.
- Co-hosts join the room from their own phones. Their cameras are drawn into the stream as bubbles beside the host's
  facecam, and everyone in the room hears each other.
- The picture with the co-hosts is what every output shows: EveryCord's delivery, YouTube, Twitch and SRT alike.
- Co-hosts need an EveryCord account. Today they join from the EveryCord app (an invite link opens it, or the App Store
  when it is not installed); joining from your own app is in early access.

## Invite co-hosts

```swift
let session = try await EveryCord.live.start(LiveOptions(title: "Squad night", cohostsEnabled: true,
                                                         facecam: FacecamOptions()))

// A link anyone signed in can use to join while the stream is live (1 to 3 uses). Shown once: keep it.
let invite = try await session.cohosts.inviteLink(maxUses: 2)
share(invite.url)                      // https://everycord.net/j/<token>

// Or invite an EveryCord user by ID: they get an invite to accept.
try await session.cohosts.invite(userID: friendID)

// The list (invited, joined, declined, left, removed), and removal.
try await session.cohosts.refresh()
let joined = session.cohosts.list.filter { $0.status == .joined }
try await session.cohosts.remove(userID: friendID)
```

```kotlin
val session = EveryCord.live.start(LiveOptions(title = "Squad night", cohostsEnabled = true, facecam = Facecam()), consent)
val cohosts = session.cohosts ?: return

val link = cohosts.inviteLink(maxUses = 2)      // shown once: keep it
cohosts.invite(friendId)
lifecycleScope.launch { cohosts.list.collect { list -> showCohosts(list) } }
cohosts.remove(friendId)
```

On Android each `Cohost` also has `hasVideo`, true while their camera is in the room and drawn into the video.

## Move the bubbles

`setFacecam(shape:corner:)` moves or reshapes the host's camera bubble, and the co-hosts' bubbles follow beside it. See
[Facecam and captions](facecam-captions.md).

## Picture in Picture (iOS 18 and later)

While the host plays another app, iOS would normally stop the camera. The SDK runs the host's camera as a video call in
a Picture in Picture window instead, so it keeps running, stays in the stream, and shows the co-hosts.

`session.pictureInPicture` is available when the session has a facecam or co-hosts, on iOS 18 and later:

```swift
if let pip = session.pictureInPicture {
    // Keep the self-view on screen while live; the window opens by itself when the host leaves the app.
    cameraContainer.addSubview(pip.selfView)

    pip.isPossible       // whether the window can open now
    pip.isActive         // whether it is open
    _ = pip.start()      // open it now (only while the app is in front)
    pip.stop()
    pip.switchCamera()   // front and back
}
```

Your app needs the `voip` background mode and the camera and microphone usage descriptions. On iOS 16 and 17 the
stream runs without the window: the facecam pauses while the host is in another app.

**Android.** The SDK's capture service is a foreground service with the camera and microphone types, so the facecam and
the co-host room keep running while the person uses other apps. No Picture in Picture window is needed.
