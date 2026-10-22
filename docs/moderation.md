# Moderation for partners

You can see the moderation that concerns your app: reports about your users and your streams, who is suspended or
banned, the chat bans in your users' channels, and every action EveryCord's moderators took. It is read only and comes
with every tier, in the Partner API and on the Moderation page of the [partner dashboard](dashboard.md).

> **Early access.** Moderation visibility turns on with the Partner API. Write to
> [services@symaro.com](mailto:services@symaro.com) with questions.

## Who decides

EveryCord's moderators review every report and decide what happens: remove the content, suspend the account for a
while, ban it, or dismiss the report. You see the outcome. You cannot resolve a report or lift a restriction yourself;
to ask for a review, use "Ask EveryCord to review" in the dashboard or write to
[services@symaro.com](mailto:services@symaro.com) with the report or user ID. You can always delete one of your users
with `DELETE /v1/partner/users/:externalUserId`.

**What you see.** Your users are the accounts created through your apps or linked by your backend. Your content is
your users' accounts, clips, comments and messages, and the streams started from your apps with their chat. A test key
sees test-mode data only.

All four routes take your secret key, list newest first, and page with `cursor` and `limit` (1 to 50).

**What you never see.** Who made a report, the reporter's own words, and the copy of the reported content EveryCord
keeps as evidence. This protects the people who report, and that copy can hold other people's content.

## Reports

`GET /v1/partner/moderation/reports?status=open|resolved|dismissed&targetType=&cursor=&limit=`: reports about one of
your users, or about one of your streams or a chat message in it.

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
| `targetType` | `user`, `clip`, `comment`, `message` (a chat message), `live` (a stream), `dm` (a direct message) |
| `reason` | `spam`, `harassment`, `hate`, `sexual`, `violence`, `self_harm`, `impersonation`, `other` |
| `status` | `open`, `resolved`, `dismissed` |
| `outcome` | `none`, `content_removed`, `suspended`, `banned` |

`subject` is the reported account (its `username` is null once the account is deleted). `sessionId` is set for `live`
and `message` reports. `outcome` is what EveryCord did when it closed the report; a dismissed report is `none`.

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

Your users who are suspended (until a time in the future) or banned now. `until` is when a suspension ends (null for a
ban). `reason` is the category of the report the restriction came from, or null when it did not come from a report
(moderators' own notes stay private). A restricted account cannot use EveryCord features in your app until the
restriction ends; the SDK signs it out with a sentence to show.

## Moderation actions

`GET /v1/partner/moderation/actions?from=YYYY-MM-DD&to=YYYY-MM-DD&cursor=`: what EveryCord's moderators did to your users
and your content, the last 30 days by default and at most 92 days per request. `action` is `suspend`, `ban`, `lift`,
`suspension_ended`, `content_removed` or `report_dismissed`; `reason` is the report's category when the action was
taken on a report. Moderators' names and private notes are not included.

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
`GET /v1/partner/moderation/chat-bans?hostExternalUserId=&cursor=` lists the bans and timeouts in force in your users'
channels:

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

Moderation events are found by a background pass, so they arrive within about 15 seconds. See
[Webhooks](webhooks.md) for signatures and retries.

## The dashboard's Moderation page

At `everycord.net/partners/dashboard/moderation`, for live or test mode: **Reports** (open, resolved, dismissed),
**Restricted users** (suspended and banned, and until when), **Chat bans** and the **Actions** log, 50 rows each, each
exportable as CSV (up to 1,000 rows). Every row has "Ask EveryCord to review".
