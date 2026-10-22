# Accounts and sign-in

Every person who streams, chats, sends Sparks or makes clips through your app has an EveryCord account. You choose how
they get one: linked to your own user IDs from your backend (the usual way), or with Sign in with Apple, Google or an
email code. Accounts come with every tier (feature `accounts`).

> **Early access.** Code samples are illustrative until the 1.0.0 release. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## Your users, linked from your backend

This flow keeps sign-in inside your app: the person never sees an EveryCord sign-in screen.

### 1. Create or update the account

From your backend, with your secret key. Your own user ID is the `externalUserId` (1 to 128 characters of letters,
digits and `_ . : -`):

```
PUT /v1/partner/users/acct:42
Authorization: PartnerSecret ecsk_live_...
Content-Type: application/json

{ "username": "maker42", "displayName": "Maker" }
```

The answer is `201` when the account was created and `200` when it was updated. Without a `username`, the account gets
a temporary `userNNNNNNNN` handle and the person picks one later.

### 2. Mint a sign-in token

When the person opens the EveryCord features in your app:

```
POST /v1/partner/users/acct:42/token
Authorization: PartnerSecret ecsk_live_...
```

```json
{ "userToken": "ecut_...", "expiresAt": "2026-10-22T18:05:00.000Z" }
```

The token is single use and lasts 5 minutes. Send it to your app over your own authenticated connection.

### 3. Sign in with the SDK

The SDK exchanges the token (`POST /v1/partner/auth/token`) for the usual EveryCord access and refresh tokens and keeps
the session in the Keychain (iOS) or encrypted storage (Android).

```swift
let user = try await EveryCord.auth.signIn(userToken: token)
```

```kotlin
val user = EveryCord.auth.signIn(token)
```

The token must come from the same partner and mode (live or test) as the app's publishable key, be unused and be less
than 5 minutes old; otherwise the exchange answers `401 unauthorized`. A suspended or banned account gets
`403 account_suspended`.

**Deleting a user.** `DELETE /v1/partner/users/:externalUserId` deletes the EveryCord account exactly as a person
deleting their own account does: clips, messages and follows go, and Sparks balances are zeroed with the ledger kept.
Call it when the person deletes their account with you.

**Create only.** Send `If-None-Match: *` with the `PUT` to create without updating: an `externalUserId` that is already
linked answers `409 external_user_exists`.

## Sign in with Apple, Google or an email code

These work as they do in EveryCord's own apps, with the partner headers. A new account made this way is attributed to
your app.

```swift
// Sign in with Apple: put the nonce's SHA-256 in the request, then pass the raw nonce.
let nonce = EveryCordAuth.appleNonce()
request.nonce = nonce.sha256
// ...after ASAuthorizationController succeeds:
let user = try await EveryCord.auth.signInWithApple(
    identityToken: identityToken, rawNonce: nonce.raw,
    authorizationCode: authorizationCode, fullName: credential.fullName)

// Google: an ID token whose audience is EveryCord's client ID.
let user = try await EveryCord.auth.signInWithGoogle(idToken: idToken)

// Email: a 6-digit code.
try await EveryCord.auth.startEmailSignIn(email: "maker@example.com")
let user = try await EveryCord.auth.verifyEmail(email: "maker@example.com", code: "123456")
```

```kotlin
val user = EveryCord.auth.signInWithGoogle(idToken)

EveryCord.auth.startEmailSignIn("maker@example.com")
val user = EveryCord.auth.verifyEmail("maker@example.com", "123456")
```

`isNewUser` is true right after the sign-in that created the account, so you can show your own onboarding.

## The signed-in user

```swift
EveryCord.auth.currentUser          // EveryCordUser? (id, username, displayName, avatarURL)
await EveryCord.auth.signOut()      // signs out here and revokes the session on the server
```

```kotlin
EveryCord.auth.currentUser          // StateFlow<EveryCordUser?>
EveryCord.auth.signedOut            // SharedFlow: the server ended the session (a reason, or null)
EveryCord.auth.signOut()
```

On iOS, `EveryCord.userDidChangeNotification` is posted when the user signs in or out.

## Profiles, follows and blocking

Partner users have ordinary EveryCord profiles at `everycord.net/u/<username>`, with followers, following and blocking.
Your app reads and changes them through the EveryCord API routes for profiles, follows and blocks, with the partner
headers and the user's access token. They work at every tier.

A person suspended or banned by EveryCord's moderators cannot call the API; your backend gets a `user.restricted`
[webhook](webhooks.md) with the status and, for a suspension, when it ends.

## Direct messages

One-to-one conversations between two signed-in accounts, with read receipts, typing and push when the other person is
away (feature `direct_messages`, Extras tier and up, routes `/v1/messages/*`).

- **You can only message people who follow you.** Once someone has written to you, you can always reply.
- Blocked accounts (either way) cannot see or send in their conversation; suspended and banned accounts are hidden.
- `canSend` on a conversation tells your app whether to enable the composer, and `cantSendReason` says why not.

The SDKs' direct message views are in early access; until then, call the routes with the user's access token.

## Who counts as your user

- An account created through your app (by your backend, or by a sign-in in your app with `isNewUser: true`) is
  attributed to your app and counted in `newUsers` in your [usage](api-reference.md#get-v1partnerusage).
- An existing EveryCord account that signs in through your app is not attributed to you, but its requests from your
  app count in `activeUsers` for the day.
