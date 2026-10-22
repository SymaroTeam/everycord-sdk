// swift-tools-version: 5.9
//
// EveryCord SDK for iOS: https://everycord.net/developers
//
// The SDK ships as a compiled binary, EveryCordSDK.xcframework, attached to each GitHub release of this repository.
// LiveKit's WebRTC and UniFFI frameworks, which the binary links dynamically, come from LiveKit's own packages at the
// exact versions the release was built with.
//
// Install: in Xcode choose File > Add Package Dependencies, enter https://github.com/SymaroTeam/everycord-sdk and add
// the EveryCordSDK product to your app and to its broadcast upload extension. Then `import EveryCordSDK`.
//
// The version and checksum lines below are rewritten for each release.

import PackageDescription

let version = "1.0.0"
let checksum = "c854bc01afb21f018d4d6e79f432583625ef7c01ae2e049c46049a90d3b94d0b"

let package = Package(
    name: "EveryCordSDK",
    platforms: [
        .iOS(.v16),
    ],
    products: [
        .library(name: "EveryCordSDK", targets: ["EveryCordSDKLinks"]),
    ],
    dependencies: [
        .package(url: "https://github.com/livekit/webrtc-xcframework.git", exact: "150.7871.3"),
        .package(url: "https://github.com/livekit/livekit-uniffi-xcframework.git", exact: "0.1.9"),
    ],
    targets: [
        .binaryTarget(
            name: "EveryCordSDK",
            url: "https://github.com/SymaroTeam/everycord-sdk/releases/download/\(version)/EveryCordSDK.xcframework.zip",
            checksum: checksum
        ),
        // Links the binary together with its LiveKit frameworks, and re-exports the EveryCordSDK module.
        .target(
            name: "EveryCordSDKLinks",
            dependencies: [
                "EveryCordSDK",
                .product(name: "LiveKitWebRTC", package: "webrtc-xcframework"),
                .product(name: "LiveKitUniFFI", package: "livekit-uniffi-xcframework"),
            ],
            path: "Sources/EveryCordSDKLinks"
        ),
    ]
)
