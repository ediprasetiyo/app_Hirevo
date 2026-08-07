# UI Wireframes — Hirevo HRIS

Deskripsi tekstual wireframe untuk setiap screen utama. Sebagai blueprint untuk designer (Figma) & developer (Next.js + Flutter).

**Design system:**
- **Web**: shadcn/ui (Radix + Tailwind), Inter font, neutral palette + brand accent (configurable per tenant).
- **Mobile**: Material 3 design tokens (Flutter), bottom nav.
- **Theme**: light + dark.
- **Spacing**: 4px base unit.
- **Breakpoints**: sm 640, md 768, lg 1024, xl 1280, 2xl 1536.

---

## A. Layout Conventions

### A.1 Web (HR Backoffice)
```
┌────────────────────────────────────────────────────────────┐
│ TopBar: [Logo] [Search ⌘K]      [Tenant▾] [Bell] [Avatar] │
├──────┬─────────────────────────────────────────────────────┤
│      │ Breadcrumb > Section > Page                         │
│ Nav  ├─────────────────────────────────────────────────────┤
│ side │                                                     │
│ bar  │  Page Content                                       │
│      │                                                     │
│      │                                                     │
└──────┴─────────────────────────────────────────────────────┘
```
- Sidebar collapsible, sticky.
- Top bar: cmd-K command palette (global search + actions).
- Right drawer untuk approval inbox quick-view.

### A.2 Mobile (Employee Self-Service)
```
┌─────────────────────┐
│ AppBar              │
│ (page title + ⋮)    │
├─────────────────────┤
│                     │
│ Content (scroll)    │
│                     │
│                     │
├─────────────────────┤
│ [🏠][⏰][💼][🔔][👤] │   ← Bottom Nav
└─────────────────────┘
```
- Home · Attendance · Modules · Notifications · Profile
- FAB (Floating Action Button): "Clock In" (saat di Home/Attendance).

---

## B. Web Wireframes

### B.1 Sign Up Tenant (Public)
**Single page, 2-step:**
1. Email + password + company name + employee count.
2. Verify email (code 6 digit) → auto-login → onboarding wizard.

**Components:**
- Big hero left (value props 3 bullet).
- Form card right (shadow-lg, max-w-md).
- Trust badges bottom (BPJS-ready, PPh 21 TER 2024, PSE Kominfo).

---

### B.2 Login Screen
**Components:**
- Center card: email + password + "Continue with WebAuthn" + "Forgot password".
- After password: MFA challenge screen (TOTP input 6-digit boxes OR WebAuthn prompt).
- "Trust this device for 30 days" checkbox.

---

### B.3 Onboarding Wizard
**5 steps with progress bar:**
1. **Company Info**: name, NPWP, NIB, industry, logo upload, address.
2. **Branches**: add 1+ branch (name, address, head office flag).
3. **Invite Team**: email + role chips.
4. **Salary Components Template**: choose preset (Manufacturing / Retail / Office) OR custom.
5. **Upload Employees**: download template → upload xlsx → preview → confirm.

Each step: Next/Back, Save & Exit.

---

### B.4 HR Dashboard (Home)
**Layout — grid 12 columns:**

```
┌────────────────────────────────────────────────────────────────────┐
│ Welcome back, Edi 👋 — Acme Corp · Jul 2026                        │
├──────────────────────────────────────────────────────────────────────
│ Setup Checklist [85%] ─────────────────────────────────             │
├──────────────────────────────────────────────────────────────────────
│ [Headcount: 248]  [Attendance Today: 95%]  [On Leave: 12]  [Late: 5]│
├────────────────────────────────────┬───────────────────────────────┤
│ Payroll Cost (last 6 mo) [line]    │ Headcount by Dept [donut]     │
│                                    │                               │
├────────────────────────────────────┴───────────────────────────────┤
│ Recent Activity Feed                                                │
│ • Rudi clocked in late (8:15)                                      │
│ • Payroll Jun 2026 approved by Sari                                │
│ • New hire: Ahmad — Software Engineer                              │
├──────────────────────────────────────────────────────────────────────
│ Upcoming: 🎂 3 birthdays · 📅 5 contract expiry · 🏖 12 leave    │
└────────────────────────────────────────────────────────────────────┘
```

**Filters top right:** branch, period.

---

### B.5 Employee List
**Components:**
- Table dengan column: avatar+name, employee_no, position, department, status badge, hire_date, actions (⋮).
- Filters bar: status, branch, dept, search (autocomplete).
- Bulk actions: assign role, export, send notif.
- Right side: add button + import button.
- Row click → drawer slide-in dengan profile preview.

---

### B.6 Employee Detail Page
**Layout — sidebar tabs + main:**

```
┌─────────────────────────────────────────────────────────┐
│ [< Back] Rudi Hartono [EMP001]            [Edit] [⋮]   │
│ Software Engineer · Technology · Active                 │
├──────────┬──────────────────────────────────────────────┤
│ Personal │ Personal Info                                │
│ Employ.  │  NIK: 32**********  NPWP: 12.345.678.9-...  │
│ Tax      │  DOB: 15/05/1990   Phone: 0812*****890      │
│ BPJS     │  Address: ...                                │
│ Bank     │  ...                                         │
│ Documents│                                              │
│ Salary   │                                              │
│ History  │                                              │
│ Family   │                                              │
└──────────┴──────────────────────────────────────────────┘
```

**Per tab:** view + edit inline. Salary tab requires re-auth (sensitive).

---

### B.7 Org Chart
**Visual interactive tree** (D3 / react-flow):
- Drag node to re-parent.
- Click node → side panel with summary.
- Zoom in/out.
- Filter by branch.
- Toggle: name only / name + position / name + position + photo.

---

### B.8 Attendance Module — Admin View

**Top tabs:** Logs · Live · Anomalies · Locations · Shifts

**Logs tab:**
- Table: date, employee, in time, out time, location, status, fraud_score badge.
- Calendar view toggle (heatmap presence).
- Filters: date range, branch, status, fraud_score >= X.

**Anomalies tab:**
- Card list of flagged attendance with photo, map snippet, fraud signals listed.
- Inline Approve / Reject.

**Locations tab:**
- Map (Leaflet) with markers + radius circles.
- Side panel: list locations, add new (click on map).

---

### B.9 Payroll Module — Run Payroll

**Page: "Payroll Runs"**
- List runs (table): period, status, total_net, employees count, approved_by, actions.

**New Run flow (multi-step modal):**
1. Select period + company + scope.
2. Click "Calculate" → progress bar.
3. Review summary card:
   ```
   Periode: Juli 2026
   Karyawan: 248
   Total Gross:  Rp  2.450.000.000
   Total Lembur: Rp     85.000.000
   Total BPJS:   Rp    178.500.000
   Total PPh21:  Rp     95.250.000
   Total Net:    Rp  2.241.250.000
   ```
4. Slip preview list (right-scroll).
5. Request Approval → notify approver.
6. Approver: review summary again → Approve (with re-auth) or Reject.
7. Post-approval: bank file download + slip sent.

**Edge cases UI:**
- Per-employee error banner (red badge on row).
- "Recalculate this employee" inline.
- "Reopen period" — destructive button with confirm + reason.

---

### B.10 Payslip Detail
```
┌─────────────────────────────────────────────────────────┐
│ SLIP GAJI · Juli 2026                  [Print] [Download]│
├─────────────────────────────────────────────────────────┤
│ Acme Corp                Rudi Hartono [EMP001]          │
│ Periode: 1-31 Jul 2026   Software Engineer              │
├─────────────────────────────────────────────────────────┤
│ EARNINGS                  | DEDUCTIONS                  │
│ Gaji Pokok    8.000.000   | BPJS Kes (1%)      80.000  │
│ T. Jabatan    1.500.000   | BPJS JHT (2%)     160.000  │
│ T. Transport    500.000   | BPJS JP (1%)       80.000  │
│ Lembur          750.000   | PPh 21            425.000  │
│ ──────────                | Pinjaman          500.000  │
│ Gross        10.750.000   | ──────────                  │
│                           | Total Deduc.   1.245.000   │
├─────────────────────────────────────────────────────────┤
│ NET RECEIVED:  Rp 9.505.000                             │
│ Transfer to: BCA ****1234                               │
└─────────────────────────────────────────────────────────┘
```

---

### B.11 Reimbursement — Submit (Web)
**Page:**
- Form: title, category dropdown.
- "Add Item" → modal: date, amount, description, **drop receipt photo here** (drag-drop).
- After drop: OCR loading spinner → auto-fill fields, highlight low-confidence.
- Multiple items support.
- Submit → workflow.

---

### B.12 Reimbursement — Approval (Manager)
**Page: Inbox**
- Card list, per card: requester, category, amount, fraud_score badge (color).
- Click → modal with full detail + receipt thumbnails + fraud signals listed.
- Approve / Reject / Forward.

---

### B.13 Leave Request List
- Calendar view (per-team) + list view toggle.
- Filters: status, leave_type, date range.
- "Submit Request" button → modal form.

---

### B.14 Reports — Custom Report Builder (Phase 2)
- Left panel: entity picker + field list (drag).
- Middle: drop zones (Columns / Rows / Filters / Group By).
- Right: live preview chart/table.
- Top: save, share, schedule, export.

---

### B.15 Settings
**Sections (sidebar):**
- General (company, locale, time zone)
- Branding (logo, color)
- Workspace Members (users + roles)
- Workflows (approval builder)
- Notifications (channels, templates)
- Integrations (Accurate, FCM, WhatsApp, SSO SAML/OIDC)
- Security (MFA policy, IP allowlist, session timeout)
- Billing & Plan (current usage, invoices, upgrade)
- API Keys & Webhooks
- Audit Log Viewer

---

### B.16 Approval Inbox (Right Drawer / Dedicated Page)
**Tabs:** Leaves · Overtime · Reimbursement · Loans · Misc
**Card per item:** requester avatar, summary, amount/date, age.
**Bulk select** + Approve/Reject buttons.
**Filter by module + date.**

---

## C. Mobile Wireframes (Flutter)

### C.1 Home Screen
```
┌────────────────────────┐
│ 🌤 Selamat pagi, Rudi  │
│ Senin, 1 Juli 2026     │
├────────────────────────┤
│ ┌──────────────────┐   │
│ │   📍 CLOCK IN    │   │  ← Big card, gradient
│ │ Belum absen hari │   │
│ │  ini · Tap here  │   │
│ └──────────────────┘   │
├────────────────────────┤
│ Pengumuman                │
│ • Libur tgl 17 Agustus    │
│ • Survey engagement 2026  │
├────────────────────────┤
│ Quick Actions          │
│ [Cuti] [Lembur] [Reim] │
│ [Slip] [Loan] [Asset]  │
├────────────────────────┤
│ [🏠][⏰][💼][🔔][👤]    │
└────────────────────────┘
```

---

### C.2 Clock-In Screen (Camera + GPS)
**Full-screen camera preview (front-facing)** with overlay:
- Oval face guide (helps positioning).
- Bottom: live GPS coords + accuracy + "Within Kantor Pusat ✓".
- Liveness prompt: "Kedipkan mata" (animated icon).
- Auto-capture after liveness pass.
- Show progress: "Uploading...", "Verifying...", success animation.

**Failure states:**
- Wajah tidak cocok (retry button).
- Lokasi di luar kantor (show map with current location vs office).
- Mock GPS detected (red alert + contact HR).

---

### C.3 Attendance History (Mobile)
- Month calendar (heatmap by presence color).
- Tap day → detail sheet (in time, out time, photo, location, status).
- "Ajukan koreksi" link.

---

### C.4 Leave Request (Mobile)
**Stepper form:**
1. Pilih jenis cuti (chip list + saldo each).
2. Pilih tanggal (range picker).
3. Tulis alasan + attach (optional).
4. Confirm summary → Submit.

After submit: status tracker visual (Pending → Manager → HR → Approved).

---

### C.5 Slip Gaji (Mobile)
- List by month (with thumbnail of total net).
- Tap → detail screen (mirror web layout, simplified).
- Buttons: Share, Download PDF, Email.

---

### C.6 Reimbursement Submit (Mobile)
**Big "+" FAB → Camera launches.**
- Take photo → OCR loading → result modal with editable fields.
- "Add another item" or "Submit".

---

### C.7 AI Chatbot (Mobile)
**Chat UI** like WhatsApp:
- Bubble assistant kiri, user kanan.
- Suggested prompts chip bottom: "Sisa cuti", "Slip terakhir", "Pinjam dana".
- Citation chips: tap → opens doc snippet.
- Voice input button.

---

### C.8 Employee Directory (Mobile)
- Search bar top.
- List avatar + name + position + phone/email/WA quick actions.
- Click → detail (no salary visible).
- Org chart access button.

---

### C.9 Profile (Mobile)
**Sections:**
- Personal info (read-only critical, editable phone/address).
- Documents (list + view PDF in-app).
- Settings (notif preferences, language ID/EN, dark mode, biometric login).
- Trusted devices (list + revoke).
- Logout.

---

### C.10 Notifications (Mobile)
- Sectioned by today / earlier.
- Per item: icon by category, title, body, time.
- Tap → deep-link to relevant screen.
- Swipe to mark read / dismiss.

---

## D. Component Library Notes (shadcn + Flutter)

### Web (shadcn/ui):
- **Form**: react-hook-form + zod resolver.
- **Table**: TanStack Table + virtualization.
- **Charts**: Recharts (simple) + ECharts (advanced).
- **Date**: react-day-picker.
- **Notifications**: sonner (toast).
- **Command palette**: cmdk.
- **Drag-drop**: dnd-kit.
- **Map**: leaflet + react-leaflet.

### Mobile (Flutter):
- **State**: Riverpod 2.x.
- **HTTP**: Dio + retry interceptor + auth interceptor.
- **Storage**: Hive (offline cache) + flutter_secure_storage (tokens).
- **Camera**: camera + image_picker.
- **ML**: google_ml_kit (face detection + on-device liveness).
- **GPS**: geolocator + flutter_background_geolocation.
- **Push**: firebase_messaging.
- **PDF view**: syncfusion_flutter_pdfviewer.
- **Charts**: fl_chart.
- **Map**: flutter_map (Leaflet-based).
- **i18n**: easy_localization.

---

## E. Accessibility Checklist (all screens)
- Contrast ratio ≥ 4.5:1.
- All interactive elements keyboard-accessible (web).
- Screen reader labels.
- Font size scalable (mobile respects system text scale).
- Color not sole indicator (also icon / text).
- Form errors announced via aria-live.
- Focus visible.

---

## F. Empty / Error / Loading States (mandatory)
Every list page has:
- **Loading**: skeleton (not spinner).
- **Empty**: illustration + 1-line description + CTA.
- **Error**: friendly message + Retry + Contact support.

Example empty state:
> *(illustration of empty calendar)*
> Belum ada cuti diajukan.
> **[Ajukan Cuti]**

---

## G. Wireframe Deliverables for Designer
Designer akan produce Figma file dengan:
1. **Design tokens** (colors, type, spacing).
2. **Component library** (variants & states).
3. **Screen designs** (desktop + mobile) untuk setiap screen di atas.
4. **Prototype** clickable untuk 3 critical flows: clock-in, payroll run, reimbursement submit.
5. **Handoff** specs (Tokens Studio + dev mode).
