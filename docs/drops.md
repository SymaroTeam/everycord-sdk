# Drops

Viewers earn in-game item codes by watching a creator's stream. Creators or game studios upload the codes; there is no
purchase and no chance involved, and the items have no money value. Feature `drops`, Extras tier and up (routes
`/v1/drops/*` and `POST /v1/live/:id/drops/beat`).

> **Early access.** Drop cards in the SDKs' player are in early access; until then, call the routes with the user's
> access token. Write to [services@symaro.com](mailto:services@symaro.com) to join.

## How a drop works

1. A creator (or a game studio working with them) creates a drop: the item's name and description, the game and its age
   rating, how long viewers must watch (15 to 240 minutes, in steps of 5), the minimum viewer age (13, 16 or 18), the
   regions, and the start and end (1 hour to 30 days).
2. The creator uploads the codes: Apple offer codes or the publisher's own item codes, one per line or the first CSV
   column, up to 10,000 per upload. At least 10 codes are needed to submit.
3. EveryCord reviews every drop before it starts. An approved drop starts and ends on time.
4. While the creator is live, viewers who watch long enough in EveryCord's player claim a code. Each code goes to one
   viewer.
5. Creators see totals only (claims, codes left); numbers under 5 show as "fewer than 5".

## Rules

- Creators are 18 or older, in good standing, with an account old enough for drops.
- Item names and descriptions cannot mention currency or Sparks.
- A code already used in another drop, or found leaked, refuses the whole upload (`422 codes_in_use`).
- Viewers report a code that does not work (`code_invalid`, `code_used`); repeated reports pause the drop for review.
- Only watch time in EveryCord's own player counts.

## Routes

| Route | What |
|---|---|
| `GET /v1/drops/eligibility` | whether the creator can make drops, and why not |
| `GET /v1/drops/terms` | the drop terms and their version |
| `GET /v1/drops/mine` | the creator's drops |
| `POST /v1/drops` | creates a draft |
| `GET`, `PATCH /v1/drops/:id` | reads or changes a draft (or one sent back for changes) |
| `POST /v1/drops/:id/codes` | uploads codes (`text/plain`, `text/csv` or JSON) |
| `POST /v1/drops/:id/submit` | sends it for review with the accepted terms version |
| `POST /v1/drops/:id/pause`, `/resume`, `/end`, `/extend` | runs it (extend once, up to 14 days) |
| `GET /v1/drops/:id/analytics` | totals |
| `POST /v1/drops/claims/:claimId/report` | a viewer reports a code |

All take the user's access token and the partner headers.
