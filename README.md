# Waracle Store — Monorepo

One codebase, three linked components plus UI. Every part runs **independently**, and they **link together** through a shared REST API and a common product catalogue.

```
WaracleStore/
├── backend/   REST API + Swagger docs   |  MySQL data layer
├── web/       React + Vite web UI (runs standalone, or against the backend)
└── mobile/    Expo / React Native app (same brand + catalogue as web)
```

## Architecture

- **API (backend/):** an Express REST API documented with Swagger UI.
  Exposes auth, products, cart pricing and orders. Interactive docs at
  `http://localhost:4000/api/docs`.
- **MySQL (backend/):** a MySQL data layer (`users`, `products`,
  `orders`). Selected with `DB_DRIVER=mysql`. A zero-setup in-memory driver is the default so the API also runs with no database at all.
- **Mobile (mobile/):** an Expo React Native app mirroring the web UI
  (same palette, tagline, values and 9-product catalogue). Reads from the backend when `EXPO_PUBLIC_API_URL` is set, otherwise uses a bundled sample catalogue.
- **Web (web/):** runs fully standalone with an in-browser mock API, or against the real backend when `VITE_API_URL` is set.

The **same 9 products** and the **same pricing rules** are used by the API, the web app and the mobile app, so totals and catalogue match everywhere.

## Prerequisites

* Node.js: v22.0.0 or higher (Tested on v26.7.0)
* npm: v8.6.0 or higher (Tested on v11.19.0)
* Git: Required for version control
* Web Browser: Chrome (latest version) recommended for the web UI
* Expo Go: Required for mobile testing, not required otherwise

Docker (Optional): Required only if running the real MySQL database instance natively

## Launching the Application

Installation and execution are handled by root-level launch scripts. You do not need to manually install NPM packages or run individual dev servers.

**1. Launch the Full Stack**
To install all dependencies across workspaces and concurrently start the Backend, Web UI, and Mobile app, run:

```bash
./launch-all.sh
```

**2. Launch Web Only**
To install dependencies and start only the React + Vite web UI:

```bash
./launch-web.sh
```

Note: The web app runs on http://localhost:5173 using a standalone mock API by default. To use the real backend, ensure you have set VITE_API_URL=http://localhost:4000 in web/.env.local prior to launching.

**3. Launch Mobile Only**
To install dependencies and start only the Expo React Native app:

```bash
./launch-mobile.sh
```

## Database Configuration
By default, the backend uses a zero-setup in-memory driver. If you want to use the real MySQL data layer:

Start a MySQL instance. For a quick Docker setup, you can use:

```bash
docker run --name waracle-mysql -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=waracle_store -p 3306:3306 -d mysql:8
```
In backend/.env, set `DB_DRIVER=mysql` alongside your `DB_*` connection variables.

Run `./launch-all.sh` (or start the backend via your preferred method). The API automatically creates and seeds the tables on boot.
---

# QA Automation Assessment — WS-101 Coupon Codes

## Overview

This assessment evaluates the Coupon Codes release in the Waracle Store Android application using native mobile UI automation.

The objective is to verify the release against acceptance criteria AC-1 to AC-6, including positive, negative, edge-case and end-to-end checkout scenarios.

**Framework:** Espresso with Kotlin and JUnit4
**Platform:** Android
**Test device:** Pixel 8 Android Emulator, Android 16 (API 36)
**Test package:** `com.anonymous.waraclestore`

## Running the Android Automation Tests

### Prerequisites

- Android Studio and Android SDK
- An available Android emulator, with a running Android virtual device
- JDK 17
- Node.js and npm
- Gradle wrapper included in `mobile/android`

The Android project under `mobile/android` contains the native instrumentation tests. The tests run on an installed Android application using Espresso, rather than inside Expo Go.

### Setup

From the repository root:

```bash
npm install
```

On macOS, select JDK 17 if another Java version is the default:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Start an Android emulator using Android Studio, then navigate to the Android project:

```bash
cd mobile/android
```

The debug configuration packages the React Native JavaScript bundle into the Android application, allowing the instrumentation tests to run without requiring a separate Metro session.

### Execute the test suite

```bash
./gradlew :app:connectedDebugAndroidTest
```

The complete suite currently contains tests expected to fail because of confirmed release defects. A failing Gradle result is therefore not necessarily an automation infrastructure failure.

To execute a single test:

```bash
./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.anonymous.waraclestore.CouponTest#invalidCouponIsRejectedWithoutDiscount'
```

Android instrumentation reports are generated under:

`mobile/android/app/build/reports/androidTests/connected/debug/`

## Test Structure

The native automation is located under:

`mobile/android/app/src/androidTest/java/com/anonymous/waraclestore/`

- `LaunchTest.kt` — application launch and navigation smoke testing.
- `ShoppingFlow.kt` — reusable shopping flow for adding a product to the cart.
- `CouponTest.kt` — coupon validation, pricing, shipping and checkout tests.

The shared shopping helper reduces duplicated navigation logic and keeps individual tests focused on their respective assertions.

## Requirements Coverage

| Acceptance Criterion | Coverage | Observed Result |
|---|---|---|
| AC-1: Apply coupon from cart | Coupon entry and application flows | Coupon application interaction works |
| AC-2: WARACLE25 reduces subtotal by 25% | Single-item and two-item positive tests | FAIL — incorrect discount |
| AC-3: £5 shipping for non-empty basket | Shipping calculation test | PASS |
| AC-4: Subtotal minus discount plus shipping | Discounted and undiscounted total tests | PASS without coupon; FAIL with coupon |
| AC-5: Invalid or empty coupon rejected | Invalid-code and empty-code tests | PASS |
| AC-6: Confirmation displays coupon, discount and final total | End-to-end checkout automation and manual verification | FAIL — missing coupon/discount and incorrect total |

The checkout journey itself has been verified through login, shipping details, demo payment fields, order submission and confirmation navigation.

## Defect Findings

### BUG-001: Incorrect percentage discount

**Severity:** High
**Affected criteria:** AC-2 and AC-4

**Steps to reproduce:**
1. Add one Men's Logo T-Shirt (£24.99) to the cart.
2. Enter `WARACLE25`.
3. Apply the coupon.
4. Review the discount and total.

**Expected:**
- Subtotal: £24.99
- Discount (25%): £6.25
- Shipping: £5.00
- Total: £23.74

**Actual:**
- Discount: £0.25
- Total: £29.74

The pricing implementation assigns a fixed £0.25 discount instead of calculating 25% of the subtotal. This also affects larger baskets.

The automated positive-path tests deliberately assert the expected requirement values rather than accepting the incorrect implementation values.

### BUG-002: Order confirmation omits coupon information

**Severity:** Medium
**Affected criterion:** AC-6

**Steps to reproduce:**
1. Add a product to the cart.
2. Apply `WARACLE25`.
3. Proceed through login and checkout.
4. Submit the demo order.
5. Review the confirmation screen.

**Expected:**
The order confirmation displays the applied coupon, discount and final total.

**Actual:**
The confirmation displays the final amount but does not display the applied coupon code or discount breakdown.

The automated checkout test successfully reached the confirmation screen before failing its assertion that `WARACLE25` should be displayed. The incorrect total is linked to BUG-001 rather than being treated as a separate pricing defect.

## Automation Design Decisions

### Stable selectors

React Native `testID` values were used with Espresso's `withTagValue()` matcher where appropriate.

An initial placeholder-based selector for `John Doe` matched multiple inputs and caused an `AmbiguousViewMatcherException`. Android's view hierarchy confirmed that `testID="checkout_name_input"` was exposed as the native view tag.

The selector was replaced with the stable, purpose-specific identifier. Subsequent executions passed.

This approach reduces ambiguity and avoids relying on presentation text that may be duplicated or changed.

### Synchronisation

Initial React Native rendering required additional synchronisation before Espresso could interact with the home screen.

A temporary fixed delay helped identify the timing issue, but the final shared navigation helper uses a bounded readiness wait rather than an unconditional sleep.

Intermittent failures finding the `All Products` heading were also observed. Repeated successful runs followed without changes to the checkout selectors. This remains a test-reliability concern and should not be misclassified as a confirmed coupon defect.

### Independent assertions

Tests were separated to distinguish shipping calculations, undiscounted totals and discounted totals.

The discounted-total test asserts the expected final amount directly rather than first asserting the already-known incorrect discount message. This allows AC-4 to be evaluated independently of AC-2.

### Retaining legitimate failing tests

Tests exposing confirmed product defects have not been weakened merely to produce a successful build.

Expected and actual results are documented separately, with failures retained as regression evidence for the acceptance criteria.

## Release Assessment

**Recommendation: Do not release WS-101 in its current state.**

The coupon feature does not satisfy the required 25% discount calculation, resulting in customers receiving less discount than advertised. The order confirmation also omits information explicitly required by AC-6.

Before release:
1. Correct the discount calculation.
2. Ensure the checkout and confirmation use the corrected totals.
3. Display the applied coupon and discount on the confirmation screen.
4. Rerun the affected automated tests and verify the results.
5. Investigate the intermittent shopping-navigation test failure to improve suite reliability.

The automated suite provides regression coverage for the principal coupon scenarios while preserving evidence of the current release defects.
