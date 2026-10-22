# Quickstart for iOS

Install the EveryCord SDK, sign a user in, and go live from your app's broadcast upload extension.

> **Early access.** Release builds of the SDK go to early-access partners first. Code samples are illustrative until
> the 1.0.0 release and follow the SDK's current public names. Write to
> [services@symaro.com](mailto:services@symaro.com) for access and your keys.

## Requirements

- iOS 16 or later. Picture in Picture with the camera running in the background needs iOS 18.
- Xcode 27 or later (the XCFramework is built with Xcode 27). In the Simulator the SDK runs on Apple silicon Macs
  (arm64 only).
- A publishable key for your app's bundle ID (`ecpk_test_...` while you integrate), from the
  [partner dashboard](dashboard.md).
- An App Group shared by your app and its broadcast upload extension.

The SDK ships as a compiled, signed `EveryCordSDK.xcframework`. LiveKit's two binary frameworks (`LiveKitWebRTC` and
`RustLiveKitUniFFI`, for co-hosts) come in as their own dependencies and are embedded in your app. Don't add LiveKit's
own packages or pods (`client-sdk-swift`, `LiveKitClient`, `LiveKitUniFFI`) next to it: LiveKit's Swift code is already
inside the SDK. If your app uses LiveKit itself, write to services@symaro.com before you integrate.

## Install with Swift Package Manager

In Xcode, choose **File > Add Package Dependencies**, enter the repository URL and pick version 1.0.0 or later:

```
https://github.com/SymaroTeam/everycord-sdk
```

Add the `EveryCordSDK` product to your app target **and** to your broadcast upload extension target.

In a `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/SymaroTeam/everycord-sdk", from: "1.0.0"),
],
targets: [
    .target(
        name: "MyApp",
        dependencies: [.product(name: "EveryCordSDK", package: "everycord-sdk")]
    ),
]
```

## Install with CocoaPods

CocoaPods trunk becomes read-only in December 2026, so the EveryCord pod is not on trunk. Use either form below; both
install the same SDK as Swift Package Manager, from the release's `EveryCordSDK-CocoaPods.zip` (the SDK and LiveKit's
`RustLiveKitUniFFI.xcframework`), with LiveKit's `LiveKitWebRTC` pod.

**The podspec at a release tag.** Pin the version in the URL:

```ruby
platform :ios, '16.0'
use_frameworks!

target 'MyApp' do
  pod 'EveryCordSDK', :podspec => 'https://raw.githubusercontent.com/SymaroTeam/everycord-sdk/1.0.0/EveryCordSDK.podspec'
end

target 'MyAppBroadcast' do
  pod 'EveryCordSDK', :podspec => 'https://raw.githubusercontent.com/SymaroTeam/everycord-sdk/1.0.0/EveryCordSDK.podspec'
end
```

**The EveryCord specs repository.** Add it as a source, before the CocoaPods CDN (which still serves `LiveKitWebRTC`):

```ruby
source 'https://github.com/SymaroTeam/everycord-specs.git'
source 'https://cdn.cocoapods.org/'

platform :ios, '16.0'
use_frameworks!

target 'MyApp' do
  pod 'EveryCordSDK', '~> 1.0'
end

target 'MyAppBroadcast' do
  pod 'EveryCordSDK', '~> 1.0'
end
```

With either form, add this to the end of the Podfile: LiveKit's `LiveKitWebRTC` pod still declares iOS 13, which
current Xcode no longer builds, so its deployment target is raised to your app's:

```ruby
post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      if config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'].to_f < 16.0
        config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '16.0'
      end
    end
  end
end
```

Then run `pod install` and open the `.xcworkspace`.

## Set up the project

1. **App Group.** In Signing and Capabilities, add the same App Group (for example `group.com.example.app`) to the app
   and to the broadcast upload extension. The SDK hands the live session from the app to the extension there.
2. **Broadcast upload extension.** Add a target with **File > New > Target > Broadcast Upload Extension** (no setup UI
   needed). Its bundle ID is usually your app's plus `.Broadcast`.
3. **Info.plist of the app.** `NSMicrophoneUsageDescription`, and `NSCameraUsageDescription` if you use the facecam or
   co-hosts. For Picture in Picture, add the `voip` background mode.
4. **Info.plist of the extension.** Set `EveryCordAppGroup` to your App Group (or override `appGroupIdentifier` in
   code), and keep `NSExtensionPrincipalClass` pointing at your handler class.

## Write the broadcast extension

Replace the template's `SampleHandler` with a subclass of `EveryCordBroadcastHandler`. It picks up the session your app
started, captures the screen with app audio and the microphone, encodes and uploads, and ends the broadcast when the app
stops the session.

```swift
import EveryCordSDK

final class SampleHandler: EveryCordBroadcastHandler {
    override var appGroupIdentifier: String { "group.com.example.app" }
}
```

The extension runs under iOS's memory limit for broadcast extensions, so keep other frameworks out of it.

## Configure the SDK

Call `configure` once at launch, before anything else. It loads your partner configuration in the background and
restores a signed-in user.

```swift
import EveryCordSDK
import SwiftUI

@main
struct MyApp: App {
    init() {
        EveryCord.configure(publishableKey: "ecpk_test_...", appGroup: "group.com.example.app")
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

`EveryCord.config` holds your tier, features, branding and limits once loaded. Hide what your tier does not include:

```swift
if EveryCord.isEnabled(.cohosts) {
    // Show the co-host button.
}
```

## Sign a user in

Your backend links your user to an EveryCord account and mints a single-use token
([Accounts and sign-in](accounts.md)). Pass it to the SDK:

```swift
let token = try await myBackend.everyCordUserToken()   // from POST /v1/partner/users/:externalUserId/token
let user = try await EveryCord.auth.signIn(userToken: token)
print(user.username)
```

## Go live

Start a session in the app, then let the person start the broadcast with the system picker:

```swift
let options = LiveOptions(
    title: "Ranked grind",
    orientation: .portrait,
    destinations: [
        .rtmp(name: "YouTube", url: URL(string: "rtmps://a.rtmps.youtube.com/live2")!, streamKey: youTubeKey),
    ],
    facecam: FacecamOptions(shape: .circle, corner: .bottomTrailing)
)
let session = try await EveryCord.live.start(options)
```

```swift
EveryCordBroadcastPicker(extensionBundleIdentifier: "com.example.app.Broadcast")
    .frame(width: 60, height: 60)
```

The extension picks the session up within 10 minutes of `start`. `LiveSession` is an `ObservableObject`:

```swift
struct LiveStatusView: View {
    @ObservedObject var session: LiveSession

    var body: some View {
        VStack {
            Text(session.state.rawValue)          // idle, connecting, live, reconnecting, ended
            Text("\(session.viewerCount) watching")
            Text("\(session.bitrate / 1000) kbps")
            Button("End stream") { Task { await session.stop() } }
        }
    }
}
```

`session.outputs` lists each destination and EveryCord's own delivery with its state, bitrate and a short reason when
one fails (never a URL or a key).

## Watch and chat

```swift
EveryCordPlayerView(sessionID: session.id)
ChatView(chat: session.chat)
```

The player plays a live session or its replay. It shows "Powered by EveryCord" when your tier's branding requires it.

## Errors

The SDK throws `EveryCordError`:

| Case | When |
|---|---|
| `notConfigured` | `configure` was not called |
| `configUnavailable` | the configuration has not loaded yet (no network since launch) |
| `featureNotIncluded(feature, requiredTier:)` | the feature is above your tier (`403 partner_tier`) |
| `invalidPublishableKey` | `401 partner_key`: missing, unknown, revoked or wrong-mode key |
| `appMismatch` | `403 partner_app`: the bundle ID is not the key's app |
| `partnerPaused` | `403 partner_paused`: your partnership or this app is paused |
| `notSignedIn` | the call needs a signed-in partner user |
| `invalidOptions(sentence)` | `LiveOptions` failed validation; the sentence says what to change |
| `appGroupUnavailable` | the App Group is missing from the app or the extension |
| `alreadyLive` | a live session is already running in this app |
| `unsupported(sentence)` | needs a newer iOS (Picture in Picture needs iOS 18) |
| `network` | the request never got a response |
| `server(status:code:message:)` | any other API error, with its code and a sentence to show |

## Next

- [Live streaming](live-streaming.md): destinations, bonding and the EveryCord Relay.
- [Co-hosts and Picture in Picture](cohosts.md)
- [Sparks, video requests and Boosts](creator-monetization.md)
