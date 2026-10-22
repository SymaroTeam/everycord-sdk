# Mac and Apple Vision Pro

The iOS SDK grows macOS and visionOS slices in the same XCFramework, installed the same way (Swift Package Manager or
CocoaPods). Feature `apple_platforms`, Extras tier: with it, macOS and visionOS builds of your app are accepted with
their own publishable keys.

> **Early access.** The macOS and visionOS slices follow the iOS release and go to early-access partners first. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Apps and keys

Each platform of your app is its own partner app, with its own keys:

| Platform | `platform` value | Identified by |
|---|---|---|
| iPhone and iPad | `ios` | bundle identifier |
| Mac | `macos` | bundle identifier |
| Apple Vision Pro | `visionos` | bundle identifier |
| Android | `android` | application ID |

Add each one in the [partner dashboard](dashboard.md). The SDK sends `EveryCord-SDK: macos/<version>` or
`visionos/<version>` with its requests.

## What EveryCord does on these platforms today

- **Mac.** EveryCord's Mac app records and streams the Mac screen, and also records an iPhone connected over USB.
- **Apple Vision Pro.** EveryCord's visionOS app watches live streams with the chat beside them, and browses Live Now
  and clips.
- **Sign-in with a code.** Where typing is awkward, people sign in by approving a short code on their iPhone (device
  code sign-in, `everycord.net/link/<code>`).

The SDK's macOS and visionOS builds bring these to partner apps. What each build includes is listed in the
[changelog](changelog.md) with its release.

## Requirements

The same as iOS where they apply: Xcode 27 or later, and the same Swift package or podspec. Minimum macOS and visionOS
versions are set with the first release of each slice.
