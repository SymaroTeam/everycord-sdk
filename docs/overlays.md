# Overlays

Name banners, badges, follower and subscriber counts, Sparks goals, recent supporters, chat and the viewer count,
drawn into the video on the phone so every destination shows them: your viewers, YouTube, Twitch and SRT alike.

Feature `overlays`, Engage tier and up (routes `/v1/overlays*` and `/v1/live/:id/overlay`).

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## How it works

- Each creator has one overlay setup: up to 8 widgets, saved on EveryCord. Creators pick templates and fill in fields;
  nothing a creator sends is ever run as code.
- With `overlays: true`, the broadcast extension (iOS) or the capture service (Android) draws the widgets into the video
  before it is encoded, and reloads them within a few seconds when the creator saves a change.
- Widget text passes EveryCord's language filter: a save with objectionable words is refused (`400 inappropriate`).

## Widgets

| Type | Shows | Fields | At most |
|---|---|---|---|
| `lower_third` | a name banner | title (the name, 1 to 40), subtitle (0 to 60) | 2 |
| `badge` | a short label | title (1 to 24) | 2 |
| `follower_count` | followers | title (the label, 0 to 24), source `everycord`, `youtube` or `twitch` | 1 |
| `subscriber_count` | subscribers | title, source `everycord`, `youtube` or `twitch` | 1 |
| `sparks_goal` | progress to a Sparks goal | target (10 to 10,000,000 Sparks); shown while the stream takes Sparks | 1 |
| `recent_supporters` | the latest people who sent Sparks | title | 1 |
| `chat` | the latest chat messages | title | 1 |
| `viewer_count` | people watching | title | 1 |

Every widget has an `id` (1 to 32 of letters, digits, `_` and `-`), an `anchor` (`top_left`, `top_right`,
`bottom_left`, `bottom_right`), a `theme` (`brand`, `dark`, `light`) and `visible`.

## Read and save the setup

```swift
let overlays = session.overlays
var widgets = try await overlays.widgets()
widgets.append(EveryCordOverlayWidget(id: "name", type: "lower_third", anchor: .bottomLeft,
                                      title: "Maker", subtitle: "Ranked grind, day 12"))
try await overlays.save(widgets)       // the stream shows the change within a few seconds
```

On Android, `session.overlays` (when `overlays = true`) reloads the creator's widgets with `refresh()`, and adds one
thing iOS does not have yet: your app's own picture over the whole video.

```kotlin
val overlays = session.overlays ?: return
overlays.setImage(myOverlayBitmap)     // scaled to the video; transparent pixels show the screen
overlays.setImage(null)                // removes it
overlays.refresh()
```

Your own picture is drawn under the EveryCord mark when your tier's branding draws it.

## The API

- `GET /v1/overlays` returns `{ revision, widgets, updatedAt, templates, pollSeconds }`. `templates` describe each
  type's fields, so you can build an editor from them.
- `PUT /v1/overlays` with `{ widgets, revision }` saves the setup; a save from an older copy is refused with
  `409 revision_changed`.

Both take the creator's access token and the partner headers.
