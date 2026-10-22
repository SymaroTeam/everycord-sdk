# Partner API reference

Every Partner API route, with its request, response and errors. The base URL is `https://everycord.net`. All bodies are
JSON (`Content-Type: application/json`), keys are camelCase, timestamps are ISO 8601 in UTC, and IDs are opaque
strings: never parse them.

> **Early access.** The Partner API is in early access and answers `503 unavailable` until EveryCord turns it on for
> partners. The moderation routes are marked below; their shapes may still change. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Authentication

| Who calls | Header | Key |
|---|---|---|
| The SDK in your app | `EveryCord-Partner-Key: ecpk_...` with `EveryCord-App-Id: <bundle ID>` and `EveryCord-SDK: ios/1.0.0` | publishable |
| A signed-in partner user (through the SDK) | the headers above plus `Authorization: Bearer <accessToken>` | publishable |
| Your backend | `Authorization: PartnerSecret ecsk_...` | secret |

`EveryCord-App-Id` is your iOS, macOS or visionOS bundle identifier or Android application ID; the publishable key is
accepted only with the app it was issued for. `EveryCord-SDK` is the SDK's platform and version (`ios`, `android`,
`macos`, `visionos`), recorded for your dashboard and never required.

With the partner headers on **any** EveryCord `/v1/*` route, the request is attributed to your app (usage, live
sessions, Sparks) and your tier's gates apply. Rate limits are per network for publishable-key routes and per secret key
for secret-key routes.

## Errors

Errors are an HTTP status and:

```json
{ "error": { "code": "partner_tier", "message": "Co-hosts need the Live tier.", "feature": "cohosts", "tier": "live" } }
```

| Status | Code | Meaning |
|---|---|---|
| 400 | `validation_failed` | a field is missing or out of range |
| 401 | `partner_key` | missing, malformed, unknown or revoked key, or the wrong kind (a secret key in `EveryCord-Partner-Key`, a publishable key after `PartnerSecret`) |
| 401 | `unauthorized` | no or expired access token, or a bad user token |
| 403 | `partner_app` | `EveryCord-App-Id` is missing or is not the key's app |
| 403 | `partner_paused` | your partnership or this app is paused or ended |
| 403 | `partner_tier` | the feature is above your tier; `error.feature` is the feature key and `error.tier` the tier that includes it |
| 403 | `account_suspended` | the user is suspended or banned; `message` is a sentence to show as is |
| 404 | `not_found` | unknown ID or `externalUserId` |
| 409 | `external_user_exists` | `If-None-Match: *` and the `externalUserId` is already linked |
| 409 | `conflict` | a taken username, or too many webhooks |
| 429 | `rate_limited` | slow down; `Retry-After` says for how long |
| 503 | `unavailable` | the Partner API is not on |

A removed app's keys answer `401 partner_key`. Pausing a partner or an app ends its live sessions at once.

## SDK routes (publishable key)

### GET /v1/partner/config

Your partner, app, mode, tier, features, branding and limits. The SDK loads it at `configure` and keeps the last
answer. 120 a minute per network; `Cache-Control: private, max-age=60`.

```json
{
  "partner": { "id": "pn...", "name": "Clipwise" },
  "app": { "id": "px...", "platform": "ios", "bundleId": "com.clipwise.app" },
  "mode": "live",
  "tier": "engage",
  "features": {
    "accounts": true, "watch_capture": true, "messages_share": true, "direct_messages": true, "drops": true,
    "apple_platforms": true, "sparks": true, "overlays": true, "captions": true, "clips": true, "bonding": true,
    "live_delivery": false, "multistream": false, "capture": false, "cohosts": false, "chat": false
  },
  "branding": { "watermark": true, "sparksCredit": true, "poweredBy": true },
  "limits": { "maxDestinations": 0, "maxCohosts": 0, "maxBitrateKbps": 0 }
}
```

- `mode` is `live` or `test`, from the key.
- `features` always has all 16 keys ([Tiers, feature gates and branding](tiers-branding.md)).
- `branding`: Extras `{ false, false, true }`, Engage `{ true, true, true }`, Live as agreed (default all false).
- `limits`: Live `{ 3, 3, 4500 }`; the other tiers `0` (no such feature).

### POST /v1/partner/auth/token

Exchanges a user token from your backend for an EveryCord session. 30 a minute per network.

```json
{ "userToken": "ecut_...", "platform": "ios", "deviceName": "Sam's iPhone", "appVersion": "4.2.0" }
```

Only `userToken` is required. The answer is an AuthResult:

```json
{ "accessToken": "eyJ...", "refreshToken": "rt_...", "expiresIn": 3600, "user": { "id": "u...", "username": "maker42" }, "isNewUser": true }
```

Access tokens live 60 minutes; refresh tokens live 60 days and rotate on every use (`POST /v1/auth/refresh`). The SDKs
refresh for you.

Errors: `401 unauthorized` when the token is from another partner or mode, used, or more than 5 minutes old;
`403 account_suspended`.

### POST /v1/partner/purchases

Sent by the SDKs for each purchase of your EveryCord-powered products they see in your app (the earnings cross-check).
120 a minute per network.

```json
{ "productId": "pro_monthly", "transactionId": "2000000812345678", "price": 4.99, "currency": "USD", "storefront": "US", "purchasedAt": "2026-10-22T18:00:00.000Z", "store": "apple" }
```

`201 { "recorded": true }`, or `200 { "recorded": false }` for a transaction already recorded. `store` defaults from the
app's platform (`apple`, or `google` on Android); `transactionId` is the store's transaction or order ID (1 to 200 of
`A-Z a-z 0-9 . _ : -`); `storefront` a 2 or 3 letter country code; `purchasedAt` within the last year (default now).
Test-mode purchases are kept apart and never counted.

### Sign in with Apple, Google or an email code

`POST /v1/auth/apple`, `POST /v1/auth/google`, `POST /v1/auth/email/start` and `POST /v1/auth/email/verify` work as
for EveryCord's own apps, with the partner headers. An account created this way is attributed to your app.

## Server routes (secret key)

### PUT /v1/partner/users/:externalUserId

Creates or updates the EveryCord account linked to your user. `externalUserId` is 1 to 128 of `A-Z a-z 0-9 _ . : -`.
600 a minute.

```json
{ "username": "maker42", "displayName": "Maker", "avatarUrl": "https://cdn.example.com/u/42.jpg" }
```

All fields are optional. `201` when created, `200` when updated:

```json
{
  "userId": "u...",
  "externalUserId": "acct:42",
  "username": "maker42",
  "displayName": "Maker",
  "avatarUrl": "https://cdn.example.com/u/42.jpg",
  "createdAt": "2026-10-22T18:00:00.000Z"
}
```

- `username` and `displayName` follow EveryCord's usual rules; a taken username is `409 conflict`. Without a username,
  the account gets a temporary `userNNNNNNNN` handle and the person picks one later.
- `avatarUrl` must be `https`. It is stored and returned; EveryCord does not copy it into the account's avatar.
- Send `If-None-Match: *` to create only: `409 external_user_exists` when the ID is already linked.

### POST /v1/partner/users/:externalUserId/token

Mints a single-use sign-in token for the app, valid for 5 minutes. Only its hash is stored.

```json
{ "userToken": "ecut_...", "expiresAt": "2026-10-22T18:05:00.000Z" }
```

`404 not_found` for an unknown `externalUserId`.

### DELETE /v1/partner/users/:externalUserId

`204`. Deletes the EveryCord account exactly as a person deleting their own account does (clips, messages and follows
go; Sparks balances are zeroed with the ledger kept). `404` when unknown.

### GET /v1/partner/usage

`?from=YYYY-MM-DD&to=YYYY-MM-DD`. Daily usage of your apps (removed ones included) in the key's mode, oldest first.
`to` defaults to today (UTC) and `from` to 29 days before it; at most 92 days. 60 a minute.

```json
{
  "items": [
    {
      "appId": "px...", "platform": "ios", "bundleId": "com.clipwise.app", "mode": "live", "day": "2026-10-21",
      "liveSeconds": 86400, "viewerSeconds": 1296000, "segments": 43200, "uploadBytes": 9876543210,
      "cohostSeconds": 7200, "relaySeconds": 3600, "activeUsers": 412, "newUsers": 37,
      "sparksSent": 15000, "sparksValueCents": 9800
    }
  ]
}
```

| Field | Meaning |
|---|---|
| `liveSeconds` | seconds of partner streams within the day |
| `viewerSeconds` | each sampled minute's viewers times 60 |
| `segments`, `uploadBytes` | direct-upload segments committed, every rendition |
| `cohostSeconds` | co-hosts from joining to leaving |
| `relaySeconds` | EveryCord Relay video of your users' relays |
| `activeUsers` | accounts with a signed-in request from your app that day |
| `newUsers` | accounts attributed to your app that day |
| `sparksSent` | Sparks sent in your streams |
| `sparksValueCents` | what those Sparks were worth to EveryCord, in US cents |

Today's and yesterday's rows are recomputed before answering; older days are final.

### GET /v1/partner/live

`?status=live|ended&cursor=&limit=`. Your streams in the key's mode, newest first.

```json
{
  "items": [
    {
      "id": "lv...", "appId": "px...", "userId": "u...", "externalUserId": "acct:42", "title": "Ranked grind",
      "status": "live", "listed": false, "viewerCount": 120, "peakViewerCount": 214,
      "startedAt": "2026-10-22T17:02:11.000Z", "endedAt": null
    }
  ],
  "nextCursor": "..."
}
```

`listed` is true when the stream is on EveryCord's Live Now.

### POST /v1/partner/webhooks

```json
{ "url": "https://api.example.com/everycord/webhooks", "events": ["live.started", "live.ended"] }
```

`201`:

```json
{ "id": "pw...", "url": "https://api.example.com/everycord/webhooks", "events": ["live.started", "live.ended"], "mode": "live", "createdAt": "2026-10-22T18:00:00.000Z", "secret": "whsec_..." }
```

The secret is shown only here. `url` is `https` on a public host name. At most 10 webhooks per partner and mode
(`409`). See [Webhooks](webhooks.md).

### GET /v1/partner/webhooks

`{ "items": [Webhook] }`, without secrets.

### GET /v1/partner/webhooks/:id

The webhook and its last 50 deliveries:

```json
{
  "id": "pw...", "url": "https://api.example.com/everycord/webhooks", "events": ["live.ended"], "mode": "live",
  "createdAt": "2026-10-22T18:00:00.000Z",
  "deliveries": [
    {
      "id": "pd...", "eventId": "pe...", "type": "live.ended", "status": "delivered", "attempts": 1, "lastStatus": 204,
      "lastError": null, "nextAttemptAt": null, "createdAt": "2026-10-22T18:00:01.000Z", "deliveredAt": "2026-10-22T18:00:01.000Z"
    }
  ]
}
```

### DELETE /v1/partner/webhooks/:id

`204`. Pending deliveries fail; the delivery log stays.

### POST /v1/partner/webhooks/:id/test

`202 { "eventId": "pe..." }`: a `test` event to that webhook only.

### POST /v1/partner/revenue-reports

Your monthly revenue from EveryCord-powered features that the stores do not report (web sales, ads), after store fees:
the base of the revenue share.

```json
{ "month": "2026-09", "revenueCents": 123456, "currency": "USD", "notes": "Web subscriptions" }
```

`201`:

```json
{
  "id": "pr...", "month": "2026-09", "revenueCents": 123456, "currency": "USD", "notes": "Web subscriptions",
  "shareBps": 2000, "shareCents": 24691, "mode": "live", "createdAt": "2026-10-22T18:00:00.000Z"
}
```

- `month` is not in the future; `revenueCents` a whole number from 0; `currency` an ISO 4217 code.
- `shareBps` is your share rate when reported (1000, 2000 or 3000 basis points by tier, or your agreement's);
  `shareCents` is `floor(revenueCents * shareBps / 10000)`.
- A later report for the same month and currency replaces the earlier one in what is owed (all are kept).
- Test-mode reports are kept but never counted.

## Earnings routes (secret key)

How earnings are measured is in [Earnings tracking and payment terms](earnings.md). Test and live secret keys both work
here (store data is never test data). Amounts are micro-units of `currency` (1,000,000 = 1.00).

### PUT /v1/partner/earnings/apple

Connects App Store Connect: an API key with the Finance role (Sales and Reports at least). 10 a minute.

```json
{ "p8": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----", "keyId": "ABCDE12345", "issuerId": "<uuid>", "vendorNumber": "85012345", "role": "finance" }
```

The `.p8` (PKCS#8 PEM, at most 4 KB) is stored encrypted and never returned; a key with the same vendor number replaces
the earlier one. The check runs before the answer: EveryCord looks up each of your Apple apps by bundle ID
(`GET /v1/apps?filter[bundleId]=` in the App Store Connect API) and requests one daily Sales and Trends report. A
malformed key is `400` with the reason.

```json
{
  "connection": {
    "id": "ps...", "store": "apple", "keyId": "ABCDE12345", "issuerId": "<uuid>", "vendorNumber": "85012345", "role": "finance",
    "status": "ok",
    "checks": [{ "check": "app:com.clipwise.app", "ok": true, "message": "Found com.clipwise.app." }],
    "checkedAt": "2026-10-22T18:00:00.000Z", "lastPullAt": null, "lastSuccessAt": null, "lastError": null,
    "createdAt": "2026-10-22T18:00:00.000Z"
  },
  "joined": true,
  "newKeys": [{ "kind": "publishable", "mode": "live", "appId": "px...", "key": "ecpk_live_..." }]
}
```

`status` is `unchecked`, `ok` or `error`; a failed check's `message` says what to fix. When a check verifies an app of
a dashboard sign-up that accepted the Partner Terms, the partnership becomes active and its live keys are issued:
`joined` is true and `newKeys` holds them, shown only in this answer.

### PUT /v1/partner/earnings/google

Connects Google Play reports: `{ "bucketId": "pubsite_prod_rev_..." }` (a `gs://` address is accepted). Invite
EveryCord's service account in Play Console with "View financial data" first. The answer has the same shape; the check
lists the bucket's reports and looks for each of your Android package names in the newest one.

### GET /v1/partner/earnings/connections

`{ "items": [Connection] }`. `DELETE /v1/partner/earnings/connections/:id` answers `204`: pulls stop, pulled rows stay.

### GET and PUT /v1/partner/earnings/products

The EveryCord-powered product IDs (App Store) and SKUs (Google Play); only their report rows count. `PUT` replaces the
list (at most 500; `appId` must be one of your apps on that store).

```json
{ "products": [{ "store": "apple", "productId": "pro_monthly", "appId": "px..." }, { "store": "google", "productId": "pro_monthly" }] }
```

### GET /v1/partner/earnings

`?month=YYYY-MM` (default this month):

```json
{
  "month": "2026-10",
  "items": [{ "day": "2026-10-21", "store": "apple", "source": "apple_report", "productId": "pro_monthly", "appId": "px...", "currency": "USD", "units": 12, "grossMicros": 59880000, "proceedsMicros": 41916000, "usdMicros": 41916000 }],
  "totals": [{ "store": "apple", "currency": "USD", "units": 12, "proceedsMicros": 41916000, "shareBps": 2000, "shareMicros": 8383200 }],
  "sdk": [{ "store": "apple", "productId": "pro_monthly", "currency": "USD", "purchases": 12, "priceMicros": 59880000, "reportedUnits": 12, "gap": 0 }],
  "selfReported": [{ "month": "2026-09", "revenueCents": 123456, "currency": "USD", "notes": "Web subscriptions", "createdAt": "2026-10-01T12:00:00.000Z" }]
}
```

`grossMicros` is null when the store priced in another currency; `usdMicros` is set for US dollar rows. `sdk` compares
the purchases the SDK saw with the units the store reported.

### GET /v1/partner/statements

```json
{
  "items": [
    {
      "id": "pt...", "store": "apple", "month": "2026-09", "currency": "USD", "proceedsMicros": 1250000000, "shareBps": 2000,
      "shareMicros": 250000000, "payoutDate": "2026-10-30", "dueDate": "2026-11-29", "status": "issued",
      "paidMicros": null, "paidOn": null, "paidReference": null, "issuedAt": "2026-10-31T12:00:00.000Z"
    }
  ]
}
```

`status` is `draft`, `issued`, `paid`, `overdue` or `disputed`. See [Statements and payment terms](earnings.md#statements-and-payment-terms).

## Moderation routes (secret key, early access)

Read only, every tier. Reports about your users and your streams (never the reporter's identity or words), restricted
users, EveryCord's moderation actions and your users' chat bans. See [Moderation for partners](moderation.md) for the
fields.

| Route | What |
|---|---|
| `GET /v1/partner/moderation/reports?status=&targetType=&cursor=&limit=` | reports, newest first, with `reason`, `status` and `outcome` |
| `GET /v1/partner/moderation/restrictions?status=suspended\|banned\|all&cursor=` | your users suspended or banned now, and until when |
| `GET /v1/partner/moderation/actions?from=&to=&cursor=` | EveryCord's moderation actions on your users and content, at most 92 days per request |
| `GET /v1/partner/moderation/chat-bans?hostExternalUserId=&cursor=` | bans and timeouts in your users' channels |

## Tier gates on EveryCord routes

With the partner headers, these EveryCord routes need the feature's tier (`403 partner_tier` otherwise):

| Routes | Feature | Tier |
|---|---|---|
| `/v1/messages/*` | `direct_messages` | Extras |
| `/v1/drops/*`, `POST /v1/live/:id/drops/beat` | `drops` | Extras |
| `/v1/sparks/*`, `/v1/boosts/*`, `/v1/video-requests/*`, `POST /v1/live/:id/sparks` | `sparks` | Engage |
| `/v1/overlays*`, `/v1/live/:id/overlay` | `overlays` | Engage |
| `POST /v1/clips`, `POST /v1/recordings/:id/clips`, `/v1/me/recordings*` | `clips` | Engage |
| `/v1/relays*`, `/v1/relay/*` | `bonding` | Engage |
| `POST /v1/live` with EveryCord delivery, `/v1/live/:id/ingest/*` | `live_delivery` | Live |
| `/v1/live/:id/cohosts*`, `/v1/live/:id/cohost-links*`, `/v1/cohost-links/*`, `POST /v1/live/:id/room` | `cohosts` | Live |
| `/v1/live/:id/chat` (socket), `/v1/live/:id/messages*`, `/v1/live/:id/bans*`, `/v1/live/:id/moderators/*`, `/v1/live/:id/reactions` | `chat` | Live |

Every other route works at every tier: accounts, profiles, follows, blocking, search, feeds, watching clips and Past
streams, reports and support. `watch_capture`, `messages_share`, `apple_platforms`, `captions`, `multistream` and
`capture` have no server route; the SDKs enforce them from your configuration.

A `POST /v1/live` that only sends to your own destinations (no EveryCord delivery) works at every tier, so Sparks, Drops
and clips can hang on it. It takes `"postToLiveNow": true` to list a live-mode stream on EveryCord's Live Now.
