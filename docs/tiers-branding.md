# Tiers, feature gates and branding

Three tiers, grouped by how much partners want the features. Tiers are cumulative: Live includes Engage, which includes
Extras. Your tier is the highest one whose features you use, and it sets EveryCord's share of the revenue from the
EveryCord-powered features in your app, after app store fees.

| Tier | Share | Features | Branding |
|---|---|---|---|
| Extras | 10% | accounts, Apple Watch recording, sharing to Messages, direct messages, Drops, Mac and Apple Vision Pro | "Powered by EveryCord" |
| Engage | 20% | Extras, plus Sparks, video requests and Boosts, overlays, captions, clips and replays, bonding and the EveryCord Relay | the EveryCord mark on streams, "Sparks sent via EveryCord" |
| Live | 30% | Engage, plus worldwide live delivery and adaptive upload, multistreaming over RTMP and SRT, screen capture and facecam, co-hosts and Picture in Picture, live chat and moderation | as agreed; can be fully unbranded |

Accounts come with every tier. Your partner agreement can set a different share; the share applies only to revenue from
EveryCord-powered features.

## Feature keys

| Key | Tier | What it covers |
|---|---|---|
| `accounts` | Extras | partner users, sign-in, profiles, follows, blocking |
| `watch_capture` | Extras | the Apple Watch screen as a capture source (iOS SDK) |
| `messages_share` | Extras | sharing recordings into Messages (iOS SDK helper) |
| `direct_messages` | Extras | `/v1/messages/*` for partner users |
| `drops` | Extras | `/v1/drops/*` |
| `apple_platforms` | Extras | macOS and visionOS builds accept the key |
| `sparks` | Engage | Sparks sends, balances, payouts, video requests, Boosts |
| `overlays` | Engage | `/v1/overlays*` and drawing in the SDK |
| `captions` | Engage | automatic captions |
| `clips` | Engage | clips and replays (Past streams, Quick clips) |
| `bonding` | Engage | SRTLA bonding and the EveryCord Relay |
| `live_delivery` | Live | EveryCord delivery (`POST /v1/live` with upload to R2), viewers on your player |
| `multistream` | Live | RTMP, RTMPS and SRT destinations (up to 3) |
| `capture` | Live | screen capture and facecam |
| `cohosts` | Live | co-host rooms (LiveKit) and Picture in Picture |
| `chat` | Live | live chat and moderation |

## How the gates work

- `GET /v1/partner/config` returns `tier` and `features` (all 16 keys). The SDK hides what your tier does not include:
  check `EveryCord.isEnabled(.cohosts)` (iOS) or `EveryCord.isEnabled(EveryCordFeature.COHOSTS)` (Android) before showing a
  feature's UI.
- The server enforces the tier on every partner request. A request for a feature above your tier answers
  `403 partner_tier` with `error.feature` (the key) and `error.tier` (the tier that includes it).
- A feature in your tier can still be turned off for one app by EveryCord; `isEnabled` then returns false.

## Branding

`GET /v1/partner/config` returns `branding`, and the SDK draws it. You cannot turn it off in code.

| Tier | `watermark` | `sparksCredit` | `poweredBy` |
|---|---|---|---|
| Extras | false | false | true |
| Engage | true | true | true |
| Live | as agreed, default false | as agreed, default false | as agreed, default false |

- **`watermark`**: the EveryCord mark, drawn by the SDK into the bottom-right of the live video, so every destination
  shows it.
- **`sparksCredit`**: "Sparks sent via EveryCord" on every gift, in chat and in the gift's caption.
- **`poweredBy`**: "Powered by EveryCord" where EveryCord features appear: the player, the chat and the Sparks views.

On the Extras tier, use "Powered by EveryCord" also in your own screens that present an EveryCord feature (for example
a Drops screen you design yourself).

## Limits

`limits` in your configuration:

| Limit | Live tier | Other tiers |
|---|---|---|
| `maxDestinations` | 3 | 0 |
| `maxCohosts` | 3 | 0 |
| `maxBitrateKbps` | 4500 | 0 |

`0` means the tier has no such feature.

## Changing tier

Pick a tier when you [sign up](dashboard.md); EveryCord can change it at any time in agreement with you. The SDK picks
up a new tier within about a minute (the configuration is cached for 60 seconds).
