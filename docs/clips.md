# Clips and replays

Every stream sent through EveryCord's delivery is kept in the cloud as it happens, so the replay and clips are ready
as soon as the stream ends. Feature `clips`, Engage tier and up (routes `POST /v1/clips`,
`POST /v1/recordings/:id/clips` and `/v1/me/recordings*`). Watching clips and Past streams works at every tier.

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Replays (Past streams)

- The same 2-second segments that play live make the cloud copy (a **recording**), kept up to 12 hours per stream.
- The recording is private to the host. The host can post it to their profile as a public **Past stream**, with its
  own page at `everycord.net/live/<id>` and a view count.
- `EveryCordPlayerView` plays a session's replay once it has ended.

The host lists their recordings with `GET /v1/me/recordings` (status `recording`, `processing`, `ready` or `failed`;
`pastStream.state` `private`, `posting`, `public` or `removed`).

## Clips

A clip is 5 to 180 seconds of a stream, cut on the server from whole segments, with no re-encoding. It gets its own page
and a share link that opens at the right moment.

```swift
// The last 30 seconds so far, while live, or any range of a finished stream.
let clip = try await EveryCord.clips.create(fromSession: session.id, startSeconds: 95, durationSeconds: 30,
                                            title: "Clutch round")
print(clip.shareURL as Any)

let page = try await EveryCord.clips.list(userID: user.id)       // newest first
let more = try await EveryCord.clips.list(userID: user.id, cursor: page.nextCursor)
let one = try await EveryCord.clips.clip(id: clip.id)
```

```kotlin
// Leave out startSeconds for the last durationSeconds recorded so far ("clip that").
val clip = EveryCord.clips.create(sessionId, durationSeconds = 30, title = "Clutch round")
val page = EveryCord.clips.list(userId)
```

**Who may clip.** The host, from their own stream in any state but failed or removed; anyone signed in, from a public
Past stream they can see.

**Limits.** 20 clips a day per person (`429` past it), 10 clip requests a minute. Cutting can take up to a few minutes.
Errors: `404 recording_range` when the range holds too little video, `409 recording_failed`,
`409 removed_by_moderator`, `422 quick_clip_failed` when the cut is not a playable clip.

## Uploaded clips

People can also upload a video as a clip (`POST /v1/clips`, multipart, MP4 or MOV in H.264 or HEVC). Clips have likes,
comments, view counts and a page at `everycord.net/c/<id>`.

## Webhook

Your backend gets `clip.created` when a partner user makes a clip ([Webhooks](webhooks.md)).
