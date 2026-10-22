# Earnings tracking and payment terms

EveryCord's share is a percentage of what you earn from the EveryCord-powered features in your app, after app store
fees. Instead of relying only on numbers you report, EveryCord reads your earnings from the stores' own reports, checks
them against the purchases the SDK sees, and sends you a statement each month.

> **Early access.** Earnings tracking turns on with the Partner API. Write to
> [services@symaro.com](mailto:services@symaro.com) with questions. The routes are in the
> [API reference](api-reference.md#earnings-routes-secret-key).

## What counts

You mark which of your products are EveryCord-powered: the in-app purchase product IDs (App Store) and SKUs (Google
Play) whose value comes from EveryCord features, for example a "Pro streaming" subscription that unlocks multistreaming.
Only those products count toward the share. Mark them in the [partner dashboard](dashboard.md) or with
`PUT /v1/partner/earnings/products`.

## App Store

1. In App Store Connect, under **Users and Access > Integrations > App Store Connect API**, create a team key with the
   **Finance** role (financial reports and payments). **Sales and Reports** is the minimum; Finance also gives the
   payment dates that set the due date.
2. Download the `.p8` file (Apple lets you download it once), and note the **Key ID**, the **Issuer ID** and your
   **vendor number** (Payments and Financial Reports).
3. Upload them in the partner dashboard (or with `PUT /v1/partner/earnings/apple`). The `.p8` is stored encrypted, never
   logged and never shown again.

EveryCord checks the key right away: it must see your app's bundle ID and be able to request a Sales and Trends report.
Then, once a day, EveryCord reads the daily Sales summary reports of the last 4 days for your apps: units, and proceeds
(units times your developer proceeds, in the proceeds currency). A failed pull keeps the earlier numbers and shows the
error in the dashboard.

## Google Play

1. The partner dashboard shows EveryCord's Google Cloud service account email.
2. In Play Console, under **Users and permissions**, invite that email with **View financial data** for your app.
3. Enter your reports bucket ID (`pubsite_prod_...`, from **Download reports > Financial**) in the dashboard.

EveryCord checks that it can read the bucket and that your package name appears in the newest report. Once a day it then
reads the monthly earnings reports of this month and last: per SKU and day, units (charges less refunds), gross
(charges) and proceeds (after Google's fees and refunds; tax rows are left out).

## The SDK cross-check

The SDKs watch for purchases of your EveryCord-powered products in your app (StoreKit 2 transaction updates on iOS, Play
Billing purchase updates on Android) and send each one to `POST /v1/partner/purchases`: product ID, transaction ID,
price, currency, storefront and time. EveryCord removes duplicates by transaction ID and flags gaps against the store reports.
The SDK never sees or sends card details or the buyer's Apple Account or Google account.

## Revenue the stores do not report

Web sales, ads and other revenue from EveryCord-powered features: report it once a month with
`POST /v1/partner/revenue-reports` ([API reference](api-reference.md#post-v1partnerrevenue-reports)). Without store
connections, your share is billed from self-reported numbers and the SDK cross-check.

## What is stored

Per day, store, app and product: units, gross, proceeds (after store fees), currency and, for US dollar rows, the US
dollar value, with the report it came from (`apple_report` or `google_report`). The SDK's purchases and your
self-reported revenue sit beside them. You see your own numbers in the dashboard and with
`GET /v1/partner/earnings?month=YYYY-MM`.

## Statements and payment terms

Each month EveryCord makes a statement per store and currency:

- the calendar month (UTC),
- the proceeds of your EveryCord-powered products, from the store's reports,
- EveryCord's share at your tier,
- the date the store paid you (EveryCord records it from the store's payment),
- the **due date: 30 days after the store paid you**.

A draft follows the reports until it is issued; a statement is issued only once the payout date is known.

**You pay EveryCord's share within 30 days of receiving the money from Apple** (or from Google, for Google Play).

| Status | Meaning |
|---|---|
| draft | being prepared |
| issued | sent to you, with its due date |
| paid | paid, with the amount, date and reference |
| overdue | past the due date and unpaid |
| disputed | you raised a question about it |

See your statements in the dashboard or with `GET /v1/partner/statements`. If a statement stays unpaid well past its
due date (15 days by default, or as your agreement says), EveryCord may pause your partnership; that is always a
person's decision, never automatic, and you hear from us first.

## Questions about a statement

Write to [services@symaro.com](mailto:services@symaro.com) with the statement's month and store. A disputed statement is
not overdue while we look at it together.
