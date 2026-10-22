# Chat and moderation

Live chat beside every stream, with reactions, moderators, bans and timeouts, slow mode, followers-only chat, a
language filter and the host's own blocked words. Feature `chat`, Live tier.

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Show the chat

Signed in, people can send; signed out, the chat is read-only.

```swift
// The host's own session:
ChatView(chat: session.chat)

// Any session, for viewers:
let chat = EveryCordChat(sessionID: sessionID)
try chat.connect()                      // reconnects on its own until disconnect()
try await chat.send("GG")
try await chat.react("heart")           // heart, fire, clap or star
chat.disconnect()
```

`EveryCordChat` is an `ObservableObject` with `messages` (oldest first, up to 500), `connectionState`, `viewerCount`
and `lastError` (a sentence to show when a send is refused: slow mode, a ban, signed out).

```kotlin
val chat = EveryCord.chat(sessionId)
chat.connect()                          // reconnects with backoff until close()
lifecycleScope.launch { chat.messages.collect { adapter.submitList(it) } }
lifecycleScope.launch {
    chat.events.collect { event ->
        when (event) {
            is ChatEvent.Error -> showError(event.message)   // retryAfter for slow mode
            is ChatEvent.Viewers -> viewers.text = "${event.count} watching"
            else -> Unit
        }
    }
}
chat.send("GG")                         // false when not connected or signed out
chat.react("fire")
chat.close()
```

A chat message has the sender, their `role` (`host`, `cohost`, `moderator` or `viewer`), the text and the time. A
Sparks gift carries its amount, and its caption ends with "Sparks sent via EveryCord" when your tier's branding
requires it ([Sparks](sparks.md)).

## Moderation

The host and the host's moderators can remove messages and ban or time out people. Moderators belong to the host's
channel, so they moderate every stream of that host.

```kotlin
chat.deleteMessage(messageId)
chat.ban(userId)                        // a permanent ban from the host's chats
chat.ban(userId, seconds = 600)         // a timeout, 60 to 86,400 seconds
```

The routes, with the host's or moderator's access token:

| Route | Who | What |
|---|---|---|
| `DELETE /v1/live/:id/messages/:messageId` | host, moderators | removes a message |
| `POST /v1/live/:id/bans` with `{ userId, durationSeconds? }` | host, moderators | bans (no duration) or times out |
| `DELETE /v1/live/:id/bans/:userId` | host, moderators | lifts a ban |
| `GET /v1/live/:id/bans` | host | the banned people and until when |
| `PUT` and `DELETE /v1/live/:id/moderators/:userId` | host | adds or removes a moderator |
| `PATCH /v1/live/:id` | host | `chatEnabled`, `slowModeSeconds` (0 to 120), `followersOnlyChat`, `blockedWords` (up to 50) |

## Safety built in

- **Language filter.** Objectionable words in chat are masked (`***`). Hosts add their own blocked words.
- **Slow mode.** The host's setting, or an automatic floor in big rooms. A refused send answers `429 slow_mode` with
  `retryAfter`.
- **Blocking.** People who blocked each other do not see each other's messages.
- **Reports.** Viewers report messages and people; EveryCord's moderators review reports and can suspend or ban
  accounts. Your backend hears about it through the `user.restricted` [webhook](webhooks.md).
- Suspended and banned accounts cannot chat, and their messages are hidden.

Your backend and the partner dashboard see the reports, restrictions and chat bans that concern your app: see
[Moderation for partners](moderation.md).

Follow EveryCord's [Community guidelines](https://everycord.net/guidelines) in your app's chat, and link to them where
people report.
