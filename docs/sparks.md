# Sparks, video requests and Boosts

Sparks let viewers support the creators who stream from your app, and pay those creators. Feature `sparks`, Engage tier
and up (routes `/v1/sparks/*`, `/v1/boosts/*`, `/v1/video-requests/*` and `POST /v1/live/:id/sparks`).

> **Early access.** Code samples are illustrative until the 1.0.0 release. How people buy Sparks inside a partner app is
> set in each partner agreement and is in early access. Write to [services@symaro.com](mailto:services@symaro.com) to
> join.

## Sparks

- Viewers buy Sparks and send them to a creator during a stream. A gift shows in the chat with its message.
- Creators get half of what EveryCord receives for the Sparks they are sent, paid through Stripe Connect in the United
  States, the United Kingdom, the EEA, Canada and Switzerland. Holds, refunds and statements follow EveryCord's Sparks
  terms.
- Sparks sent in your app go through EveryCord's Sparks ledger; your partner agreement records how the revenue is split.
- In test mode, Sparks are test Sparks and never money.

```swift
let balance = try await EveryCord.sparks.balance()          // enabled, balance, testBalance, lifetimeSent
let gift = try await EveryCord.sparks.send(100, toSession: sessionID, message: "Great run!")
showGift(gift.caption)       // the message plus "Sparks sent via EveryCord" when your branding requires it
```

```kotlin
val balance = EveryCord.sparks.balance()
val result = EveryCord.sparks.send(sessionId, amount = 100, message = "Great run!")
```

A retry of the same send (the same `clientID` on iOS) never sends twice.

**The credit line.** On the Engage tier every gift shows "Sparks sent via EveryCord" (`EveryCord.sparks.credit`, and
`caption` on gifts and chat messages). On the Live tier it follows your agreement. See
[Tiers, feature gates and branding](tiers-branding.md).

**Webhook.** `sparks.received` tells your backend about each Sparks send in a partner stream ([Webhooks](webhooks.md)).

## Video requests

A viewer pays a creator Sparks for a short personal video.

- The price is held when the request is made, and paid to the creator when they deliver within the deadline (3, 5 or 7
  days). Otherwise (declined, expired, cancelled) the Sparks go back.
- A delivered request earns the creator what a live send of the same Sparks earns.
- Requesters are signed in, 18 or older and have accepted the Sparks terms. Creators are 18 or older, in good standing,
  with payouts set up and requests turned on.

```swift
let quote = try await EveryCord.sparks.videoRequestQuote(creatorID: creatorID)
if quote.eligible {
    let request = try await EveryCord.sparks.requestVideo(
        creatorID: creatorID, priceSparks: quote.priceSparks, category: quote.categories[0],
        instructions: "Say happy birthday to Sam!")
}
let mine = try await EveryCord.sparks.videoRequests()                 // sent, newest first
let toMe = try await EveryCord.sparks.videoRequests(received: true)
```

```kotlin
val request = EveryCord.sparks.requestVideo(creatorId, priceSparks = 500, category = "shoutout",
                                            instructions = "Say happy birthday to Sam!")
val open = EveryCord.sparks.videoRequests(role = "buyer", state = "open")
```

A request's `status` moves through `pending`, `accepted` and `delivered`, or ends as `declined`, `cancelled` or
`expired`.

## Boosts

Creators spend Sparks to promote their own live stream, Past stream or clip in EveryCord's feeds, putting more viewers in
front of them.

- A Boost has a budget in Sparks, a top bid per 1,000 Promoted views and a duration (24 hours by default, 7 days at
  most). The whole budget is reserved at once; unspent Sparks go back when it ends.
- Creators who boost are 18 or older and in good standing.

```swift
let boost = try await EveryCord.sparks.boost(.live(sessionID: session.id), budgetSparks: 2_000,
                                             bidPerThousand: 120, durationHours: 24)
```

```kotlin
val boost = EveryCord.sparks.boost("live", sessionId, budgetSparks = 2000, bidPerThousand = 120, hours = 24)
val boosts = EveryCord.sparks.boosts()
EveryCord.sparks.stopBoost(boost.id)
```

Targets are a live stream (while live), a public Past stream or a clip. A Boost's `status` is `active`, `ended` or
`stopped`.

## Safety

Video requests, Boosts and payouts are limited to adults in good standing. Sparks have daily limits, and EveryCord
reviews unusual activity before payouts.
