# Moderation for partners

You can see the moderation that concerns your app: reports about your users and your streams, who is suspended or
banned, the chat bans in your users' channels, and every action EveryCord's moderators took. It is read only and comes
with every tier, in the Partner API and on the Moderation page of the [partner dashboard](dashboard.md).

> **Early access.** The moderation routes, webhooks and dashboard page are in early access; their exact shapes are set
> when they ship. Write to [services@symaro.com](mailto:services@symaro.com) with questions.

## Who decides

EveryCord's moderators review every report and decide what happens: remove the content, suspend the account for a
while, ban it, or dismiss the report. You see the outcome. You cannot resolve a report or lift a restriction yourself;
to ask for a review, use "Ask EveryCord to review" in the dashboard or write to
[services@symaro.com](mailto:services@symaro.com) with the report or user ID. You can always delete one of your users
with `DELETE /v1/partner/users/:externalUserId`.

**What you see.** Your users are the accounts created through your apps or linked by your backend. Your content is the
clips, comments, chat messages and streams of your users, and anything in a stream started from your app. A test key
sees test-mode data only.

**What you never see.** Who made a report, the reporter's own words, and the copy of the reported content EveryCord
keeps as evidence. This protects the people who report, and that copy can hold other people's content.

## Reports

`GET /v1/partner/moderation/reports?status=open|resolved|dismissed&targetType=&cursor=&limit=`, newest first:

```json
{
  "items": [
    {
      "id": "rp...",
      "targetType": "message",
      "targetId": "cm...",
      "subject": { "userId": "u...", "externalUserId": "acct:42", "username": "maker42" },
      "sessionId": "lv...",
      "reason": "harassment",
      "status": "resolved",
      "outcome": "content_removed",
      "createdAt": "2026-10-22T18:00:00.000Z",
      "resolvedAt": "2026-10-22T18:20:00.000Z"
    }
  ],
  "nextCursor": null
}
```

| Field | Values |
|---|---|
| `targetType` | `user`, `clip`, `comment`, `message` (a chat message), `live` (a stream) |
| `reason` | `spam`, `harassment`, `hate`, `sexual`, `violence`, `self_harm`, `impersonation`, `other` |
| `status` | `open`, `resolved`, `dismissed` |
| `outcome` | `none`, `content_removed`, `suspended`, `banned` |

`subject` is the reported account (null when it was deleted); `sessionId` is the stream the report came from, when
there was one.

## Suspended and banned users

`GET /v1/partner/moderation/restrictions?status=suspended|banned|all&cursor=`:

```json
{
  "items": [
    { "userId": "u...", "externalUserId": "acct:42", "username": "maker42", "status": "suspended",
      "until": "2026-10-29T18:00:00.000Z", "reason": "harassment", "at": "2026-10-22T18:20:00.000Z" }
  ],
  "nextCursor": null
}
```

`until` is when a suspension ends (null for a ban). `reason` is the category the person was shown. A restricted
account cannot use EveryCord features in your app until the restriction ends; the SDK signs it out with a sentence to
show.

## Moderation actions

`GET /v1/partner/moderation/actions?from=YYYY-MM-DD&to=YYYY-MM-DD&cursor=`: what EveryCord's moderators did to your users
and your content (suspend, ban, lift, content removed, report dismissed), at most 92 days per request. Moderators'
names and private notes are not included.

```json
{
  "items": [
    { "id": "ma...", "action": "content_removed", "targetType": "clip", "targetId": "cl...", "userId": "u...",
      "externalUserId": "acct:42", "reason": "sexual", "at": "2026-10-22T18:20:00.000Z" }
  ],
  "nextCursor": null
}
```

## Chat bans

Hosts and their moderators ban and time out people in their own chats ([Chat and moderation](chat.md)).
`GET /v1/partner/moderation/chat-bans?hostExternalUserId=&cursor=` lists the bans in your users' channels:

```json
{
  "items": [
    { "hostUserId": "u...", "hostExternalUserId": "acct:42", "userId": "u...", "externalUserId": null,
      "username": "troll99", "until": null, "bannedBy": "moderator", "createdAt": "2026-10-22T18:05:00.000Z" }
  ],
  "nextCursor": null
}
```

`until` is null for a permanent ban, or when a timeout ends. `bannedBy` is `host` or `moderator`.

## Webhooks

| Type | When | `data` |
|---|---|---|
| `report.created` | someone reports one of your users or something in your streams | the report item above |
| `report.resolved` | EveryCord resolves or dismisses that report | the report item above |
| `user.restricted` | EveryCord suspends or bans one of your users | `userId`, `externalUserId`, `status`, `until`, `at` |
| `user.unrestricted` | a suspension is lifted or ends, or a ban is lifted | `userId`, `externalUserId`, `previousStatus`, `at` |

See [Webhooks](webhooks.md) for signatures and retries.

## The dashboard's Moderation page

Four tabs, each filterable and exportable as CSV: **Reports** (open, resolved, dismissed), **Restricted users**
(suspended and banned, and until when), **Chat bans**, and the **Actions** log. Every row has "Ask EveryCord to review".
