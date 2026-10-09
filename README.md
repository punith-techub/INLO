# INLO ⚡
> **Your Local-First AI Notification Co-Pilot**  
> *Zero Cloud Leakage • Smart 4-Tier Triage • Autonomous Alarms & Calendar Sync*

---

## 💡 What is INLO?
**INLO** is an intelligent, privacy-first Android app (`.apk`) that runs completely on your phone. It reads your incoming notifications across all apps (WhatsApp, Slack, Gmail, Outlook, SMS, Tasks) and **instantly turns overwhelming message clutter into clear, actionable micro-summaries**.

Even better: it detects schedule shifts in real time and **automatically sets wake-up alarms and calendar events** factoring in your travel and preparation time.

---

## ❓ Why Was INLO Built?
In modern work life:
- **Notification Overload:** Hundreds of chats bury critical directives under casual banter.
- **Missed Emergencies:** If your manager texts late at night to *"come at 8 AM tomorrow instead of 10 AM"*, you might miss it, wake up at your usual time, and arrive late.
- **Privacy Nightmare:** Sending your personal WhatsApp chats, work emails, and bank notifications to third-party cloud servers (like OpenAI or cloud APIs) is a serious security and compliance risk.

**INLO fixes all of this directly on your device.**

---

## 🚀 Key Features

### 1. 🚨 The Smart Commute & Wake-Up Engine
- **The Scenario:** Your manager texts: *"Tomorrow please come at 8 to office (usual time: 10 AM)"*.
- **The Intelligence:** INLO recognizes the early shift (-2 hours), checks your daily profile (e.g., 2h commute + 1h prep = 3h total buffer), and automatically calculates:
  $$\text{Target (08:00 AM)} - \text{Buffer (3 hours)} = \mathbf{05:00\text{ AM Alarm}}$$
- **The Action:** Autonomously schedules a **5:00 AM alarm** via Android's AlarmClock API and inserts the **8:00 AM office event** into your calendar!

### 2. ⚡ Real-Time 4-Tier Priority Triage
Every notification is instantly sorted into 4 clear levels:
- **🚨 P0 (Critical):** Manager directives, early arrival shifts, urgent server crashes (`auto-triggers alarms`).
- **⚡ P1 (High):** Agreed team decisions, project deadlines, meeting reschedules (`calendar sync`).
- **📌 P2 (Medium):** Direct mentions (`@you`), unanswered questions awaiting your reply.
- **💬 P3 (Ambient):** Group chat banter, newsletters, low-priority app pings.

### 3. 🧠 Hidden Autonomous Memory Engine
- Runs completely in the background without user intervention.
- **Learns from your behavior:** If you cancel an auto-set alarm or dismiss a priority card, INLO notices the mistake and exponentially reduces priority weight for that sender/phrase.
- If you rely on and keep the alarm, it exponentially reinforces trust. The app improves over time!

### 4. 🛡️ 100% Local-First (Zero Cloud Leakage)
- **Zero Internet Permission:** `android.permission.INTERNET` is **completely omitted** from the app manifest.
- By OS-level kernel enforcement, **no data can ever leave your phone**.
- **Privacy Shield:** Automatically drops 2FA/OTP codes, banking alerts, and passwords before processing.

---

## 🔍 How INLO Fills Gaps from Conventional Apps

| Feature | Conventional Notification Apps | INLO |
| :--- | :--- | :--- |
| **Data Privacy** | Sends chat logs to remote cloud servers | **100% On-Device** (Zero Internet permission) |
| **Schedule Understanding** | Merely snoozes or silences alerts | **Calculates travel + prep buffers** & acts |
| **System Automation** | Requires manual reminder creation | **Auto-sets alarms & calendar events** |
| **Priority Hierarchy** | Flat chronological spam list | **4-tier triage** (P0 Critical down to P3 Banter) |
| **Self-Improvement** | Static rigid rules | **Autonomous learning memory** that adapts to you |
| **Data Cleanliness** | Packed with ads, tracking & dummy data | **Minimalist, high-contrast, zero-fluff UI** |

---

## 🛠️ Tech Stack
- **Platform:** Native Android (Kotlin 2.0+, JDK 17, Android SDK 34)
- **UI Framework:** Jetpack Compose + Material 3 (Modern Dark Minimalist Cockpit)
- **Background Hook:** Android `NotificationListenerService`
- **Automation APIs:** Android `AlarmClock`, `AlarmManager`, `CalendarContract`
- **Database:** Encrypted Room SQLite (`androidx.room` + KSP)
- **Architecture:** MVVM + Coroutines + Kotlin StateFlow

---

## 📲 Setup & Installation

### Option 1: Direct APK Install
1. Connect your Android phone via USB (or transfer the file).
2. Install the pre-compiled `.apk`:
   ```bash
   adb install INLO-v1.0.0.apk
   ```
3. Open **INLO** on your phone.
4. Tap **Enable** on the top banner to grant **Notification Access** in Android Settings.
5. *(Optional)* Go to ⚙️ **Settings** to adjust your typical commute time, prep time, and manager VIP names.

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/punith-techub/INLO.git
cd INLO

# Build Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```
The compiled APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔒 Privacy & Security Commitment
INLO does not collect telemetry, track usage, or connect to external APIs. Your notifications, schedule, routines, and messages remain exclusively on your device.
