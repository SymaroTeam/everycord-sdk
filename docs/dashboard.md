# Partner dashboard

Sign up, add your apps, prove you own them, connect your store reports and get your keys, all at
[everycord.net/partners](https://everycord.net/partners). When your app passes its checks, you are joined automatically:
no waiting for an EveryCord admin.

> **Early access.** The partner dashboard opens with the Partner API. Until then, EveryCord sets up early-access partners
> by hand: write to [services@symaro.com](mailto:services@symaro.com).

## 1. Sign up

- At `everycord.net/partners/signup`, enter your company name, website, your name and work email, and pick a tier:
  Extras, Engage or Live ([Tiers, feature gates and branding](tiers-branding.md)).
- Accept the Partner Terms (at `everycord.net/partners/terms`). The version you accepted and when are recorded.
- Confirm your email with a 6-digit code (valid 10 minutes). Your partnership starts as pending: test keys work, live
  keys come when an app is confirmed (step 4).
- Sign in later at `everycord.net/partners` with a code sent to your email; a session lasts 7 days (24 hours idle).
- The owner invites team members by email (up to 25).

## 2. Add your apps

Add each app by platform and identifier: the bundle identifier for iOS, macOS and visionOS, or the package name for
Android. **Test keys** (a test publishable key for the app, and a test secret key if you have none) are issued at once,
so you can integrate before anything is checked. To remove an app, write to EveryCord.

## 3. Prove ownership and connect earnings

**iOS, macOS and visionOS.** Upload an App Store Connect API key: the `.p8` file (at most 4 KB), its Key ID, Issuer ID
and your vendor number. EveryCord checks it right away:

- it signs a token with the key and looks up your bundle ID (`GET /v1/apps?filter[bundleId]=...` in the App Store
  Connect API): the app must be in that account;
- it requests a Sales and Trends report: the key needs the Finance or Sales and Reports role.

**Android.** The page shows EveryCord's service account email. Add it in Play Console with **View financial data**, and
enter your reports bucket ID. EveryCord checks that it can read the bucket and that your package name appears in the
reports.

Each check shows its result. A failed check says exactly what to fix, for example:

- "This key can't see com.example.app"
- "Key needs the Finance or Sales and Reports role"
- "We can't read gs://pubsite_prod_..."

See [Earnings tracking and payment terms](earnings.md) for what the store connections are used for.

## 4. Joined automatically

When an app's ownership check passes and the terms are accepted, EveryCord:

- issues the **live keys** (a live publishable key per confirmed app and one live secret key), shown once on the page,
- marks your partnership active at the tier you chose,
- starts the daily earnings pulls,
- records it in its audit log.

EveryCord can still pause a partnership, change a tier or end a partnership at any time under the Partner Terms.

## 5. The dashboard

| Page | What you do there |
|---|---|
| Overview | your status, tier, share and branding |
| Apps and keys | create, roll and revoke keys; secret keys are shown once |
| Store connections | status, last pull, errors, replace a key or bucket |
| Earnings and statements | earnings by month, statements and their due dates |
| Usage | the daily usage table and a 30-day chart |
| Webhooks | add, test and see the delivery log |
| Moderation | reports about your users and streams, suspended and banned users, chat bans and EveryCord's actions ([Moderation for partners](moderation.md)) |
| Team | invite and remove members |
| Docs | these pages |

Keys and `.p8` files are never shown again after you create or upload them. Roll a key whenever you think it was
exposed.

## Safety

The dashboard has its own sign-in session, protection against cross-site requests, and rate limits on sign-up, code
entry and checks. Uploads accept only `.p8` files in the expected PEM shape, stored encrypted.
