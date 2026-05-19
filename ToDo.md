# FinoraAI — Agentic Financial Intelligence Platform
## Master Development Plan & ToDo

> **Hackathon**: AI Seekho 2026 — Challenge 1: Autonomous Content-to-Action Agent
> **Team Stack**: Kotlin (Android) · FastAPI · LangGraph · Gemini Pro · Gemma/Qwen (Edge) · n8n · Solidity · Docker

---

## 🗺️ OVERVIEW & PHILOSOPHY

FinoraAI is not a dashboard — it is a **CFO Twin**: a living, breathing financial brain that ingests chaotic real-world data (handwritten ledgers, PDFs, CSVs, live feeds), resolves contradictions, simulates futures, and executes multi-step action chains — all with an immutable audit trail on-chain.

**North Star Metric**: A founder asks "Can we afford 5 more hires?" and Finora answers in under 10 seconds with a full multi-step action chain, scenario simulation, and confidence-scored recommendation.

---

## 🌟 WOW FEATURE: "CFO War Room" — Live Scenario Battle Mode

When the user runs a "What-If" simulation, the UI enters **War Room Mode**:
- The screen splits into 2–4 parallel timeline tracks (each scenario rendered as an animated cashflow river)
- Each agent's reasoning step is visualized as a live "thought bubble" appearing in sequence
- A **Stress-O-Meter** gauge shows the financial health score ticking in real time as variables change
- Audio cue: a subtle heartbeat pulse that speeds up as cashflow approaches danger zones
- At the end, a "Battle Winner" card slides in declaring the optimal scenario with confidence %

This is not just a chart — it's a **cinematic financial simulation experience**.

---

## 🔮 SURPRISE OUTSTANDING FEATURE: "Ghost Ledger" — Handwriting Resurrection Engine

Most OCR tools fail on handwritten South Asian business ledgers (Urdu/Roman Urdu column headers, mix of Rupee symbols, pen smudges, ruled notebook pages). FinoraAI ships a **Ghost Ledger Pipeline**:

1. User photographs a handwritten ledger page with their phone camera
2. A custom fine-tuned CV model (YOLOv8 + Tesseract + Gemini Vision) detects row/column structure even in non-tabular hand-drawn grids
3. Gemini Vision extracts line items, amounts, dates — even partially legible entries
4. Uncertain extractions are flagged with a confidence halo (green/yellow/red glow per cell)
5. The user can tap any flagged cell to see Finora's "best guess" with an explanation
6. Confirmed data is **cryptographically hashed and written to the EVM audit chain** with a timestamp
7. The ledger is now a live digital asset — future photos of the same ledger are diff-compared to detect edits

**Why it's a jaw-dropper**: No competing tool handles the informal, handwritten, mixed-language financial records that 80% of Pakistani SMEs actually use. This is the feature that wins the room.

---

## 📐 PHASE 0 — PROJECT SETUP & INFRASTRUCTURE
*(Day 1 — ~4 hours)*

### 0.1 Repository Structure
```
finora-ai/
├── android/                  # Kotlin Android app
├── backend/
│   ├── api/                  # FastAPI main app
│   ├── agents/               # LangGraph agent definitions
│   ├── pipelines/            # n8n workflow JSONs
│   ├── ocr/                  # Ghost Ledger OCR service
│   ├── simulations/          # QuantLib Monte Carlo
│   ├── blockchain/           # Solidity contracts + Web3 bridge
│   └── data/                 # Mock data, seed files
├── infra/
│   ├── docker-compose.yml
│   └── cloud-run/
├── docs/
│   └── architecture.md
└── README.md
```

### 0.2 Environment Setup
- [ ] Init Git repo with branch strategy: `main`, `dev`, `feature/*`
- [ ] Docker Compose: FastAPI + Redis + PostgreSQL + n8n + Ollama (Gemma/Qwen)
- [ ] `.env` template: Gemini API key, DB URL, n8n webhook URLs, EVM RPC URL
- [ ] GitHub Actions CI: lint + test on every push to `dev`
- [ ] Google Cloud Run project initialized (free tier)
- [ ] Hardhat project for Solidity contracts

---

## 📐 PHASE 1 — DATA INGESTION LAYER (Ghost Ledger + Multi-Source)
*(Day 1–2 — ~10 hours)*

### 1.1 Input Source Types
The system must handle **5+ input types simultaneously**:

| Source Type | Format | Example |
|---|---|---|
| PDF/Report | Binary PDF | Bank statement, audit report |
| Website/Article | URL | News article about FX rates |
| CSV/JSON | Structured | Sales export from POS |
| Table/Dashboard | Screenshot/API | Google Sheets webhook |
| Mock Real-Time Feed | WebSocket/JSON | Simulated market ticker |
| **Ghost Ledger** | Phone Camera JPEG | Handwritten Urdu/Roman ledger |

### 1.2 PDF Ingestion Service
- [ ] `POST /ingest/pdf` endpoint in FastAPI
- [ ] Use `pdfplumber` + `pypdf2` for text extraction
- [ ] Gemini Pro for semantic chunking and entity extraction (dates, amounts, vendor names)
- [ ] Output: normalized `FinancialDocument` Pydantic model
- [ ] Store in PostgreSQL `documents` table with `source_type`, `ingested_at`, `credibility_score`

### 1.3 Web/Article Ingestion
- [ ] `POST /ingest/url` — scrape with `httpx` + `BeautifulSoup`
- [ ] Pass content to Gemini for relevance scoring against current financial context
- [ ] Tag: `recency`, `credibility_score` (0–1), `topic_tags`
- [ ] Reject URLs scoring below 0.3 relevance (noise filtering)

### 1.4 CSV/JSON Ingestion
- [ ] `POST /ingest/structured` — auto-detect schema with `pandas`
- [ ] Map columns to canonical schema: `date`, `amount`, `category`, `direction` (in/out)
- [ ] Handle missing columns via Gemini-powered column inference
- [ ] Validate: flag rows with null amounts, future dates, or negative inventory

### 1.5 Ghost Ledger OCR Pipeline ⭐ SURPRISE FEATURE
- [ ] `POST /ingest/ledger-photo` — accepts JPEG/PNG upload
- [ ] **Step 1**: YOLOv8 (pre-trained on document layouts) detects table boundaries and row lines
- [ ] **Step 2**: Tesseract OCR with Urdu + English language packs extracts raw text per cell
- [ ] **Step 3**: Gemini Vision API receives the image + Tesseract output for correction and structured extraction
- [ ] **Step 4**: Each extracted cell gets a `confidence_score` (0–1)
- [ ] **Step 5**: Low-confidence cells (< 0.7) flagged with `needs_review: true`
- [ ] **Step 6**: Normalized output → same `FinancialDocument` model as other sources
- [ ] **Step 7**: SHA-256 hash of the normalized document → sent to blockchain audit logger

### 1.6 Mock Real-Time Feed
- [ ] Background FastAPI WebSocket endpoint `/ws/market-feed`
- [ ] Simulates: USD/PKR rate, commodity prices, fuel index (relevant to SME costs)
- [ ] n8n workflow polls this every 60 seconds and triggers re-analysis agent if delta > 5%

### 1.7 Temporal Tagging
- [ ] All ingested documents get `document_timestamp` (extracted from content) vs `ingestion_timestamp`
- [ ] "Staleness score" = `(now - document_timestamp).days / 30` (capped at 1.0)
- [ ] Stale documents (staleness > 0.8) auto-flagged in contradiction detection

---

## 📐 PHASE 2 — AGENT ORCHESTRATION LAYER (LangGraph)
*(Day 2–3 — ~14 hours)*

### 2.1 Agent Architecture Overview

```
User Query / Trigger
        ↓
  [Orchestrator Agent]  ← Master planner (Gemini Pro)
        ↓
  ┌─────┴──────────────────────────────────────┐
  ↓                  ↓                          ↓
[Ingestion       [Insight               [Action Chain
 Agent]           Analyst Agent]         Executor Agent]
  ↓                  ↓                          ↓
[OCR/Parse]    [Contradiction           [Constraint
               Detector Agent]          Validator Agent]
                    ↓                          ↓
              [Resolution Agent]      [Rollback/Recovery
                                       Agent]
```

### 2.2 Orchestrator Agent
- [ ] Built with LangGraph `StateGraph`
- [ ] State: `{ documents[], insights[], action_plan[], execution_log[], current_step, failed_steps[] }`
- [ ] Entry node: classify query type (analysis / simulation / action / alert)
- [ ] Routes to sub-graphs based on query type
- [ ] Uses Gemini Pro (`gemini-pro`) as the reasoning LLM
- [ ] Uses local Gemma 2B via Ollama for low-stakes classification tasks (cost saving)

### 2.3 Insight Analyst Agent
- [ ] Receives all normalized documents for a given analysis session
- [ ] Runs: trend detection, anomaly detection, KPI extraction
- [ ] Outputs: `InsightReport` with fields: `key_signals[]`, `risks[]`, `opportunities[]`, `contradictions[]`
- [ ] Uses chain-of-thought prompting: "Think step by step about cashflow trends..."
- [ ] Temporal analysis: compares this month vs last 3 months for each KPI

### 2.4 Contradiction Detector Agent
- [ ] Receives `InsightReport.contradictions[]`
- [ ] For each contradiction: compares `document_timestamp`, `credibility_score`, `source_type`
- [ ] Scoring formula: `resolution_weight = credibility * (1 - staleness) * source_priority`
- [ ] `source_priority`: Real-time feed (1.0) > Bank statement (0.9) > CSV export (0.8) > Email (0.6) > News (0.5)
- [ ] Output: `ContradictionResolution` — which source wins, why, confidence %
- [ ] If unresolvable: generates `InvestigationPath` (list of clarification actions)

### 2.5 Action Chain Planner Agent
- [ ] Takes `InsightReport` + `ContradictionResolutions[]`
- [ ] Generates 3–5 interconnected actions using structured output (JSON mode)
- [ ] Each action has: `action_id`, `type`, `description`, `dependencies[]`, `constraints{}`, `rollback_action`
- [ ] Action types: `NOTIFY`, `UPDATE_RECORD`, `SIMULATE`, `FETCH_DATA`, `ALERT`, `SCHEDULE`
- [ ] Dependency graph ensures actions execute in correct order

### 2.6 Constraint Validator Agent
- [ ] Checks each action against defined constraints:
  - Budget: `action.estimated_cost <= available_budget`
  - Time: `action.estimated_duration <= deadline`
  - Rate limits: `action.api_calls <= rate_limit_remaining`
  - Resource: `action.requires_approval == false OR approval_granted`
- [ ] Infeasible actions: marked `status: REJECTED` with reason
- [ ] Modifies action parameters to find feasible alternative (e.g., reduce order quantity to fit budget)

### 2.7 Action Executor Agent
- [ ] Executes approved actions in dependency order
- [ ] Each execution step emits a state change event (via WebSocket to mobile app)
- [ ] Calls real tools: n8n webhooks, Gemini API, internal DB updates, blockchain logger
- [ ] On failure: triggers Rollback/Recovery Agent

### 2.8 Rollback/Recovery Agent
- [ ] Maintains a transaction log of all state changes
- [ ] On action failure: attempts retry (up to 3x with exponential backoff)
- [ ] If retry fails: executes `rollback_action` to reverse state
- [ ] Logs failure with `error_type`, `attempted_recovery`, `final_status`
- [ ] Notifies user via push notification with failure summary

### 2.9 LangGraph Implementation Details
- [ ] Each agent is a LangGraph `node` function
- [ ] Conditional edges: e.g., if contradictions found → route to Contradiction Detector, else skip
- [ ] `MemorySaver` checkpointing: save state after each node (enables resume on crash)
- [ ] Streaming: use `astream_events` to push real-time reasoning steps to frontend
- [ ] All LangGraph traces logged to PostgreSQL `agent_traces` table

---

## 📐 PHASE 3 — SIMULATION ENGINE
*(Day 3 — ~8 hours)*

### 3.1 Monte Carlo Cashflow Simulator
- [ ] `POST /simulate/cashflow` endpoint
- [ ] Inputs: `{ base_cashflow, variables: { revenue_change%, cost_change%, hire_count }, horizon_months }`
- [ ] QuantLib Monte Carlo: 1000 paths, log-normal distribution for revenue
- [ ] Outputs: `{ p10, p50, p90 cashflow curves, runway_months, default_probability }`
- [ ] Cached in Redis for 5 minutes (same params = same result, no recompute)

### 3.2 Scenario Comparison Engine
- [ ] `POST /simulate/compare` — accepts up to 4 scenarios simultaneously
- [ ] Runs all scenarios in parallel (asyncio `gather`)
- [ ] Returns normalized comparison: each scenario as `{ name, final_balance, runway, risk_score, recommended: bool }`
- [ ] "Battle Winner" determined by weighted score: `0.4*runway + 0.3*risk + 0.3*growth`

### 3.3 What-If Natural Language Interface
- [ ] `POST /simulate/nlq` — accepts plain text query
- [ ] Gemini Pro extracts simulation parameters from NL: "5 more hires at PKR 80k/month" → `{ hire_count: 5, monthly_cost: 400000 }`
- [ ] Runs Monte Carlo, returns result in natural language + structured data
- [ ] Example: "At current burn rate, 5 hires extend your runway from 14 to 9 months. Recommend delaying 2 hires by Q2."

### 3.4 Stress Test Scenarios (Built-In)
Pre-configured stress scenarios the user can one-tap:
- [ ] Revenue drops 20% for 3 months
- [ ] Key supplier raises prices 15%
- [ ] PKR/USD rate increases 10%
- [ ] Single largest customer churns
- [ ] Custom (user-defined via NL)

---

## 📐 PHASE 4 — BLOCKCHAIN AUDIT LAYER
*(Day 3–4 — ~6 hours)*

### 4.1 Solidity Smart Contract: `FinoraAuditLog`
```solidity
// AuditLog.sol
contract FinoraAuditLog {
    struct AuditEntry {
        bytes32 documentHash;
        string actionType;
        address recorder;
        uint256 timestamp;
        string metadataURI;  // IPFS link to full reasoning trace
    }
    mapping(uint256 => AuditEntry) public entries;
    uint256 public entryCount;
    event LogRecorded(uint256 indexed id, bytes32 hash, string actionType);
    function recordEntry(bytes32 _hash, string memory _actionType, string memory _uri) public { ... }
}
```
- [ ] Deploy to Mumbai Testnet (free) using Hardhat
- [ ] ABI exported for Python Web3.py integration

### 4.2 Python Blockchain Bridge
- [ ] `blockchain/audit_logger.py` — Web3.py wrapper
- [ ] `async def log_action(document_hash, action_type, metadata)` → writes to contract
- [ ] Called by: Ghost Ledger pipeline (on OCR completion), Action Executor (on each action), Contradiction Resolver (on resolution)
- [ ] Falls back to local PostgreSQL log if RPC unavailable (graceful degradation)

### 4.3 Audit Trail UI in App
- [ ] Dedicated "Audit Trail" screen in Android app
- [ ] Shows chronological list of all logged events with: timestamp, action type, tx hash, Etherscan link
- [ ] Tap entry → see full reasoning trace (loaded from IPFS or local DB fallback)

---

## 📐 PHASE 5 — BACKEND API (FastAPI)
*(Day 2–4 — ~10 hours)*

### 5.1 API Structure
```
/api/v1/
├── /ingest/          # All ingestion endpoints
├── /analyze/         # Trigger analysis session
├── /simulate/        # Scenario simulation
├── /actions/         # Action chain management
├── /audit/           # Blockchain audit trail
├── /ws/              # WebSocket endpoints
└── /health/          # Health check
```

### 5.2 Core Endpoints
- [ ] `POST /analyze/session` — start a new analysis session with multiple docs
- [ ] `GET /analyze/session/{id}` — poll session status + results
- [ ] `GET /analyze/session/{id}/stream` — SSE stream of agent reasoning steps
- [ ] `POST /actions/approve/{action_id}` — human-in-the-loop approval
- [ ] `POST /actions/reject/{action_id}` — reject with reason
- [ ] `GET /simulate/scenarios` — list saved scenarios
- [ ] `GET /audit/trail` — paginated audit log

### 5.3 WebSocket: Real-Time Agent Feed
- [ ] `/ws/agent-feed/{session_id}` — streams agent state changes to mobile
- [ ] Message types: `STEP_START`, `STEP_COMPLETE`, `TOOL_CALL`, `INSIGHT_FOUND`, `ACTION_EXECUTING`, `ACTION_FAILED`, `SESSION_COMPLETE`
- [ ] Android app subscribes on session start, updates UI in real time

### 5.4 Database Schema (PostgreSQL)
```sql
-- Core tables
documents (id, session_id, source_type, content_json, credibility_score, document_timestamp, staleness_score, ingested_at)
analysis_sessions (id, user_id, status, insight_report_json, created_at, completed_at)
agent_traces (id, session_id, agent_name, step, input_json, output_json, duration_ms, created_at)
action_plans (id, session_id, actions_json, constraints_json, status, created_at)
action_executions (id, action_plan_id, action_id, status, result_json, error_msg, retry_count, executed_at)
simulations (id, user_id, scenario_json, result_json, cached_at)
audit_entries (id, tx_hash, document_hash, action_type, metadata_json, created_at)
```

### 5.5 Auth & Security
- [ ] JWT authentication (Firebase Auth on Android, validated in FastAPI middleware)
- [ ] All financial data encrypted at rest (PostgreSQL encryption extension)
- [ ] API rate limiting: 100 req/min per user (slowapi)
- [ ] Sensitive fields (account numbers, amounts) masked in logs

---

## 📐 PHASE 6 — N8N AUTOMATION WORKFLOWS
*(Day 3 — ~4 hours)*

### 6.1 Workflow: Nightly Financial Digest
- Trigger: Cron (11 PM daily)
- Steps: Fetch today's transactions → Run analysis → Generate summary → Push notification to app

### 6.2 Workflow: Alert on Cash Danger Zone
- Trigger: Webhook from FastAPI when cashflow < 60-day runway
- Steps: Gemini drafts alert message → Send push notification → Log to audit trail

### 6.3 Workflow: Market Feed Monitor
- Trigger: Polling every 60s on `/ws/market-feed`
- Steps: Compare new rates to baseline → If delta > 5% → Trigger re-analysis session → Notify user

### 6.4 Workflow: Ledger Photo Processing
- Trigger: Webhook on new file upload to `/ingest/ledger-photo`
- Steps: Call OCR service → Validate output → If confidence < 0.7 → Send "review needed" push → Log to blockchain

---

## 📐 PHASE 7 — ANDROID APP (Kotlin)
*(Day 2–5 — ~20 hours)*

### 7.1 Screen Map
```
Splash / Onboarding
    └── Login (Firebase Auth)
         └── Home Dashboard
              ├── Quick Stats Cards (Cashflow, Runway, Burn Rate)
              ├── Recent Alerts Feed
              └── Quick Action FAB
                   ├── [+] New Analysis Session
                   │    └── Source Selection Screen
                   │         └── Analysis Live View (War Room) ⭐
                   │              └── Insight Report Screen
                   │                   └── Action Chain Screen
                   │                        └── Execution Tracker Screen
                   ├── [📷] Scan Ledger (Ghost Ledger) ⭐⭐
                   │    └── Camera Screen
                   │         └── OCR Review Screen (confidence halos)
                   │              └── Ledger Confirmed → adds to session
                   ├── [🔮] What-If Simulator
                   │    └── NL Query Input
                   │         └── War Room Scenario Battle ⭐
                   │              └── Scenario Winner Card
                   └── [🔗] Audit Trail
                        └── Audit Entry Detail
```

### 7.2 Home Dashboard Screen
- [ ] Top bar: "Good morning, [Name] · Finora is watching 📊"
- [ ] **Cashflow River Card**: Animated SVG/Canvas river that flows faster when healthy, slows/turns red near danger
- [ ] KPI Strip: Runway (days), Current Balance, Monthly Burn, MoM Change
- [ ] Alert Feed: Real-time list of agent-detected signals (contradiction found, action executed, etc.)
- [ ] Bottom Nav: Dashboard / Simulate / Scan / Audit

### 7.3 Analysis Session — Source Selection
- [ ] Multi-select source types with animated toggle cards
- [ ] Upload buttons per type: file picker (PDF/CSV), URL input, camera, "Connect Google Sheets"
- [ ] "Start Analysis" button → triggers `POST /analyze/session`
- [ ] Shows session ID and estimated time

### 7.4 War Room Screen ⭐ WOW FEATURE
- [ ] WebSocket connected to `/ws/agent-feed/{session_id}`
- [ ] Full-screen dark mode with neon accent colors
- [ ] Agent thought bubbles appear sequentially (each reasoning step as an animated card sliding in)
- [ ] Timeline bar at bottom: steps highlighted as completed (green pulse)
- [ ] **Contradiction Alert**: When contradiction detected → red flash + "⚡ Conflict Detected" banner
- [ ] **Stress-O-Meter**: Circular gauge (0–100) updating as insights accumulate
- [ ] Completion → smooth transition to Insight Report

### 7.5 Insight Report Screen
- [ ] Tabbed layout: Insights / Contradictions / Risks / Opportunities
- [ ] Each insight card: icon + summary + supporting sources (tappable)
- [ ] Contradictions tab: side-by-side source comparison with winner highlighted in green
- [ ] "Generate Action Plan" CTA at bottom

### 7.6 Action Chain Screen
- [ ] Vertical stepper showing 3–5 actions in dependency order
- [ ] Each step: icon, title, description, constraints badge (budget/time)
- [ ] Rejected actions shown in red with reason tooltip
- [ ] Human approval toggle for sensitive actions (e.g., "Send supplier email")
- [ ] "Execute Chain" button → navigate to Execution Tracker

### 7.7 Execution Tracker Screen
- [ ] Each action animates through states: Queued → Running → Success/Failed
- [ ] Failed actions show retry countdown + "Manual Override" option
- [ ] Before/After state panel: shows key metrics before chain vs projected after
- [ ] "Cost & Latency" footer: total API calls made, total time, estimated $ cost
- [ ] On completion: confetti animation + summary card

### 7.8 Ghost Ledger Scan Screen ⭐⭐ SURPRISE FEATURE
- [ ] CameraX integration with live edge-detection overlay (shows detected table borders in real time)
- [ ] Capture → upload to `/ingest/ledger-photo`
- [ ] Loading state: "Gemini is reading your ledger..." with subtle ink-drip animation
- [ ] Review Screen: renders extracted table with per-cell confidence color coding:
  - Green border: confidence ≥ 0.9
  - Yellow border: confidence 0.7–0.9
  - Red border + ⚠️: confidence < 0.7 (tap to manually correct)
- [ ] "Confirm & Add to Session" → blockchain hash written, document added to analysis

### 7.9 What-If Simulator Screen
- [ ] Natural language input field: "What if I hire 5 engineers at PKR 90k each?"
- [ ] Parsing animation: Gemini extracts params, shown as chips appearing
- [ ] One-tap stress test buttons (pre-configured scenarios)
- [ ] **Scenario Battle View**: side-by-side animated cashflow curves for up to 4 scenarios
  - Each curve "races" to 12-month mark
  - Danger zone (red area) shown on all curves simultaneously
  - Winner highlighted with animated crown + confidence badge
- [ ] "Save Scenario" → stored for future reference

### 7.10 Audit Trail Screen
- [ ] Chronological list: timestamp, action type, tx hash (truncated), status icon
- [ ] Filter by: action type, date range, document source
- [ ] Tap entry → full detail: document hash, full reasoning trace, Etherscan link button
- [ ] "Export PDF" button → calls backend to generate audit PDF

### 7.11 Android Tech Details
- [ ] Architecture: MVVM + Repository pattern
- [ ] Networking: Retrofit2 + OkHttp (REST) + Ktor client (WebSocket)
- [ ] State management: StateFlow + ViewModel
- [ ] Charts: MPAndroidChart (cashflow curves) + custom Canvas (War Room viz)
- [ ] Camera: CameraX + ML Kit for edge detection overlay
- [ ] Auth: Firebase Auth (Google Sign-In)
- [ ] Push notifications: Firebase Cloud Messaging (FCM)
- [ ] Local DB: Room (cache analysis results offline)
- [ ] DI: Hilt

---

## 📐 PHASE 8 — ANTIGRAVITY INTEGRATION (MANDATORY)
*(Day 4 — ~6 hours)*

> ⚠️ Google Antigravity must be the **core orchestration platform**. All agent workplans, task plans, tool calls, and decision traces must flow through Antigravity logs.

### 8.1 Antigravity Setup
- [ ] Register Finora AI project on Google Antigravity console
- [ ] Configure agent workplan: define the 6-agent graph as an Antigravity workflow
- [ ] Map LangGraph nodes → Antigravity task nodes

### 8.2 What Runs on Antigravity
- [ ] **Workplan creation**: When a new analysis session starts, Antigravity generates the workplan (which agents run, in what order, with what tools)
- [ ] **Task execution logs**: Every LangGraph node execution → logged as Antigravity task with input/output
- [ ] **Tool call registry**: All external API calls (Gemini, n8n webhooks, blockchain) registered as Antigravity tools
- [ ] **Decision trace**: Contradiction resolutions and constraint validations logged as Antigravity decision events
- [ ] **Recovery steps**: Rollback/retry actions logged as Antigravity recovery tasks

### 8.3 Antigravity Log Structure per Session
```json
{
  "workplan_id": "wp_abc123",
  "session_id": "sess_xyz",
  "tasks": [
    { "task_id": "t1", "agent": "Ingestion", "status": "complete", "duration_ms": 340, "tools_called": ["pdf_parser", "gemini_extract"] },
    { "task_id": "t2", "agent": "InsightAnalyst", "status": "complete", "duration_ms": 1200, "reasoning_steps": 4 },
    { "task_id": "t3", "agent": "ContradictionDetector", "status": "complete", "resolution": "source_a_wins", "confidence": 0.87 },
    { "task_id": "t4", "agent": "ActionPlanner", "status": "complete", "actions_generated": 4, "actions_rejected": 1 },
    { "task_id": "t5", "agent": "Executor", "status": "partial_failure", "failed_action": "a3", "recovery": "retry_succeeded" }
  ],
  "total_duration_ms": 4820,
  "total_cost_usd": 0.034
}
```

### 8.4 Antigravity Dashboard Display
- [ ] In the app's War Room screen, a collapsible "Antigravity Trace" panel shows live task log
- [ ] Each task appears as it completes, with status badge and duration
- [ ] Full trace exportable as JSON for judges to review

---

## 📐 PHASE 9 — DEMO SCENARIO (HACKATHON DEMO)
*(Day 4–5 — ~4 hours)*

### 9.1 Scenario: "Inventory Crisis at Finora Textiles SME"

**Setup (pre-loaded mock data)**:
1. `warehouse_sheet.csv` — shows 500 units remaining (dated 3 weeks ago) ← STALE
2. `supplier_email.pdf` — warns of supply chain disruption (dated yesterday)
3. `sales_dashboard.json` — shows 180 units/day velocity (last 7 days)
4. `customer_complaints.csv` — 23 complaints about delayed delivery (last 48 hours)
5. `logistics_news_url` — article about port strikes affecting textile imports
6. **BONUS**: `handwritten_ledger.jpg` — Ghost Ledger photo showing actual stock count

**Demo Flow** (3–5 min):
1. User opens app, taps "New Analysis", adds all 6 sources including ledger photo
2. Ghost Ledger scan: Gemini reads handwritten stock count, shows confidence halos
3. War Room activates: agents reasoning in real time
4. **Contradiction detected**: warehouse CSV says 500 units, Ghost Ledger shows 287 units → red flash
5. Resolution: warehouse CSV marked stale (3 weeks old), Ghost Ledger wins (higher recency + manual verification)
6. Insight: "At 180 units/day, stockout in 1.6 days. Supplier reliability: LOW."
7. Action chain generated:
   - A1: Validate stock (cross-reference Ghost Ledger count) ✅
   - A2: Notify procurement manager (WhatsApp/email draft) ✅
   - A3: Simulate emergency order (PKR 500k budget) — BUDGET EXCEEDED, auto-reduced to PKR 350k ✅
   - A4: Update customer delivery estimates (+5 days) ✅
   - A5: Schedule 24-hour monitoring alert ✅
8. Execution tracker: A3 retry shown (API failure → retry success)
9. Outcome: stockout risk reduced from CRITICAL to MODERATE, all actions logged on blockchain
10. What-If: "What if we expedite shipping at double cost?" → Scenario Battle shows 3 options

### 9.2 Stress Test Moments to Show
- [ ] Show A3 budget constraint rejection and auto-modification
- [ ] Show warehouse CSV being down-ranked (stale source)
- [ ] Show A2 failing on first attempt (simulated API failure) → retry → success
- [ ] Show Antigravity trace panel with all steps

---

## 📐 PHASE 10 — POLISH, TESTING & DEPLOYMENT
*(Day 5 — ~8 hours)*

### 10.1 Testing
- [ ] Unit tests: each agent node (pytest + LangGraph test utils)
- [ ] Integration tests: full session flow with mock data
- [ ] Load test: 10 concurrent sessions (ensure Redis caching works)
- [ ] Android: UI tests with Espresso for critical flows

### 10.2 Performance Targets
| Operation | Target Latency |
|---|---|
| PDF ingestion | < 2s |
| Full analysis session (5 docs) | < 15s |
| Monte Carlo simulation (1000 paths) | < 3s |
| Ghost Ledger OCR | < 5s |
| WebSocket first event | < 1s |

### 10.3 Cost Targets (per session)
| Service | Estimated Cost |
|---|---|
| Gemini Pro (analysis) | ~$0.02 |
| Gemini Vision (OCR) | ~$0.005 |
| Blockchain tx (Mumbai testnet) | $0.00 (testnet) |
| Total per session | ~$0.025 |

### 10.4 Deployment (Free Tier)
- [ ] FastAPI → Google Cloud Run (free tier: 2M req/month)
- [ ] PostgreSQL → Supabase free tier
- [ ] Redis → Upstash free tier
- [ ] n8n → n8n Cloud free tier (5 active workflows)
- [ ] Ollama/Gemma → Cloud Run with GPU (or disable for demo, use Gemini only)
- [ ] Android APK → direct APK install (no Play Store needed for hackathon)

### 10.5 README Sections
- [ ] Architecture diagram (Mermaid)
- [ ] Data sources list
- [ ] Antigravity integration explanation
- [ ] Assumptions & constraints
- [ ] Cost/latency analysis table
- [ ] Baseline comparison (vs manual CFO analysis)
- [ ] Known limitations
- [ ] Setup instructions (Docker Compose one-liner)

---

## 📐 PHASE 11 — DEMO VIDEO SCRIPT
*(Day 5 — ~2 hours)*

### 11.1 Video Structure (4 minutes)
- **0:00–0:30** — Hook: "70% of startups fail from cashflow blindness. Finora sees everything."
- **0:30–1:00** — Ghost Ledger demo (camera scan of handwritten ledger, confidence halos)
- **1:00–2:00** — Full analysis session: 6 sources ingested, War Room activates, contradiction detected
- **2:00–3:00** — Action chain: constraint rejection, execution, one failure+recovery
- **3:00–3:30** — What-If Scenario Battle: 3 scenarios race, winner declared
- **3:30–4:00** — Audit Trail: blockchain proof, Antigravity trace shown

---

## ✅ MASTER CHECKLIST (Priority Order)

### 🔴 Must-Have (Core Demo)
- [ ] FastAPI skeleton + PostgreSQL running in Docker
- [ ] 5 ingestion endpoints (PDF, URL, CSV, real-time, ledger)
- [ ] LangGraph 6-agent pipeline (Orchestrator, Ingestion, Insight, Contradiction, Action, Executor)
- [ ] Contradiction detection with staleness scoring
- [ ] Action chain with constraint validation
- [ ] Rollback/retry on action failure
- [ ] Monte Carlo simulation (QuantLib)
- [ ] WebSocket real-time streaming to Android
- [ ] Android: Home, Analysis, War Room, Action Chain, Execution Tracker screens
- [ ] Antigravity integration + trace logs
- [ ] Mock demo scenario data pre-loaded

### 🟡 Should-Have (Differentiators)
- [ ] Ghost Ledger OCR pipeline (YOLOv8 + Tesseract + Gemini Vision)
- [ ] Ghost Ledger review screen with confidence halos
- [ ] Scenario Battle View (War Room for simulations)
- [ ] What-If NL query interface
- [ ] Blockchain audit logger (Mumbai testnet)
- [ ] Audit Trail screen in app

### 🟢 Nice-to-Have (Polish)
- [ ] Stress-O-Meter gauge animation
- [ ] Audio heartbeat in War Room
- [ ] Export audit PDF
- [ ] n8n nightly digest workflow
- [ ] Offline caching (Room DB)

---

## 🧑‍💻 TEAM TASK SPLIT (Suggested)

| Member | Focus Area |
|---|---|
| Lead AI Engineer | LangGraph agents + Gemini integration + Antigravity |
| Backend Engineer | FastAPI endpoints + PostgreSQL + n8n workflows |
| Android Engineer | Kotlin app — all screens + WebSocket + CameraX |
| Full-Stack / Blockchain | QuantLib simulations + Solidity contract + Web3 bridge |
| All | Demo scenario data prep + video recording |

---

*Last updated: May 2026 · FinoraAI · AI Seekho Hackathon 2026*
