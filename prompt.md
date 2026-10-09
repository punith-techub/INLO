# Prompt Engineering & AI-Assisted Development Log (`prompt.md`)
**Project:** INLO ⚡ (Your Local-First AI Notification Co-Pilot)  
**Repository:** [punith-techub/INLO](https://github.com/punith-techub/INLO)  
**Hackathon:** Vibe Coding Hackathon 2026  
**License:** Open Source  

---

## 1. Project Overview

### 1.1 Problem Statement
In fast-paced modern work environments, knowledge workers and professionals suffer from **extreme notification fatigue**. Critical messages—such as an urgent late-night directive from a manager to arrive early for a client meeting—routinely get buried under trivial banter in team chat apps (WhatsApp, Slack, Teams) or promotional emails. 
Furthermore, sending private messages and notifications to cloud LLMs (OpenAI, Anthropic, or external cloud endpoints) introduces massive privacy, data sovereignty, and enterprise compliance vulnerabilities.

### 1.2 The Solution: INLO ⚡
**INLO** is an autonomous, on-device AI co-pilot designed for Android. It operates as a local daemon via Android's `NotificationListenerService`, parsing, categorizing, and acting upon notifications in real time.
- **Zero Cloud Leakage:** The app completely omits `android.permission.INTERNET`, ensuring that kernel-level sandboxing guarantees zero data egress.
- **Smart Commute & Wake-Up Engine:** Automatically detects temporal directives (e.g., *"Tomorrow come at 8 to office"*), subtracts user-configured travel and preparation buffers, and autonomously schedules wake-up alarms and calendar appointments.
- **4-Tier Priority Triage:** Automatically classifies incoming messages into P0 (Critical/Actionable), P1 (High/Decisions), P2 (Medium/Mentions), and P3 (Low/Banter).
- **Autonomous Memory Engine:** Tracks user feedback (such as dismissed notifications or cancelled alarms) to continuously optimize classification weights locally.

---

## 2. Tech Stack & Architecture

### 2.1 Technologies Used
- **Platform & Language:** Android (minSdk 26, targetSdk 34, compileSdk 34), Kotlin 2.0.21, OpenJDK 17.
- **UI Framework:** Jetpack Compose with Material 3 (High-contrast, dark-mode cockpit aesthetic).
- **Asynchronous & Reactive Programming:** Kotlin Coroutines (`Dispatchers.IO`), Reactive `StateFlow`, `SharedFlow`.
- **Local Persistence:** Encrypted Room SQLite (`androidx.room` 2.6.1 with KSP), `DataStore` & `SharedPreferences`.
- **System Integration APIs:**
  - `NotificationListenerService` (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
  - Android `AlarmClock` API (`AlarmClock.ACTION_SET_ALARM`) & `AlarmManager` (Exact Alarms)
  - Android `CalendarContract` (`CalendarContract.Events`) for local calendar insertion
- **Security & Privacy Layer:** Zero network permission (`android.permission.INTERNET` omitted) + Regex-based PII/OTP scrubbing (`PrivacyShieldFilter`).

### 2.2 System Architecture Diagram
```
+--------------------------------------------------------------------------+
|                       Incoming Android Notifications                     |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                  INLONotificationListenerService                         |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       PrivacyShieldFilter                                |
|        (Drops OTPs, 2FA tokens, credit card digits, bank passwords)      |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       NotificationProcessor                              |
|  +---------------------------+       +---------------------------------+ |
|  | TemporalSignalExtractor   | ----> | CommuteAlarmCalculator          | |
|  +---------------------------+       +---------------------------------+ |
|                                                      |                   |
|  +---------------------------+                       v                   |
|  | AutonomousMemoryEngine    | ----> | PriorityClassifier (P0 - P3)    | |
|  +---------------------------+       +---------------------------------+ |
|                                                      |                   |
|  +---------------------------+                       |                   |
|  | MicroSummarizer           | <---------------------+                   |
|  +---------------------------+                                           |
+--------------------------------------------------------------------------+
              |                                            |
              v                                            v
+-----------------------------+              +-----------------------------+
|    Automations Dispatch     |              |     Local Room Database     |
| - SystemAlarmDispatcher     |              | - NotificationDao           |
| - CalendarSyncDispatcher    |              | - SummaryDao                |
| - LocalNotificationNotifier |              | - AutomationDao / MemoryDao |
+-----------------------------+              +-----------------------------+
                                                           |
                                                           v
                                             +-----------------------------+
                                             |   Jetpack Compose UI        |
                                             | - Dashboard (Triage Feed)   |
                                             | - AlarmsHistoryScreen       |
                                             | - RecapDialog & Settings    |
                                             +-----------------------------+
```

---

## 3. AI Code Generation Log

### Interaction 1: Core System & Architecture Scaffolding
- **Instruction / Prompt:**
  > *"Architect and implement INLO: an on-device Kotlin Android application that intercepts system notifications using NotificationListenerService, extracts temporal shift directives (e.g., manager early-morning messages), calculates commute and wake-up alarm times, and triages notifications into 4 priority tiers without any cloud communication or internet permission."*
- **AI Tool / Model:** Gemini / Google Antigravity
- **Purpose:** Establish clean MVVM architecture, entity models, Room database configurations, core engine extractors, and UI layout.
- **Files Affected:**
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/protocolx/inlo/INLOApp.kt`
  - `app/src/main/java/com/protocolx/inlo/data/model/*` (`PriorityTier.kt`, `MicroSummaryCard.kt`, `NotificationEntity.kt`, `ScheduledAutomation.kt`, `UserSettings.kt`)
  - `app/src/main/java/com/protocolx/inlo/data/db/*` (`AppDatabase.kt`, DAOs)
  - `app/src/main/java/com/protocolx/inlo/engine/*` (`NotificationProcessor.kt`, `PriorityClassifier.kt`, `MicroSummarizer.kt`)
- **Outcome & Verification:** Full multi-tier project scaffolded and compiled cleanly into Gradle targets.

---

### Interaction 2: Commute & Early Shift Alarm Engine
- **Instruction / Prompt:**
  > *"Implement CommuteAlarmCalculator and TemporalSignalExtractor. Given an incoming message like 'Tomorrow please come at 8 to office' and user settings with typical arrival at 10 AM, 2-hour commute, and 1-hour prep, parse the target time, identify the -2 hour shift delta, calculate a 5:00 AM wake-up alarm, and write comprehensive JUnit unit tests."*
- **AI Tool / Model:** Gemini / Google Antigravity
- **Purpose:** Build deterministic, regex-powered NLP extraction and mathematical buffer computation with robust unit tests.
- **Files Affected:**
  - `app/src/main/java/com/protocolx/inlo/engine/CommuteAlarmCalculator.kt`
  - `app/src/main/java/com/protocolx/inlo/engine/TemporalSignalExtractor.kt`
  - `app/src/test/java/com/protocolx/inlo/CommuteAlarmCalculatorTest.kt`
- **Outcome & Verification:** All unit tests in `CommuteAlarmCalculatorTest.kt` passed, verifying exact 5:00 AM alarm calculation and P0 classification for manager early shifts.

---

### Interaction 3: Jetpack Compose Dashboard & Cockpit UI
- **Instruction / Prompt:**
  > *"Create a high-contrast, modern cockpit dashboard in Jetpack Compose featuring a real-time permission status banner, quick triage filtering tabs (All, P0 Critical, P1 High, P2 Mentions, P3 Banter), expandable micro-summary cards with confidence scores, and an alarms history screen."*
- **AI Tool / Model:** Gemini / Google Antigravity
- **Purpose:** Provide an intuitive user experience allowing instant inspection of notification triage, alarm triggers, and user feedback actions.
- **Files Affected:**
  - `app/src/main/java/com/protocolx/inlo/ui/screens/DashboardScreen.kt`
  - `app/src/main/java/com/protocolx/inlo/ui/screens/AlarmsHistoryScreen.kt`
  - `app/src/main/java/com/protocolx/inlo/ui/screens/RecapDialog.kt`
  - `app/src/main/java/com/protocolx/inlo/ui/screens/SettingsScreen.kt`
  - `app/src/main/java/com/protocolx/inlo/ui/viewmodel/MainViewModel.kt`
- **Outcome & Verification:** Rendered responsive UI with Material 3 styling, reactive state binding via `StateFlow`, and quick actions for alarm confirmations and dismissals.

---

## 4. Debugging & Error Resolution Log

### Incident 1: Startup Crash on Launch & Permission Check
- **Observed Error / Issue:** App encountered runtime exceptions during launch when querying notification listener status and scheduling exact alarms on Android 12+ (API 31/32) and Android 13+ (API 33).
- **Root Cause:**
  1. `NotificationManagerCompat.getEnabledListenerPackages` could fail or return inconsistent data on certain customized Android ROMs.
  2. Starting intent activities without `FLAG_ACTIVITY_NEW_TASK` from application contexts caused crashes.
  3. `AlarmManager.canScheduleExactAlarms()` required explicit API level gating (Build.VERSION_CODES.S).
- **Prompt Used:**
  > *"Refactor PermissionHelper and MainActivity to safely handle notification listener verification, exact alarm permissions, and intent navigation across Android 10 through 14 without crashing."*
- **Files Affected:**
  - `app/src/main/java/com/protocolx/inlo/ui/PermissionHelper.kt`
  - `app/src/main/java/com/protocolx/inlo/ui/MainActivity.kt`
- **Solution:** Added secondary verification via `Settings.Secure.getString(contentResolver, "enabled_notification_listeners")`, wrapped intent transitions in try-catch with fallback to standard settings, and applied API level checks.

---

### Incident 2: Calendar Provider Crash & Unhandled Security Exception
- **Observed Error / Issue:** `CalendarSyncDispatcher` threw `SecurityException` when attempting to write calendar entries when permissions were pending or if calendar ID 1 did not exist on the device.
- **Root Cause:** Hardcoded `CALENDAR_ID = 1` failed on devices where the primary calendar ID was different or not configured, and missing calendar permissions resulted in unhandled crashes.
- **Prompt Used:**
  > *"Update CalendarSyncDispatcher to dynamically query for the user's primary or visible calendar ID, safely catch any SecurityException or content provider failure, and log the action gracefully without interrupting the background listener."*
- **Files Affected:**
  - `app/src/main/java/com/protocolx/inlo/automation/CalendarSyncDispatcher.kt`
- **Solution:** Implemented `findPrimaryCalendarId(context)` querying `CalendarContract.Calendars.CONTENT_URI` for `IS_PRIMARY == 1` or `VISIBLE == 1`, with safe fallbacks and defensive `Throwable` catch blocks.

---

### Incident 3: Android 13+ Notification Dispatching Security Exception
- **Observed Error / Issue:** `LocalNotificationNotifier` crashed or dropped alerts silently on Android 13+ (API 33+) due to unverified `POST_NOTIFICATIONS` runtime permission.
- **Root Cause:** Android 13 introduced runtime permission for posting notifications; trying to notify without permission or proper channel initialization led to exceptions.
- **Prompt Used:**
  > *"Update LocalNotificationNotifier to defensively check ContextCompat.checkSelfPermission for Manifest.permission.POST_NOTIFICATIONS, ensure notification channel creation, and handle nullable notification managers safely."*
- **Files Affected:**
  - `app/src/main/java/com/protocolx/inlo/automation/LocalNotificationNotifier.kt`
- **Solution:** Wrapped notification posts with `ContextCompat.checkSelfPermission` checks, nullable manager casting, and defensive exception logging.

---

## 5. AI Features & Design Decisions

### 5.1 Privacy-First Architecture (Zero Internet)
- **Design Prompt:** *"How do we guarantee 100% privacy so that enterprise users and privacy-conscious individuals trust INLO with all their notifications?"*
- **Architecture Choice:** Rather than relying on cloud LLM inference, implement an on-device hybrid intelligence pipeline combining regex-based entity parsing, temporal heuristics, and an autonomous Bayesian-style memory weight engine. Omit `android.permission.INTERNET` from `AndroidManifest.xml` entirely so the OS kernel guarantees zero network transmission.

### 5.2 Commute Math Model
$$\text{Calculated Wake-Up Alarm} = \text{Target Arrival Time} - (\text{Commute Minutes} + \text{Preparation Minutes})$$
- When an early shift directive is detected from a VIP contact (e.g., manager, team lead), INLO verifies if:
  $$\Delta = \text{Typical Arrival Hour} - \text{Target Arrival Hour} > 0$$
- If an early shift is detected, the event is immediately elevated to **P0 (Critical)**, triggering both an autonomous alarm proposal and calendar event synchronization.

### 5.3 Autonomous Feedback Reinforcement
- If a user cancels an automatically scheduled alarm or dismisses a high-priority card, `AutonomousMemoryEngine` updates the confidence score of the sender and keyword pattern:
  $$\text{weight}_{t+1} = \max(0.1, \text{weight}_t - 0.2)$$
- Conversely, when an action is confirmed or preserved, trust is reinforced:
  $$\text{weight}_{t+1} = \min(1.0, \text{weight}_t + 0.1)$$

---

## 6. Testing & Improvements

### 6.1 Unit Testing
- Executed unit tests in `CommuteAlarmCalculatorTest.kt`:
  - `testManagerEarlyShiftAlarmCalculation()`: Verified that for an 8:00 AM target arrival with 120m commute + 60m prep, the calculated alarm time is 05:00 AM (120m early delta). **Passed.**
  - `testTemporalExtractionFromText()`: Verified regex parsing extracts `targetHour = 8`, `isTomorrow = true`, `targetLocation = "Office"`. **Passed.**
  - `testPriorityClassificationForManagerEarlyShift()`: Verified that an early morning shift message from a VIP sender yields `PriorityTier.P0_CRITICAL`. **Passed.**

### 6.2 Manual & Device Verification
- Tested installation of signed release package `INLO-v1.0.1.apk`.
- Verified notification capture via Android `NotificationListenerService` across WhatsApp, SMS, and email notifications.
- Tested edge cases with missing calendar permissions, ensuring graceful notification card creation without application crashes.

---

## 7. Final Summary

| Metric / Dimension | Detail |
| :--- | :--- |
| **AI Coding Assistant** | Google Antigravity (Gemini 3.8 Flash) |
| **Primary Code Artifacts** | 50+ source files, 3,300+ lines of Kotlin code |
| **Key Accomplishments** | Full on-device notification parsing, 4-tier triage, autonomous alarm & calendar generation |
| **Privacy Guarantee** | Zero network permissions (`INTERNET` permission completely absent) |
| **Release Artifact** | `INLO-v1.0.1.apk` generated and ready for direct installation |
| **Repository Status** | Clean build, passing unit tests, published to GitHub `main` |
