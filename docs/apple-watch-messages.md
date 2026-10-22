# Apple Watch and Messages

Two iOS SDK helpers in the Extras tier: the Apple Watch screen as a capture source (feature `watch_capture`), and
sharing recordings into Messages (feature `messages_share`). Neither needs a server route; the SDK enables them from
your configuration.

> **Early access.** Both helpers are in early access in the iOS SDK; this page describes how they work in EveryCord's
> own app today. Write to [services@symaro.com](mailto:services@symaro.com) to join.

## Apple Watch recording

In EveryCord, a person's Apple Watch screen appears in a recording or a live stream through Apple Watch Mirroring on
their iPhone, which does the recording. Streams can be started and stopped from the Watch, the person can mute the
microphone or add a marker from their wrist, and a Live Activity shows the stream's state.

**Requirements.** An iPhone with iOS 16 or later, and an Apple Watch Series 6 or later or any Apple Watch Ultra (Apple
Watch SE does not support Apple Watch Mirroring).

**In your app.** The helper adds the Watch screen as a capture source to your recording or live stream, so your users
can show a workout, a watch face or a Watch app the same way they show their iPhone screen. Check
`EveryCord.isEnabled(.watchCapture)` before offering it.

## Sharing to Messages

In EveryCord, recordings go straight into iMessage conversations from the Messages app drawer, and creators talk with
their community in [direct messages](accounts.md#direct-messages).

**In your app.** The helper gives your iMessage app extension a ready-made browser of the person's recordings and clips
and sends the one they pick into the conversation. Check `EveryCord.isEnabled(.messagesShare)` before offering it.

## Tier

Both are in the Extras tier, so every partner gets them, with "Powered by EveryCord" where the EveryCord feature
appears. See [Tiers, feature gates and branding](tiers-branding.md).
