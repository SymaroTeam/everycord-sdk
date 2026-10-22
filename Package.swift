// swift-tools-version: 5.9
//
// EveryCord SDK for iOS: https://everycord.net/developers
//
// The SDK ships as a compiled binary, EveryCordSDK.xcframework, attached to each GitHub release of this repository.
// LiveKit's WebRTC and UniFFI frameworks, which the binary links dynamically, come straight from LiveKit's own releases
// at the exact versions the release was built with. Only those two prebuilt frameworks: LiveKit's Swift code (including
// the LiveKitUniFFI bindings) is already inside EveryCordSDK, and a second copy from LiveKit's packages would define
// its classes twice. An app that also uses LiveKit on its own needs EveryCord's help first (services@symaro.com).
//
// Install: in Xcode choose File > Add Package Dependencies, enter https://github.com/SymaroTeam/everycord-sdk and add
// the EveryCordSDK product to your app and to its broadcast upload extension. Then `import EveryCordSDK`.
//
// The `version` and `checksum` lines below are rewritten for each release (SDK/scripts/build-ios.sh); the LiveKit
// lines change only when the SDK is built against another LiveKit.

import PackageDescription

let version = "1.0.0"
let checksum = "ebb72ef599e5995e742f72aabb9a9fe8a14b4fedfeae060a022e76024e11f23a"

let package = Package(
    name: "EveryCordSDK",
    platforms: [
        .iOS(.v16),
    ],
    products: [
        .library(name: "EveryCordSDK", targets: ["EveryCordSDKLinks"]),
    ],
    targets: [
        .binaryTarget(
            name: "EveryCordSDK",
            url: "https://github.com/SymaroTeam/everycord-sdk/releases/download/\(version)/EveryCordSDK.xcframework.zip",
            checksum: checksum
        ),
        // LiveKit's prebuilt frameworks, as webrtc-xcframework 150.7871.03 and livekit-uniffi-xcframework 0.1.9 declare them.
        .binaryTarget(
            name: "LiveKitWebRTC",
            url: "https://github.com/livekit/webrtc-xcframework/releases/download/150.7871.03/LiveKitWebRTC.xcframework.zip",
            checksum: "5727a03c32ed902da570b699e3983e3fb4bfe853ab4da045af70912a7db44557"
        ),
        .binaryTarget(
            name: "RustLiveKitUniFFI",
            url: "https://github.com/livekit/livekit-uniffi-xcframework/releases/download/0.1.9/RustLiveKitUniFFI.xcframework.zip",
            checksum: "8db93e8ccd9f22263c39855808da48a4d6b5d620d8f6e43ab4501ede847b23eb"
        ),
        // Links the binary together with its LiveKit frameworks, and re-exports the EveryCordSDK module.
        .target(
            name: "EveryCordSDKLinks",
            dependencies: ["EveryCordSDK", "LiveKitWebRTC", "RustLiveKitUniFFI"],
            path: "Sources/EveryCordSDKLinks"
        ),
    ]
)
