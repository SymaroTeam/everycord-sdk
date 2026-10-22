# Changelog

Releases of the EveryCord SDKs and the Partner API. Versions follow semantic versioning: a minor version adds features,
a major version may change the public API.

## Early access (October 2026)

The SDKs and the Partner API are in early access. Release builds go to early-access partners first, starting with iOS;
the public repository gets each release's binaries as it ships.

**Partner API**

- Keys: publishable (`ecpk_`) and secret (`ecsk_`), in live and test modes.
- `GET /v1/partner/config` with tier, features, branding and limits.
- Partner users: `PUT`, `DELETE /v1/partner/users/:externalUserId`, user tokens and `POST /v1/partner/auth/token`.
- Usage, live sessions, webhooks (`live.started`, `live.ended`, `clip.created`, `sparks.received`, `user.restricted`,
  `test`) and monthly revenue reports.
- Tier gates on every EveryCord route used with the partner headers.

**iOS SDK** (iOS 16 and later)

- `EveryCord.configure`, configuration and feature checks.
- Sign-in with your user tokens, Apple, Google and email codes.
- Live streaming from a broadcast upload extension (`EveryCordBroadcastHandler`) with EveryCord delivery, up to three
  RTMP, RTMPS or SRT destinations, bonding and the EveryCord Relay.
- Co-hosts, Picture in Picture (iOS 18), facecam, overlays and captions.
- Player, chat, Sparks, video requests, Boosts and clips.

**Android SDK** (Android 10 and later)

- `EveryCord.configure`, configuration and feature checks.
- Sign-in with your user tokens, Google and email codes.
- Live streaming with MediaProjection (`EveryCordCaptureService`), EveryCord delivery, up to three RTMP, RTMPS or SRT
  destinations, bonding and the EveryCord Relay.
- Co-hosts, facecam, overlays (including your own picture) and captions (Android 13 and later).
- Player, chat with moderation, Sparks, video requests, Boosts and clips.

**Next in early access**

- Earnings: App Store Connect and Google Play connections, statements, and the SDK purchase cross-check.
- The self-serve [partner dashboard](dashboard.md).
- [Moderation visibility](moderation.md): reports, restricted users, chat bans and the actions log, with webhooks.
- Apple Watch capture and Messages sharing helpers.
- macOS and visionOS slices of the iOS SDK.
