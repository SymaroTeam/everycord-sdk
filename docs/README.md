# EveryCord SDK documentation

The EveryCord SDK puts EveryCord's live streaming, co-hosts, chat, clips, Sparks and accounts inside your own iOS and
Android app, with EveryCord's servers behind them. Your users stream from your app, your viewers watch in your app, and
EveryCord runs the infrastructure: upload URLs, worldwide delivery, chat, clips, creator earnings and moderation. In
return, EveryCord receives a share of the revenue from the EveryCord-powered features in your app (10%, 20% or 30%, by
tier).

> **Early access.** The SDKs and the Partner API are in early access. Code samples are illustrative until the 1.0.0
> release and follow the SDKs' current public names. To join, write to
> [services@symaro.com](mailto:services@symaro.com).

## Start here

- [How it works](overview.md): the pieces, the keys, and how live video goes from the phone straight to the cloud.
- [Quickstart for iOS](quickstart-ios.md): install with Swift Package Manager or CocoaPods and go live from a broadcast
  extension.
- [Quickstart for Android](quickstart-android.md): install with Gradle and go live from a MediaProjection service.
- [Tiers, feature gates and branding](tiers-branding.md): what each tier includes and what EveryCord's name appears on.

## Accounts

- [Accounts and sign-in](accounts.md): your own user IDs linked to EveryCord accounts, Sign in with Apple, Google and
  email codes, profiles, follows, blocking and direct messages.

## Live

- [Live streaming](live-streaming.md): EveryCord delivery, adaptive upload, RTMP, RTMPS and SRT destinations, bonding,
  the EveryCord Relay and playback in your app.
- [Co-hosts and Picture in Picture](cohosts.md): up to three friends on camera, and the host's camera in a floating
  window.
- [Facecam and captions](facecam-captions.md): the host's camera in the stream, and automatic captions.
- [Overlays](overlays.md): name banners, badges, counters, Sparks goals and chat drawn into the video.
- [Chat and moderation](chat.md): live chat, reactions, moderators, bans and the language filter.
- [Clips and replays](clips.md): Past streams and clips cut on the server.

## Engage and extras

- [Sparks, video requests and Boosts](creator-monetization.md): viewers support creators, and creators get paid.
- [Drops](drops.md): viewers earn in-game item codes by watching.
- [Apple Watch and Messages](apple-watch-messages.md): the Apple Watch screen as a capture source, and sharing into
  Messages.
- [Mac and Apple Vision Pro](mac-vision-pro.md): the SDK on macOS and visionOS.

## Server

- [Partner API reference](api-reference.md): every route, with requests, responses and error codes.
- [Webhooks](webhooks.md): events, signatures and retries, with verification code in Node.js and Python.
- [Moderation for partners](moderation.md): reports, suspended and banned users, chat bans and moderation actions for
  your app.

## Business

- [Earnings tracking and payment terms](earnings.md): App Store Connect and Google Play reports, statements, and
  payment 30 days after the store pays you.
- [Partner dashboard](dashboard.md): sign up at everycord.net/partners, add apps, connect your store, and get your keys.
- [Security and code protection](security.md): keys, compiled binaries, and what the SDK never logs.
- [Changelog](changelog.md)

## Get help

- Email [services@symaro.com](mailto:services@symaro.com) for early access, integration questions and partnership terms.
- The documentation is also on the web at [everycord.net/developers](https://everycord.net/developers), and with the
  install files at [github.com/SymaroTeam/everycord-sdk](https://github.com/SymaroTeam/everycord-sdk).

EveryCord is made by Symaro, LLC. The SDKs are proprietary and are licensed only under a partner agreement.
