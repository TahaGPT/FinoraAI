# FinoraAI — Agentic Financial Intelligence Platform

<p align="center">
  <strong>🧠 Your CFO Twin · Powered by AI</strong><br>
  <em>AI Seekho Hackathon 2026 — Challenge 1: Autonomous Content-to-Action Agent</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Min_SDK-26-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Backend-FastAPI-009688?style=flat-square" />
  <img src="https://img.shields.io/badge/AI-Gemini_Pro-4285F4?style=flat-square" />
</p>

---

## 📖 What is FinoraAI?

FinoraAI is not a dashboard — it is a **CFO Twin**: a living, breathing financial brain that ingests chaotic real-world data (handwritten ledgers, PDFs, CSVs, live feeds), resolves contradictions, simulates futures, and executes multi-step action chains — all with an immutable audit trail on-chain.

**North Star Metric**: A founder asks _"Can we afford 5 more hires?"_ and Finora answers in under 10 seconds with a full multi-step action chain, scenario simulation, and confidence-scored recommendation.

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| **Mobile App** | Kotlin · Jetpack Compose · Material3 |
| **Architecture** | MVVM + Repository Pattern |
| **Navigation** | Compose Navigation with animated transitions |
| **Networking** | Retrofit2 + OkHttp (REST) · Ktor (WebSocket) |
| **Camera** | CameraX + ML Kit (edge detection) |
| **Charts** | MPAndroidChart + Custom Canvas |
| **Local DB** | Room (offline caching) |
| **DI** | Hilt (Dagger) |
| **Auth** | Firebase Auth (Google Sign-In) |
| **Push** | Firebase Cloud Messaging (FCM) |
| **Backend** | FastAPI · LangGraph · Gemini Pro |
| **Automation** | n8n workflows |
| **Blockchain** | Solidity · Polygon Mumbai Testnet |
| **Infra** | Docker · Google Cloud Run |

---

## 📐 Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│                   Android App (Kotlin)                │
│  ┌────────────────────────────────────────────────┐   │
│  │  UI Layer (Jetpack Compose + Material3)        │   │
│  │  ├── Screens (13 screens)                      │   │
│  │  ├── Components (9 reusable)                   │   │
│  │  └── Theme (Dark + Light + Animations)         │   │
│  ├────────────────────────────────────────────────┤   │
│  │  Navigation (Compose NavHost + transitions)    │   │
│  ├────────────────────────────────────────────────┤   │
│  │  ViewModel Layer (StateFlow + LiveData)        │   │
│  ├────────────────────────────────────────────────┤   │
│  │  Data Layer                                    │   │
│  │  ├── Repository (API + Local DB)               │   │
│  │  ├── Retrofit (REST) + Ktor (WebSocket)        │   │
│  │  └── Room (Offline Cache)                      │   │
│  └────────────────────────────────────────────────┘   │
└───────────────────────┬──────────────────────────────┘
                        │ HTTPS / WSS
┌───────────────────────▼──────────────────────────────┐
│              Backend (FastAPI + LangGraph)            │
│  ┌─────────┐ ┌──────────┐ ┌───────────┐ ┌────────┐  │
│  │Ingestion│ │ Insight  │ │Contradict.│ │ Action │  │
│  │ Agent   │ │ Analyst  │ │ Detector  │ │Executor│  │
│  └────┬────┘ └────┬─────┘ └─────┬─────┘ └───┬────┘  │
│       └──────────┬─┴────────────┘            │       │
│            Orchestrator Agent (Gemini Pro)    │       │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────┘       │
│  │ QuantLib │ │  n8n     │ │ Blockchain              │
│  │Monte Carl│ │Workflows │ │ Audit Logger            │
│  └──────────┘ └──────────┘ └─────────────────────────│
└──────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
finora-ai/
├── README.md
├── ToDo.md                              # Master development plan
├── palette.scss                         # Design color palette
└── android/                             # 📱 Kotlin Android App
    ├── build.gradle.kts                 # Project-level plugins
    ├── settings.gradle.kts              # Module configuration
    ├── gradle.properties                # Build properties
    └── app/
        ├── build.gradle.kts             # App dependencies & config
        └── src/main/
            ├── AndroidManifest.xml      # Permissions & activity
            ├── res/values/
            │   └── themes.xml           # Edge-to-edge base theme
            └── java/com/finora/ai/
                ├── MainActivity.kt      # Entry point + scaffold
                ├── FinoraApplication.kt # App-level init
                │
                ├── data/model/
                │   └── Models.kt        # 20+ domain models
                │
                └── ui/
                    ├── theme/
                    │   ├── Color.kt     # 40+ color tokens
                    │   ├── Theme.kt     # Dark & Light schemes
                    │   ├── Type.kt      # Typography scale
                    │   └── Shape.kt     # Corner radius system
                    │
                    ├── navigation/
                    │   ├── Navigation.kt # 14 routes + transitions
                    │   └── NavGraph.kt   # Full nav wiring
                    │
                    ├── components/
                    │   └── Components.kt # 9 reusable components
                    │
                    └── screens/
                        ├── splash/SplashScreen.kt
                        ├── login/LoginScreen.kt
                        ├── home/HomeScreen.kt
                        ├── analysis/SourceSelectionScreen.kt
                        ├── warroom/WarRoomScreen.kt
                        ├── insight/InsightReportScreen.kt
                        ├── actionchain/ActionChainScreen.kt
                        ├── execution/ExecutionTrackerScreen.kt
                        ├── ghostledger/GhostLedgerCameraScreen.kt
                        ├── ghostledger/GhostLedgerReviewScreen.kt
                        ├── simulator/SimulatorScreen.kt
                        ├── simulator/ScenarioBattleScreen.kt
                        ├── audit/AuditTrailScreen.kt
                        └── audit/AuditDetailScreen.kt
```

---

## 🖥️ Screen Walkthrough (13 Screens)

### Flow Diagram

```
Splash → Login → Home Dashboard
                    ├── [+] New Analysis → Source Selection → War Room ⭐ → Insight Report → Action Chain → Execution Tracker
                    ├── [📷] Scan Ledger → Ghost Ledger Camera ⭐⭐ → OCR Review (confidence halos)
                    ├── [🔮] What-If Simulator → Scenario Battle ⚔️ (racing curves)
                    └── [🔗] Audit Trail → Audit Detail (blockchain proof)
```

---

### 1. Splash Screen
> `screens/splash/SplashScreen.kt`

**Cinematic brand reveal** with 4 animation phases:
1. Particle burst background with 30 floating orbs
2. Logo bounces in with `spring(DampingRatioMediumBouncy)`
3. "FinoraAI" title slides up with low-bounce spring
4. Subtitle fades in, loading dots pulse with staggered delays

Auto-navigates to Login after ~2.5 seconds.

---

### 2. Login Screen
> `screens/login/LoginScreen.kt`

**Glass-morphism design** with:
- 4 ambient floating orbs (`Canvas` with `infiniteTransition`)
- Slide-up form card with email/password fields
- Google Sign-In button with gradient border
- Loading spinner on auth

---

### 3. Home Dashboard
> `screens/home/HomeScreen.kt`

The **financial command center** featuring:
- **Top Bar**: "Good morning, [Name] · Finora is watching 📊" with pulsing green dot
- **Cashflow River Card**: Animated sine-wave water that changes color by health score:
  - Green (≥70) → fast flowing
  - Amber (40-69) → slow
  - Red (<40) → danger
- **KPI Strip**: Runway days, Balance, Burn Rate, MoM Change
- **Quick Action Grid**: 4 cards → New Analysis, Scan Ledger, Simulate, Audit
- **Alert Feed**: Staggered entrance cards with color-coded accent bars
- **Theme Toggle**: Dark ↔ Light mode switch in the top bar
- **Expandable FAB**: Staggered sub-action buttons with scale + bounce animations

---

### 4. Source Selection
> `screens/analysis/SourceSelectionScreen.kt`

**Multi-select data sources** for analysis:
- 6 source types: PDF, URL, CSV, Real-Time Feed, Ghost Ledger, Google Sheets
- Animated toggle cards with scale bounce on selection
- Upload area expands below selected sources
- Sticky bottom bar slides in when sources are selected
- "Start Analysis ⚡" button triggers War Room

---

### 5. War Room ⭐ (WOW Feature)
> `screens/warroom/WarRoomScreen.kt`

**Cinematic live analysis visualization** — full-screen dark mode:
- **Animated Background**: Neon grid lines + 16 floating particles
- **Stress-O-Meter**: Circular gauge updating in real-time (85 → 45 → 62 → 72)
- **Contradiction Flash**: Full-screen red overlay with "⚡ CONFLICT DETECTED" banner
- **Agent Thought Bubbles**: Sequential cards sliding in with color-coded accent bars:
  - Turquoise = Step Start
  - Green = Complete
  - Teal = Tool Call
  - Mint = Insight Found
  - Red = Contradiction Detected
- **Progress Bar**: Animated with percentage counter
- Auto-transitions to Insight Report on completion

---

### 6. Insight Report
> `screens/insight/InsightReportScreen.kt`

**Tabbed analysis results** with `HorizontalPager`:
- **Insights Tab**: Signal cards (revenue decline, burn rate warning, inventory critical)
- **Contradictions Tab**: Side-by-side source comparison with green winner highlight
  - Example: Warehouse CSV (500 units, 3 weeks old) vs Ghost Ledger (287 units, 1 day old) → Ghost Ledger wins ✅
- **Risks Tab**: Probability-scored risk cards
- **Opportunities Tab**: Value-scored opportunity cards
- "Generate Action Plan ⚡" CTA at bottom

---

### 7. Action Chain
> `screens/actionchain/ActionChainScreen.kt`

**Vertical stepper** showing 5 dependency-ordered actions:
1. Validate Stock Count 📝
2. Notify Procurement Manager 📩 (requires approval badge)
3. Emergency Order Simulation 🔮 (budget exceeded → auto-adjusted ₨500k → ₨350k)
4. Update Delivery Estimates 📝
5. Schedule 24-Hour Monitoring ⏰

Each step has: numbered circle, accent line connector, constraint badges, approval toggles.

---

### 8. Execution Tracker
> `screens/execution/ExecutionTrackerScreen.kt`

**Animated state machine** per action:
- Queued → Running (pulsing amber dot) → Success ✅ / Failed ❌
- Failed action shows retry → recovery → success
- **Confetti animation** on completion ("🎉🎊✨ All Actions Complete! ✨🎊🎉")
- **Summary Card**: Actions executed (5/5), Retries (1), Total time (4.8s), Est. cost ($0.034)

---

### 9. Ghost Ledger Camera ⭐⭐ (SURPRISE Feature)
> `screens/ghostledger/GhostLedgerCameraScreen.kt`

**Live edge-detection camera overlay**:
- Animated scan line sweeping vertically
- Dashed table boundary detection rectangle
- Corner markers at all 4 corners
- Pulsing border opacity
- Capture button (floating circular FAB)
- Processing overlay: "Gemini is reading your ledger..." with spinner

---

### 10. Ghost Ledger Review
> `screens/ghostledger/GhostLedgerReviewScreen.kt`

**Per-cell confidence color coding** (the "confidence halos"):
- 🟢 Green border: confidence ≥ 0.9
- 🟡 Yellow border: confidence 0.7–0.9 (pulsing)
- 🔴 Red border + ⚠️: confidence < 0.7 (fast pulse, tap to edit)
- Horizontally scrollable extracted table
- Legend bar showing color meanings
- Overall confidence score (78%)
- "Confirm & Add to Session 🔗" → writes SHA-256 hash to blockchain

---

### 11. What-If Simulator
> `screens/simulator/SimulatorScreen.kt`

**Natural language scenario input**:
- Text field: _"What if I hire 5 engineers at PKR 90k each?"_
- **Live parameter extraction**: chips appear as you type (`hire_count: 5`, `salary: 90k`, `monthly_cost: 450k`)
- **One-tap stress tests**: 4 pre-configured scenarios
  - Revenue drops 20% for 3 months
  - Supplier raises prices 15%
  - PKR/USD increases 10%
  - Largest customer churns
- "Run Scenario Battle ⚔️" button

---

### 12. Scenario Battle
> `screens/simulator/ScenarioBattleScreen.kt`

**Racing cashflow curves** on dark canvas:
- 3 scenarios rendered as animated `Path` curves racing to 12-month mark
- Red danger zone overlay at bottom 30% of chart
- Dashed danger threshold line
- Curves draw progressively over ~4 seconds
- **Winner Card** bounces in with crown 👑:
  - Name, Runway (186 days), Balance (₨5.1M), Confidence (87%)
  - "Save Scenario" button

---

### 13. Audit Trail + Detail
> `screens/audit/AuditTrailScreen.kt` + `AuditDetailScreen.kt`

**Blockchain-secured audit log**:
- Chronological list with color-coded action types (OCR, Contradiction, Action, Simulation)
- Transaction hash, document hash per entry
- Filter button for type/date filtering
- "Export Audit PDF" button
- **Detail screen**: Full tx details (block number, network, gas), 5-step reasoning trace, PolygonScan link

---

## 🎨 Design System

### Color Palette (from `palette.scss`)

| Name | Hex | Usage |
|---|---|---|
| Ink Black | `#060B0F` | Dark background |
| Jet Black | `#03272B` | Dark surface |
| Dark Teal | `#004346` | Primary (light), containers |
| Mint Leaf | `#09BC8A` | Primary accent, CTAs |
| Turquoise | `#3FCDB4` | Secondary accent |
| Pearl Aqua | `#75DDDD` | Tertiary, info elements |

### Semantic Colors

| Purpose | Color |
|---|---|
| Success | `#09BC8A` (Mint Leaf) |
| Warning | `#F5A623` (Amber) |
| Danger | `#E74C5E` (Red) |
| Critical Pulse | `#FF3B5C` (Hot Red) |
| Neon Mint | `#00FFB3` (War Room glow) |
| Neon Cyan | `#00F0FF` (War Room accent) |

### Theme Switching

The app supports **dark mode** (default) and **light mode** with smooth `animateColorAsState` transitions. Toggle via the ☀️/🌙 icon on the Home Dashboard top bar.

---

## 🎬 Animation Reference

| Category | Animations Used |
|---|---|
| **Entrance** | `fadeIn`, `slideInVertically`, `slideInHorizontally`, `scaleIn`, `expandVertically` |
| **Exit** | `fadeOut`, `slideOutVertically`, `scaleOut`, `shrinkVertically` |
| **Infinite** | `infiniteTransition` for pulses, waves, particles, scan lines |
| **Spring** | `DampingRatioLowBouncy` for card entrances, `DampingRatioMediumBouncy` for buttons |
| **Canvas** | Sine-wave water, neon particles, gauge arcs, scan lines, racing curves |
| **State** | `animateColorAsState`, `animateFloatAsState`, `animateIntAsState` |

All transitions use `FastOutSlowInEasing` for enter and `FastOutLinearInEasing` for exit to feel natural and responsive.

---

## 🚀 Build & Run Instructions

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| **Android Studio** | Hedgehog (2023.1.1) or newer | [developer.android.com](https://developer.android.com/studio) |
| **JDK** | 17+ | Bundled with Android Studio |
| **Android SDK** | API 34 (Android 14) | Via SDK Manager |
| **Kotlin** | 1.9.22 | Via Gradle plugin |

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-team/finora-ai.git
cd finora-ai
```

### Step 2: Open in Android Studio

```
File → Open → Select the `android/` directory
```

> **Important**: Open the `android/` folder specifically, not the root `finora-ai/` folder. Android Studio needs `settings.gradle.kts` at the project root.

### Step 3: Sync Gradle

Android Studio will auto-detect the Gradle files and prompt you to sync. Click **"Sync Now"** in the notification bar, or:

```
File → Sync Project with Gradle Files
```

This downloads all dependencies (~200MB on first sync):
- Jetpack Compose BOM 2024.02
- Material3, Navigation, Animation
- Retrofit2, OkHttp, Ktor
- CameraX, Room, Coroutines

### Step 4: Build the Project

**Via Android Studio:**
```
Build → Make Project    (or Ctrl+F9)
```

**Via Command Line:**
```bash
cd android

# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Run all checks
./gradlew check
```

The APK output will be at:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

### Step 5: Run on Device/Emulator

**Option A — Emulator:**
1. Open **Device Manager** in Android Studio (Tools → Device Manager)
2. Create a new device: **Pixel 7** with **API 34** system image
3. Click ▶ **Run** (or `Shift+F10`)

**Option B — Physical Device:**
1. Enable **Developer Options** on your Android phone
2. Enable **USB Debugging**
3. Connect via USB cable
4. Select your device in the run configuration dropdown
5. Click ▶ **Run**

**Option C — Command Line:**
```bash
# List connected devices
adb devices

# Install and run
./gradlew installDebug
adb shell am start -n com.finora.ai.debug/com.finora.ai.MainActivity
```

### Step 6: (Optional) Firebase Setup

To enable authentication and push notifications:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project "FinoraAI"
3. Add an Android app with package `com.finora.ai`
4. Download `google-services.json`
5. Place it in `android/app/google-services.json`
6. Uncomment Firebase dependencies in `android/app/build.gradle.kts`:
   ```kotlin
   // Remove the // comment prefix from these lines:
   implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
   implementation("com.google.firebase:firebase-auth-ktx")
   implementation("com.google.firebase:firebase-messaging-ktx")
   ```
7. Uncomment the plugin in `android/build.gradle.kts`:
   ```kotlin
   id("com.google.gms.google-services") version "4.4.0" apply false
   ```
8. Re-sync Gradle

---

## 🧪 Common Gradle Commands

```bash
cd android

# Build
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew bundleRelease          # Build AAB (for Play Store)

# Install
./gradlew installDebug           # Install on connected device
./gradlew uninstallDebug         # Uninstall debug build

# Test
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests

# Clean
./gradlew clean                  # Clean build outputs
./gradlew clean assembleDebug    # Clean + rebuild

# Info
./gradlew dependencies           # Show dependency tree
./gradlew signingReport          # Show signing info
./gradlew lint                   # Run lint checks
```

---

## 📊 Performance Targets

| Operation | Target Latency |
|---|---|
| App cold start | < 1.5s |
| Screen transition | < 400ms |
| PDF ingestion | < 2s |
| Full analysis (5 docs) | < 15s |
| Monte Carlo (1000 paths) | < 3s |
| Ghost Ledger OCR | < 5s |
| WebSocket first event | < 1s |

---

## 💰 Cost Targets (per session)

| Service | Estimated Cost |
|---|---|
| Gemini Pro (analysis) | ~$0.02 |
| Gemini Vision (OCR) | ~$0.005 |
| Blockchain tx (Mumbai testnet) | $0.00 |
| **Total per session** | **~$0.025** |

---

## 🧑‍💻 Team Task Split

| Member | Focus Area |
|---|---|
| Lead AI Engineer | LangGraph agents + Gemini integration + Antigravity |
| Backend Engineer | FastAPI endpoints + PostgreSQL + n8n workflows |
| Android Engineer | Kotlin app — all screens + WebSocket + CameraX |
| Full-Stack / Blockchain | QuantLib simulations + Solidity contract + Web3 bridge |
| All | Demo scenario data prep + video recording |

---

## 📋 Known Limitations

- **Mock Data**: All screens currently use inline mock data (no API calls yet)
- **Firebase**: Auth is bypassed (Login → Home directly) until `google-services.json` is added
- **Camera**: Ghost Ledger uses a placeholder; real CameraX preview needs physical device
- **WebSocket**: War Room simulates agent events locally; needs backend `/ws/agent-feed` connection
- **Charts**: Scenario Battle uses custom Canvas; MPAndroidChart integration pending for production curves

---

## 📄 License

This project is built for the **AI Seekho Hackathon 2026**. All rights reserved by the FinoraAI team.

---

<p align="center">
  <em>Last updated: May 2026 · FinoraAI · AI Seekho Hackathon 2026</em>
</p>
