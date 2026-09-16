# Gut Tracker — Tech Design

Personal, single-user Android app for tracking food/drink intake, daily medication, wellbeing rating, and digestion rating. Not for distribution — built for one person, on one phone.

**Status:** implemented and verified end-to-end. Backend is live at `https://gut-tracker-gules.vercel.app`. The Android app builds (`./gradlew assembleDebug` in `android/`) and every core flow — logging, medication, rating save, edit/delete, one-off items, Overview charts — was tap-tested on an emulator with real syncing to production confirmed via direct API checks, then the test data was cleaned out. Nothing has been committed to git yet.

## Feature set

**Core loop**
- Quick-tap shortlist for drinks (Beer, Sparkling Wine, Whiskey, Cocktail, Coffee) and food (Sausage, Brettljause, Noodles, Gulasch, Toast, Müsli, Salad, Vegetarian, Restaurant), each entry timestamped. "Vegetarian" and "Restaurant" are deliberately kept abstract as fast catch-alls rather than one entry per specific dish.
- The **drinks list is closed** — exactly those 5, no "add new" affordance, no growth expected there. Only the food shortlist is extensible.
- The shortlist grid is **icon-only, no text labels** — each default item gets a small hand-drawn pictogram on a colored tile (recognized by shape/position rather than read as text), which keeps the grid compact and calm as the list grows past a handful of items. Custom items added later (see below) don't get a bespoke icon and fall back to a letter-monogram tile instead — the grid ends up a mix of real icons (curated defaults) and initials (anything you add). The "Logged today" list always stays fully text-labeled regardless, since precision matters more there than on the fast-tap grid.
- "Add new" flow for food off the shortlist (drinks are closed, see above), distinguishing two cases: a **one-off** (the common case — logged once, never saved as a shortlist item) and **save to my shortlist** (an unchecked-by-default checkbox on the same form, for the rarer thing you expect to eat again). Since anything logged this way is always food, the form has no category toggle — it's implicitly `'food'`.
- No quantity field on entries — since every tap is individually timestamped, "2 coffees" is just two separate taps (two entries), which also keeps each one independently editable/deletable instead of needing to fix a shared count. (Supersedes the earlier +/- stepper idea — redundant once every entry has its own timestamp.)
- Every entry is individually timestamped (no meal-slot bucketing — mealtimes aren't regular, so the raw timestamp is what matters for spotting delayed reactions).
- Any entry (and the daily fields below) can be edited after the fact — time, quantity, or deleted — to support logging retroactively.
- One-tap "medication taken" toggle for the single daily medication — stamps the time, tappable again to undo.
- Daily **wellbeing rating**, 1 (Super Bad) – 10 (Perfect Day) — named "wellbeing" rather than "pain," since higher is better here (and for digestion below), which is the opposite of how a conventional pain scale runs (higher = worse) and would read backwards under that name.
- Daily digestion rating, 1 (no digestion) – 5 (perfect digestion).
- Daily context tag — single-select, shortlist starting with "Home" (default) and "Vacation", extensible the same way food/drink items are. Adding a new tag reuses the same bottom-sheet pattern as adding a food item, simplified further: just a name field and a save button — no category toggle (tags aren't categorized) and no one-off/shortlist checkbox (every tag you bother adding is inherently meant to be reused, unlike a one-off food item).
- Daily free-text note (optional) — for anything a tag can't capture (stress, travel, illness).

**Supporting features**
- Frequency-sorted shortlists — both the food/drink items and the day-context tags sort by use count, so the quick-tap list stays fast as it grows.
- Two independent reminder notifications: medication (default ~12:00 noon) and wellbeing rating (default ~20:00), each firing only if that day's field is still unset.
- Trend view: wellbeing and digestion each get a line chart over 7/30/90 days (same visual format for both — no reason for one to be a bar chart and the other a line, they're the same kind of daily rating), with the date ranges where `context_tag = 'Vacation'` shaded as a background band behind both charts (one "Vacation" legend swatch, under the wellbeing chart, covers both since they sit directly adjacent) — a plain visual cue, no analysis attached.

**Explicitly out of scope for now** (data is in a real database if wanted later; just not built into the app today):
- Symptom tags (bloating/cramping/etc.) — the symptom itself doesn't vary, only its severity, which the wellbeing rating already captures.
- Trigger/pattern surfacing — no in-app correlation logic for now; that analysis happens offline, directly against the data, later.
- Streak counters / gamification — plain reminders only, no engagement mechanics.
- In-app CSV/export — the data lives in Neon, so it's queryable directly (`psql`, a SQL client, a script) whenever it's actually needed; no export UI until that stops being enough.
- Per-entry notes — notes live at the daily level only.
- Meal photos, sleep/stress logging.

## Architecture

```
Android app (Kotlin, Jetpack Compose)
  Room (local cache + write queue)
  WorkManager (background sync + 2 daily reminders)
        |
        | HTTPS, Bearer token
        v
Next.js API routes on Vercel
  Drizzle ORM
        |
        v
Neon (serverless Postgres)
```

Mirrors the stack already used in `world-cup-app` (Next.js + Vercel + Neon + Drizzle), swapping that project's NextAuth session auth for a single bearer token, since this app has exactly one user and no browser login screen.

## Backend

**Stack:** Next.js (App Router) API routes, deployed on Vercel. Neon serverless Postgres as the database, accessed through `@neondatabase/serverless` + Drizzle ORM. Schema pushed with `drizzle-kit push`, same as world-cup-app — no hand-written SQL migrations.

**Schema (Drizzle / Postgres):**

```
items
  id            serial, pk
  name          text
  category      text            -- 'food' | 'drink' (no alcohol/non-alcohol distinction)
  use_count     integer, default 0
  last_used_at  timestamp

entries
  id            serial, pk
  timestamp     timestamp        -- editable after creation
  type          text            -- 'food' | 'drink'
  item_id       integer, fk -> items.id, nullable  -- set when logged from the shortlist, or saved as a new one
  label         text, nullable                     -- set for a one-off (item_id null); exactly one of item_id/label is set

tags
  id            serial, pk
  name          text            -- e.g. 'Home', 'Vacation'
  use_count     integer, default 0
  last_used_at  timestamp

daily_logs
  date                   date, pk  -- 'YYYY-MM-DD'
  medication_taken_at    timestamp, nullable
  wellbeing_rating       integer   -- 1-10 (1 = Super Bad, 10 = Perfect Day)
  wellbeing_logged_at    timestamp
  digestion_rating       integer   -- 1-5
  digestion_logged_at    timestamp
  context_tag_id         integer, fk -> tags.id, default = 'Home' tag
  notes                  text, nullable
```

**Seed data:** the default `items` (5 drinks, 9 food) and `tags` ("Home", "Vacation") rows get inserted by a one-off seed script (`scripts/seed.ts`, same pattern as world-cup-app's seed scripts), run once against Neon during setup — not hardcoded into the app.

**API routes:** REST endpoints under `/api` — `items` (list, create — create only hit when "save to my shortlist" is checked, not for a plain one-off; always `category: 'food'` since the drinks list is closed), `tags` (list, create), `entries` (list by single `date` or a `from`/`to` range — the range form exists for the Android app's sync to pull the Overview screen's 7/30/90-day windows in one request instead of looping per-day, create, update, delete), `daily-logs/[date]` (get by date, upsert) and `daily-logs` (`from`/`to` range list, same sync purpose).

**Auth:** a single long-lived bearer token, checked in middleware on every request. No NextAuth, no login screen, no per-user rows — appropriate for exactly one user talking to their own API. The token is generated once and used unchanged by both sides below.

**Secrets — never committed to git**, matching how world-cup-app keeps `DATABASE_URL` out of the repo (`.gitignore` excludes `.env*`):
- **Vercel side:** the bearer token and `DATABASE_URL` live only as Vercel environment variables and in a local `.env.local`. `.env*` stays in `.gitignore`.
- **Android side:** the token lives in a git-ignored `local.properties` (or `gradle.properties`) on the dev machine, injected into the app as a `BuildConfig` field at compile time — the Android equivalent of `.env.local`. It's baked into the personal APK at build time; the properties file itself is never committed. `local.properties` / `*.properties` with secrets get added to `.gitignore` alongside the usual Android entries when the repo is set up.

## Android app

**Stack:** Kotlin, Jetpack Compose, Retrofit/OkHttp for the REST client.

**Offline-first sync:** the app writes every tap to a local Room database immediately, so logging works with no signal. A WorkManager job periodically pushes unsynced rows to the Vercel API and pulls down anything new. This replaces the offline persistence Firestore would have given for free — the real cost of moving off Firebase, and the main new piece of plumbing in this design.

**Reminders:** two independent daily WorkManager jobs, each checking the relevant field on today's `daily_logs` row (via the local Room cache) and firing a local notification if it's still unset — medication around noon, wellbeing rating in the evening (~20:00). Fully on-device, no push infrastructure. Exact cutoff times should be easy to tweak later (a small settings screen or just constants for now).

**Trends/analytics:** computed client-side from data pulled from the API (dataset is small — one person's daily entries). Line chart of wellbeing/digestion over 7/30/90 days. No trigger/pattern logic in-app (see Explicitly out of scope).

**Distribution:** no Play Store. Signed APK built in Android Studio, installed via USB debugging or sideload.

## Visual design

Mockups published here: https://claude.ai/artifact/4awogwAcrBJtwgw7q245aD — updated to match the current feature set: medication toggle and rating reminder cards on Today, "Wellbeing" (not "Pain") as the 1-10 scale's label throughout, the day-context tag and notes field on the rating screen, the icon-only shortlist grid (including Salad, spaghetti-only noodles, bowl-free salad), the closed drinks list (no "add new" tile), the food-only add-new sheet with its "save to my shortlist" checkbox, matching line-chart treatment for both wellbeing and digestion trends with vacation-period shading on both, the edit-entry sheet (date, time, delete), no quantity field anywhere (repeat taps are just separate timestamped entries), and no streak pill or pattern-surfacing card.

The day-tag "add new" flow (tapping "+ Add" next to Home/Vacation) isn't a separate mockup — it's a trivially simplified version of the food add-new sheet (name field + save, no category toggle, no checkbox), described above rather than drawn again.

## Open decisions

- Exact API route shape (REST vs. a couple of batched endpoints) — fine to settle during implementation rather than upfront.
- Reminder cutoff times (noon / 8pm) are defaults, not confirmed as final — fine to adjust once the app is actually in use.
