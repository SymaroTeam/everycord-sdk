# Webhooks

EveryCord calls your backend when something happens to your app's streams and users: a stream starts or ends, a clip is
made, Sparks arrive, or a user is suspended or banned. Every call is signed with your webhook's secret.

> **Early access.** The Partner API is in early access. Write to [services@symaro.com](mailto:services@symaro.com) to
> join.

## Set one up

From your backend, with your secret key, or in the [partner dashboard](dashboard.md):

```
POST /v1/partner/webhooks
Authorization: PartnerSecret ecsk_live_...
Content-Type: application/json

{ "url": "https://api.example.com/everycord/webhooks", "events": ["live.started", "live.ended", "sparks.received"] }
```

```json
{
  "id": "pw...",
  "url": "https://api.example.com/everycord/webhooks",
  "events": ["live.started", "live.ended", "sparks.received"],
  "mode": "live",
  "createdAt": "2026-10-22T18:00:00.000Z",
  "secret": "whsec_..."
}
```

The `secret` is shown only in this answer: store it with your other secrets. The URL must be `https` on a public host
name (no IP addresses, no `localhost`, no user name or password in it). Each partner has up to 10 webhooks per mode, and
a test key's webhooks get only test-mode events.

Send yourself a `test` event at any time with `POST /v1/partner/webhooks/:id/test`.

## Events

| Type | When | `data` |
|---|---|---|
| `live.started` | a partner stream starts (right when it is created) | `sessionId`, `appId`, `userId`, `externalUserId`, `listed`, `startedAt` |
| `live.ended` | a partner stream ends | `sessionId`, `appId`, `userId`, `externalUserId`, `startedAt`, `endedAt`, `peakViewers` |
| `clip.created` | a partner user makes a clip | `clipId`, `userId`, `externalUserId`, `appId`, `title`, `sessionId`, `createdAt` |
| `sparks.received` | Sparks are sent in a partner stream | `sendId`, `sessionId`, `appId`, `recipientUserId`, `recipientExternalUserId`, `senderUserId`, `sparks`, `test`, `createdAt` |
| `user.restricted` | EveryCord suspends or bans a partner user | `userId`, `externalUserId`, `status` (`suspended` or `banned`), `until`, `at` |
| `test` | you asked for one | `message`, `webhookId` |
| `report.created` | someone reports one of your users or something in your streams | the report, as in [Moderation for partners](moderation.md#reports) |
| `report.resolved` | EveryCord resolves or dismisses that report | the report |
| `user.unrestricted` | a suspension is lifted or ends, or a ban is lifted | `userId`, `externalUserId`, `previousStatus`, `at` |

`externalUserId` is your own user ID, or `null` for an EveryCord account your backend did not create. Events other than
`live.started` are found by a background pass, so they arrive within about 15 seconds.

## The request

```
POST /everycord/webhooks HTTP/1.1
Content-Type: application/json
User-Agent: EveryCord-Webhooks/1.0
EveryCord-Event: live.ended
EveryCord-Delivery: pd...
EveryCord-Signature: t=1792692000,v1=5f2b...

{"id":"pe...","type":"live.ended","createdAt":"2026-10-22T18:00:00.000Z","data":{"sessionId":"lv...","appId":"px...","userId":"u...","externalUserId":"acct:42","startedAt":"2026-10-22T17:02:11.000Z","endedAt":"2026-10-22T18:00:00.000Z","peakViewers":214}}
```

The body's `id` is the event's ID, the same in every retry: use it to ignore an event you already handled.

## Verify the signature

`EveryCord-Signature` holds `t`, the Unix time in seconds when the request was signed, and `v1`, the hex HMAC-SHA256 of
`<t>.<raw body>` with the whole `whsec_...` secret as the key (UTF-8). To verify:

1. Read the raw request body as bytes, before any JSON parsing.
2. Compute the HMAC over `t`, a dot, and the raw body, and compare it with `v1` in constant time.
3. Reject a `t` more than 5 minutes away from your clock, so an old request cannot be replayed.

### Node.js

```js
import crypto from 'node:crypto';
import express from 'express';

const SECRET = process.env.EVERYCORD_WEBHOOK_SECRET; // whsec_...

export function verifyEveryCordSignature(rawBody, header, secret, toleranceSeconds = 300) {
  if (typeof header !== 'string') return false;
  const fields = {};
  for (const part of header.split(',')) {
    const index = part.indexOf('=');
    if (index > 0) fields[part.slice(0, index).trim()] = part.slice(index + 1).trim();
  }
  const t = Number(fields.t);
  if (!Number.isInteger(t) || !fields.v1) return false;
  if (Math.abs(Date.now() / 1000 - t) > toleranceSeconds) return false;
  const expected = crypto.createHmac('sha256', secret).update(`${t}.`).update(rawBody).digest();
  const received = Buffer.from(fields.v1, 'hex');
  return received.length === expected.length && crypto.timingSafeEqual(received, expected);
}

const app = express();

// express.raw keeps the body as a Buffer, exactly as it was signed.
app.post('/everycord/webhooks', express.raw({ type: 'application/json' }), (req, res) => {
  if (!verifyEveryCordSignature(req.body, req.get('EveryCord-Signature'), SECRET)) {
    return res.status(400).send('Bad signature');
  }
  const event = JSON.parse(req.body.toString('utf8'));
  // Handle event.type and event.data; ignore an event.id you already handled.
  res.sendStatus(204);
});

app.listen(3000);
```

### Python

```python
import hashlib
import hmac
import json
import os
import time

from flask import Flask, abort, request

SECRET = os.environ["EVERYCORD_WEBHOOK_SECRET"]  # whsec_...


def verify_everycord_signature(raw_body: bytes, header: str | None, secret: str, tolerance: int = 300) -> bool:
    if not header:
        return False
    fields = {}
    for part in header.split(","):
        key, sep, value = part.partition("=")
        if sep:
            fields[key.strip()] = value.strip()
    try:
        t = int(fields["t"])
        received = fields["v1"]
    except (KeyError, ValueError):
        return False
    if abs(time.time() - t) > tolerance:
        return False
    expected = hmac.new(secret.encode("utf-8"), f"{t}.".encode("utf-8") + raw_body, hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, received)


app = Flask(__name__)


@app.post("/everycord/webhooks")
def everycord_webhook():
    raw_body = request.get_data()  # the raw bytes, as they were signed
    if not verify_everycord_signature(raw_body, request.headers.get("EveryCord-Signature"), SECRET):
        abort(400)
    event = json.loads(raw_body)
    # Handle event["type"] and event["data"]; ignore an event["id"] you already handled.
    return "", 204
```

## Answer quickly

Answer with any `2xx` within 10 seconds, then do the slow work afterwards. Redirects are not followed and count as
failures. A delivery that fails is retried after 1 minute, 5 minutes, 30 minutes, 2 hours and 12 hours, then marked
failed. Events are kept for 30 days.

## Manage webhooks

| Route | What |
|---|---|
| `GET /v1/partner/webhooks` | your webhooks in the key's mode |
| `GET /v1/partner/webhooks/:id` | one webhook and its last 50 deliveries (`status` `pending`, `delivered` or `failed`, `attempts`, `lastStatus`, `lastError`, `nextAttemptAt`) |
| `DELETE /v1/partner/webhooks/:id` | deletes it; pending deliveries fail, the log stays |
| `POST /v1/partner/webhooks/:id/test` | sends a `test` event to that webhook only |

The [partner dashboard](dashboard.md) shows the same delivery log.
