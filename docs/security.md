# Security and code protection

How the SDK, the keys and the media are protected, and what you should do on your side.

## Keys

- **Publishable keys** ship in your app. Each is tied to one app: the server accepts it only with that app's bundle ID
  or application ID (`403 partner_app` otherwise). On its own it can read your configuration and start sign-in, nothing
  more.
- **Secret keys** stay on your backend. They are shown once; EveryCord stores only a peppered hash. Never put one in an
  app, a repository or a log. The Android SDK refuses a secret key in `configure`.
- **Roll and revoke** keys in the [partner dashboard](dashboard.md). A revoked key answers `401 partner_key` at once.
- **Test and live** keys are separate: test keys never see live-mode data.
- **Webhook secrets** (`whsec_...`) are shown once. [Verify every webhook](webhooks.md#verify-the-signature) and reject
  old timestamps.

## User sessions

- User tokens from your backend are single use and expire after 5 minutes; only their hash is stored.
- Access tokens live 60 minutes; refresh tokens live 60 days and rotate on every use.
- The iOS SDK keeps the session in the Keychain (data protection keychain); the Android SDK in encrypted storage.

## Media

- Live video goes from the device straight to Cloudflare R2 through presigned upload URLs that are short-lived and
  scoped to one stream. No partner server, and no EveryCord media server, handles the upload.
- RTMP and SRT stream keys and SRT passphrases stay on the device and go only to their server.
- Co-host video travels in an encrypted WebRTC room (LiveKit).

## What the SDK never does

- It never logs URLs, keys, tokens, stream keys or passphrases. Status messages describe failures without them.
- It includes no advertising, analytics or attribution SDKs, and does not track people across other companies' apps
  and websites.
- It never sees card details or the buyer's store account; the purchase cross-check sends only product, transaction,
  price, currency and storefront.

## Code protection

The SDKs ship as compiled binaries only. No SDK source code is shared, in the public repository or anywhere else.

- **iOS**: `EveryCordSDK.xcframework`, built for distribution with stripped local symbols, no source info files, no
  debug symbols in the release zip, and only the public `.swiftinterface`.
- **Android**: an AAR shrunk and obfuscated with R8: internal classes are renamed and repackaged, with keep rules only
  for the public API.
- The backend stays on EveryCord's infrastructure.

The SDKs are licensed under your partner agreement. Reverse engineering, decompiling or redistributing them outside your
app is not allowed.

## Safety and moderation

- EveryCord's moderators review reports, and can suspend or ban accounts; your backend gets a `user.restricted` webhook,
  and you see reports, restrictions and actions about your app ([Moderation for partners](moderation.md)).
- Video requests, Drops, Boosts and payouts are limited to adults in good standing.
- Chat has a language filter, slow mode, moderators, bans and blocking ([Chat and moderation](chat.md)).

## Reporting a security issue

Email [services@symaro.com](mailto:services@symaro.com) with "Security" in the subject. Please give us a chance to fix
an issue before you share it.
