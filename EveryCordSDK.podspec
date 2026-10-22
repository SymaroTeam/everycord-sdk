# EveryCord SDK for iOS: https://everycord.net/developers
#
# CocoaPods trunk becomes read-only in December 2026, so this pod is installed from this podspec at a release tag or
# from the EveryCord specs repository (github.com/SymaroTeam/everycord-specs):
#
#   pod 'EveryCordSDK', :podspec => 'https://raw.githubusercontent.com/SymaroTeam/everycord-sdk/1.0.0/EveryCordSDK.podspec'
#
# The binary is the same EveryCordSDK.xcframework Swift Package Manager installs, in the release's
# EveryCordSDK-CocoaPods.zip together with LiveKit's RustLiveKitUniFFI.xcframework (Apache-2.0, license in LICENSES/).
# LiveKit's WebRTC framework comes from LiveKit's own binary pod. Not LiveKitClient or LiveKitUniFFI: LiveKit's Swift
# code, the UniFFI bindings included, is already inside EveryCordSDK, and a second copy would define its classes twice.
# The version and sha256 lines are rewritten for each release (SDK/scripts/build-ios.sh).

Pod::Spec.new do |s|
  s.name              = 'EveryCordSDK'
  s.version           = '1.0.0'
  s.summary           = 'Live streaming, co-hosts, chat, clips, Sparks and accounts from EveryCord, inside your iOS app.'
  s.description       = <<~DESC
    The EveryCord SDK puts EveryCord's live streaming (worldwide delivery, adaptive upload, RTMP, RTMPS and SRT
    destinations, bonding), co-hosts, Picture in Picture, overlays, captions, chat, clips, Sparks and accounts inside
    your own app, with EveryCord's servers behind them. Licensed only under an EveryCord partner agreement.
  DESC
  s.homepage          = 'https://everycord.net/developers'
  s.documentation_url = 'https://everycord.net/developers'
  s.license           = {
    :type => 'Proprietary',
    :text => 'Copyright (c) 2026 Symaro, LLC. All rights reserved. Use only under an EveryCord partner agreement with Symaro, LLC. See https://github.com/SymaroTeam/everycord-sdk/blob/main/LICENSE.'
  }
  s.author            = { 'Symaro, LLC' => 'services@symaro.com' }
  s.source            = {
    :http => "https://github.com/SymaroTeam/everycord-sdk/releases/download/#{s.version}/EveryCordSDK-CocoaPods.zip",
    :sha256 => 'ff9e62070efecb5327c0083a3fd43df701fdff933cea8232c14cb707c4b8831c'
  }

  s.platform              = :ios, '16.0'
  s.ios.deployment_target = '16.0'
  s.swift_versions        = ['5.9']
  s.vendored_frameworks   = 'EveryCordSDK.xcframework', 'RustLiveKitUniFFI.xcframework'
  s.frameworks            = 'ReplayKit', 'AVKit', 'Speech'

  # The WebRTC build the SDK was compiled against (webrtc-xcframework 150.7871.03).
  s.dependency 'LiveKitWebRTC', '= 150.7871.03'
end
