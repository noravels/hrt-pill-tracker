# Sync and account model research

Last updated: 2026-05-19

This document captures product requirements around optional account sync for mobile and required login for web.

## Product requirement

Mobile apps:

- Android and iOS must work fully local-first without an account.
- Users may optionally connect an account for backup/sync.
- Android should support Google account sign-in.
- iOS should support Apple account sign-in.
- Optional account connection should not block core tracking features.

Web app:

- Login should be required.
- Without login, web cannot reliably keep medication tracking state across browsers/devices and would be easy to lose.
- Web can still use the same core domain model, but persistence should be remote/account-scoped.

## Architecture implications

Core SDK should remain auth-agnostic and network-agnostic.

Core should not know:

- Google Sign-In
- Sign in with Apple
- OAuth tokens
- backend APIs
- user accounts
- cloud sync providers

Core should know only repository interfaces:

```text
TreatmentRepository
MedicationRepository
DoseEventRepository
SettingsRepository
```

The app layer chooses repository implementations:

```text
Mobile offline mode:
  Local repositories only

Mobile signed-in mode:
  Local repositories + sync engine

Web mode:
  Remote/account-scoped repositories, possibly with browser cache later
```

## Recommended sync shape

Use a local-first model for mobile:

```text
UI -> Core service -> Local repository -> Local DB
                         |
                         v
                     Sync engine -> Remote API
```

The sync engine should be outside core. It observes local changes and reconciles with the remote backend when signed in.

Recommended local data metadata for future sync:

```text
id: stable UUID
createdAt
updatedAt
deletedAt nullable      # tombstone for sync deletes
syncStatus              # localOnly | pendingUpload | synced | conflict
serverVersion optional
```

Do not add sync to MVP unless needed, but design IDs/timestamps now so sync can be added without migrations that rewrite everything.

## Account identity

Supported identity providers:

```text
Provider
  - google
  - apple
```

Mobile account connection:

```text
AccountConnection
  provider
  providerUserId
  email optional
  displayName optional
  linkedAt
```

Avoid storing raw provider tokens in core data. Token storage is platform/backend responsibility.

## Backend options

Potential backend choices:

### Option A: Supabase

Pros:

- Auth supports Google and Apple OAuth.
- Postgres database and row-level security.
- Works well for web login requirement.
- Mobile SDKs exist.
- Fast MVP.

Cons:

- Adds backend dependency.
- Need careful privacy/security and RLS rules.

### Option B: Firebase

Pros:

- Strong Google sign-in support.
- Apple sign-in supported.
- Good mobile SDKs.
- Offline persistence options.

Cons:

- Data model less relational.
- Apple ecosystem and web auth flows can still require careful setup.

### Option C: Custom backend

Pros:

- Maximum control.
- Can expose exactly the repository API needed by the core wrappers.

Cons:

- More work: auth, token verification, database, migrations, security, hosting.

Initial recommendation: Supabase is likely the fastest fit for required web login + optional mobile sync, while preserving local-first mobile behavior.

## Web login requirement

Web should start with an auth gate:

```text
Unauthenticated:
  show landing/login page

Authenticated:
  load account-scoped treatment data from remote repository
  run core calculations locally in browser using fetched data
  save changes through remote repository
```

The web app can later add an offline cache, but login remains required.

## Mobile local-first behavior

Mobile first launch:

```text
- User can create a treatment plan without signing in.
- Data is stored locally.
- App can schedule local notifications.
- Settings show optional “Back up / sync with account”.
```

When user connects account:

```text
- Existing local data is uploaded to remote account.
- Future changes are written locally first.
- Sync engine uploads changes in background.
- If user signs out, app should ask whether to keep local data on device.
```

## Conflict policy for later

MVP can avoid complex multi-device conflict resolution. Future sync should define:

- last-write-wins for simple fields
- event-level merge for dose events
- tombstones for deletes
- conflict UI only if needed

Dose events are naturally append/update records, so they are easier to merge than mutable summaries. This reinforces the decision to derive taken count from dose events.

## Privacy notes

Medication tracking data is sensitive health-related data. If remote sync is added:

- use HTTPS only
- use provider auth and secure token handling
- enforce per-user row-level access
- avoid public analytics containing medication names or dose timings
- consider encrypted backups later
- include clear privacy copy before enabling sync

## Updated repo architecture implication

```text
src/lib
  pure core SDK, no auth/network

src/android
  local repositories
  optional Google sign-in
  optional sync engine

src/ios
  local repositories
  optional Sign in with Apple
  optional sync engine

src/web
  login required
  remote repositories
  bridge to shared core model

src/backend or infra later
  auth provider integration
  remote repository API/database
```
