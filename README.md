# EveryCord SDK

The EveryCord SDK puts EveryCord's live streaming, co-hosts, chat, clips, Sparks and accounts inside your own iOS and
Android app, with EveryCord's servers behind them.

- **Live**: worldwide delivery straight from the phone to Cloudflare R2, adaptive upload, up to three RTMP, RTMPS or SRT
  destinations, bonding over every network and the EveryCord Relay.
- **On camera**: up to three co-hosts, Picture in Picture on iOS 18, facecam, overlays and automatic captions.
- **Community**: live chat and moderation, clips and replays, Drops, direct messages and accounts linked to your own
  user IDs.
- **Earnings**: Sparks, video requests and Boosts, with creator payouts.

EveryCord is the free screen recorder and live streaming app from Symaro, LLC, on the App Store since 2018. Partner apps
use its technology for a share of the revenue from the EveryCord-powered features: 10%, 20% or 30% by tier.

> **Early access.** The SDKs and the Partner API are in early access. Release binaries are attached to this
> repository's releases as they ship, starting with iOS; until the first release, the install steps below are for
> reference. Write to [services@symaro.com](mailto:services@symaro.com) to join.

## Documentation

Everything is documented at **[everycord.net/developers](https://everycord.net/developers)**, and the same pages are in
[`docs/`](docs/README.md):

- [How it works](docs/overview.md)
- [Quickstart for iOS](docs/quickstart-ios.md) and [Quickstart for Android](docs/quickstart-android.md)
- [Tiers, feature gates and branding](docs/tiers-branding.md)
- [Partner API reference](docs/api-reference.md) and [Webhooks](docs/webhooks.md)
- [Earnings tracking and payment terms](docs/earnings.md) and the [Partner dashboard](docs/dashboard.md)

## Install on iOS

iOS 16 or later, Xcode 27 or later.

**Swift Package Manager.** In Xcode, choose **File > Add Package Dependencies**, enter
`https://github.com/SymaroTeam/everycord-sdk`, pick version 1.0.0 or later, and add the `EveryCordSDK` product to your
app and to its broadcast upload extension.

```swift
.package(url: "https://github.com/SymaroTeam/everycord-sdk", from: "1.0.0")
```

**CocoaPods.** CocoaPods trunk becomes read-only in December 2026, so install from the podspec at a release tag:

```ruby
pod 'EveryCordSDK', :podspec => 'https://raw.githubusercontent.com/SymaroTeam/everycord-sdk/1.0.0/EveryCordSDK.podspec'
```

or from the EveryCord specs repository:

```ruby
source 'https://github.com/SymaroTeam/everycord-specs.git'
source 'https://cdn.cocoapods.org/'

pod 'EveryCordSDK', '~> 1.0'
```

Either way, add the Podfile's `post_install` hook from the quickstart (it raises LiveKit's `LiveKitWebRTC` pod from
iOS 13 to 16, which current Xcode needs). Then: [Quickstart for iOS](docs/quickstart-ios.md).

## Install on Android

Android 10 (API 29) or later. The SDK is `net.everycord:sdk` on GitHub Packages; see [ANDROID.md](ANDROID.md) for the
repository setup and a GitHub token with `read:packages`.

```kotlin
dependencies {
    implementation("net.everycord:sdk:1.0.0")
}
```

Then: [Quickstart for Android](docs/quickstart-android.md).

## What is in this repository

| Path | What |
|---|---|
| `docs/` | the documentation, the same as everycord.net/developers |
| `Package.swift` | the Swift package: the release's `EveryCordSDK.xcframework` and its LiveKit dependencies |
| `EveryCordSDK.podspec` | the CocoaPods spec: the same framework, from the release's `EveryCordSDK-CocoaPods.zip` |
| `ANDROID.md` | Gradle and Maven setup for the Android SDK |
| `samples/` | sample apps for iOS and Android |
| `CHANGELOG.md` | releases |

This repository holds documentation, install files, samples and release binaries only. The SDK's source code is not
public: the SDKs ship as compiled, obfuscated binaries.

## License

Proprietary. Copyright (c) 2026 Symaro, LLC. All rights reserved. The SDKs may be used only under an EveryCord partner
agreement with Symaro, LLC; see [LICENSE](LICENSE).

## Contact

[services@symaro.com](mailto:services@symaro.com) for early access, integration help and partnership terms. Report
security issues to the same address with "Security" in the subject.
