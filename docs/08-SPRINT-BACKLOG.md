# Sprint Backlog — Hirevo HRIS MVP

**Methodology:** Scrum, 2-week sprints, 12 sprints = 24 weeks (~6 bulan).
**Velocity Target:** 30 story points/sprint per cross-functional squad (tim: 4 BE + 2 FE + 2 Mobile + 1 QA + 1 PM/PO).
**Definition of Ready (DoR):** Story has AC, design ref, dependencies cleared, points estimated.
**Definition of Done (DoD):**
- Code merged to `main` via PR (≥1 approval).
- Unit test coverage ≥ 80%, mutation score ≥ 60% for critical paths.
- Integration test (Testcontainers) for happy path + 2 edge cases.
- API spec updated (springdoc-openapi).
- Liquibase changeset added if schema change.
- E2E test (Playwright web / Patrol mobile) for user-facing.
- Deployed to staging via ArgoCD.
- QA sign-off.
- Documentation updated (user-facing + technical).
- No P0/P1 bugs open.

**Story ID** mengacu ke [03-USER-STORIES.md](03-USER-STORIES.md).

---

## Sprint 0 — Foundation (2 weeks, before sprint 1)
**Pre-sprint setup, tidak hitung velocity.**

- Repo monorepo setup (Maven multi-module + Turborepo for FE/Mobile).
- Local dev: Docker Compose (Postgres, Redis, Kafka, OpenSearch, MinIO).
- CI pipeline (GitHub Actions): lint, test, build, image push, sign.
- K8s cluster staging (EKS) provisioned via Terraform.
- ArgoCD installed, deploys `hello-world` service.
- Liquibase baseline migration.
- Spring Boot template service (with security, observability, tenant context filter).
- shadcn/ui scaffold + login page.
- Flutter scaffold + login page.
- Design system v0 in Figma.

**Deliverable:** End-to-end dev loop works: code → commit → CI → staging.

---

## Sprint 1 — Authentication Foundation [P0]
**Sprint Goal:** User dapat sign up tenant, login (password + MFA), dan invite member.

| Story | Pts | Owner |
|-------|-----|-------|
| US-001 Sign up tenant | 5 | BE+FE |
| US-002 Login dengan MFA (TOTP) | 8 | BE+FE |
| US-005 Invite & manage users | 5 | BE+FE |
| US-003 Device binding (basic) | 5 | BE+Mobile |
| Tech: JWT infra, refresh token rotation | 5 | BE |
| Tech: Spring Security config + filter chain | 3 | BE |
| **Total** | **31** | |

**Notable:**
- Password Argon2id, TOTP secret encrypted via KMS.
- Refresh token storage di Redis (rotating).
- Email service abstraction (SES untuk prod, MailHog untuk dev).

**Risks:**
- WebAuthn registration in browser quirks → defer to sprint 2.

---

## Sprint 2 — Tenant Multi-Tenancy + RBAC [P0]
**Sprint Goal:** RLS aktif, RBAC berfungsi, super_admin bisa kelola roles.

| Story | Pts | Owner |
|-------|-----|-------|
| Tenant resolver middleware (subdomain → context) | 5 | BE |
| RLS policy + DataSourceInterceptor | 5 | BE |
| Roles & permissions CRUD | 5 | BE+FE |
| Permission catalog seed (40+ permissions) | 2 | BE |
| @PreAuthorize custom evaluator (RBAC+ABAC) | 5 | BE |
| US-004 Onboarding wizard (skeleton) | 3 | FE |
| WebAuthn enrollment + verify | 5 | BE+FE |
| **Total** | **30** | |

---

## Sprint 3 — Employee Management Core [P0]
**Sprint Goal:** CRUD employee + contract + struktur organisasi.

| Story | Pts | Owner |
|-------|-----|-------|
| US-010 Create employee | 8 | BE+FE |
| US-012 Edit dengan audit log | 3 | BE |
| US-013 Org chart visual | 5 | FE |
| Org structure CRUD (company/branch/dept/position/job_level) | 5 | BE+FE |
| Field-level encryption (NIK, NPWP, bank) | 5 | BE |
| Audit log infra (Hibernate Envers + Kafka) | 5 | BE |
| **Total** | **31** | |

---

## Sprint 4 — Employee Onboarding + Documents [P0]
**Sprint Goal:** Bulk import + dokumen upload jalan.

| Story | Pts | Owner |
|-------|-----|-------|
| US-011 Bulk import via Excel | 5 | BE+FE |
| US-014 Upload dokumen | 3 | BE+FE |
| US-015 Resign / terminate flow | 5 | BE+FE |
| Document service (S3 + signed URL + virus scan ClamAV) | 5 | BE |
| US-004 Onboarding wizard (complete) | 5 | FE |
| Employee mobile: view own profile | 3 | Mobile |
| Bulk import: async job + progress | 3 | BE |
| **Total** | **29** | |

---

## Sprint 5 — Attendance Foundation [P0] ⭐
**Sprint Goal:** Karyawan bisa absen mobile dengan GPS, geofence works.

| Story | Pts | Owner |
|-------|-----|-------|
| US-022 Geofencing + work_locations | 5 | BE+FE |
| US-024 Clock-out + duration | 3 | BE+Mobile |
| Attendance schema + RLS + partition | 3 | BE |
| Mobile: GPS + camera permission + selfie capture | 5 | Mobile |
| Mobile: clock-in API + offline cache | 5 | Mobile |
| US-025 History view mobile (calendar) | 5 | Mobile |
| Admin: attendance logs list + filter | 3 | FE |
| **Total** | **29** | |

**Note:** Face recognition + anti-mock = next sprint (heavy ML work).

---

## Sprint 6 — Attendance Anti-Fraud [P0] ⭐
**Sprint Goal:** Face match + liveness + anti-mock GPS aktif.

| Story | Pts | Owner |
|-------|-----|-------|
| US-021 Face enrollment (3 angle, quality check) | 5 | BE+Mobile |
| US-020 Face recognition match server-side (ArcFace via ONNX Runtime) | 8 | BE |
| Mobile: TFLite liveness (blink+head) | 5 | Mobile |
| US-023 Anti-mock GPS + jailbreak detection | 5 | Mobile |
| Server: speed teleport + IP geo cross-check | 3 | BE |
| Anomaly review dashboard | 3 | FE |
| Fraud_score aggregation logic | 2 | BE |
| **Total** | **31** | |

**Risk:** Self-hosted ArcFace inference perf. POC week-1, fallback Vendor (AWS Rekognition) di week-2 if needed.

---

## Sprint 7 — Leave Management [P0] + Attendance Polish
**Sprint Goal:** Cuti end-to-end + lembur otomatis.

| Story | Pts | Owner |
|-------|-----|-------|
| US-034 Master leave_types seed UU 13/2003 | 3 | BE |
| US-030 Pengajuan cuti | 5 | BE+FE+Mobile |
| US-032 Saldo otomatis (prorata + carry-over) | 5 | BE |
| US-033 Kalender tim | 3 | FE |
| US-026 Lembur form + PP 35 calc | 5 | BE+FE |
| US-028 WA bot clock-in (basic) | 5 | BE |
| Holidays master + Indonesia 2026-2027 seed | 2 | BE |
| **Total** | **28** | |

---

## Sprint 8 — Workflow Engine + Leave Approval [P0]
**Sprint Goal:** Approval engine generic; cuti & lembur lewat workflow.

| Story | Pts | Owner |
|-------|-----|-------|
| US-140 Workflow CRUD + builder UI | 8 | BE+FE |
| US-141 Approval inbox terpusat | 5 | BE+FE+Mobile |
| US-142 Delegate approval | 3 | BE+FE |
| US-031 Approval cuti integrated dgn workflow | 5 | BE |
| Lembur approval | 3 | BE |
| Push notif via FCM | 5 | BE+Mobile |
| **Total** | **29** | |

---

## Sprint 9 — Payroll Engine (Heavy) [P0] 🔥
**Sprint Goal:** Payroll bisa di-calc end-to-end, akurat secara fungsional.

| Story | Pts | Owner |
|-------|-----|-------|
| US-041 Salary components (formula engine JSONLogic) | 8 | BE |
| US-042 Salary structure + history | 5 | BE+FE |
| US-040 Payroll run state machine + async Kafka | 8 | BE |
| US-043 PPh 21 TER engine + 100 golden tests | 8 | BE |
| Rule-pack versioned (TER, PTKP, brackets) seed | 3 | BE |
| **Total** | **32** | |

**4 BE devs all-hands.** Tax consultant review week-2.

---

## Sprint 10 — Payroll Complete [P0]
**Sprint Goal:** BPJS + THR + lembur in payroll + slip PDF + bank file.

| Story | Pts | Owner |
|-------|-----|-------|
| US-044 BPJS 5 program + 50 tests | 8 | BE |
| US-046 Lembur auto-pull from attendance | 3 | BE |
| US-045 THR engine (PP 36/2021) | 5 | BE |
| US-047 Slip PDF generator (iText / OpenHTMLtoPDF) | 5 | BE |
| Send slip: email + push + WA | 3 | BE |
| US-049 Bank file: BCA, Mandiri, BRI, BNI, CIMB (strategy pattern) | 5 | BE |
| US-058 Mobile slip gaji view + download | 3 | Mobile |
| **Total** | **32** | |

---

## Sprint 11 — Reimbursement + Loan + Self-Service Polish
**Sprint Goal:** Reimbursement (OCR + fraud) + Loan request.

| Story | Pts | Owner |
|-------|-----|-------|
| US-064 Kategori reimbursement | 2 | BE+FE |
| US-060 Submit reimbursement + OCR | 8 | BE+Mobile |
| US-061 Fraud detection (pHash + ELA + heuristics) | 8 | BE |
| US-062 Approval reimbursement (workflow) | 2 | BE |
| US-063 Settle via payroll | 3 | BE |
| US-070 Loan request + eligibility | 5 | BE+FE+Mobile |
| US-071 Auto-deduct cicilan di payroll | 3 | BE |
| **Total** | **31** | |

---

## Sprint 12 — AI Assistant + Dashboard + Bukti Potong + Hardening [P0]
**Sprint Goal:** MVP ready. AI chatbot basic, dashboard, BP 1721-A1, prod-hardening.

| Story | Pts | Owner |
|-------|-----|-------|
| US-048 Bukti Potong 1721-A1 + e-Bupot XML | 8 | BE+FE |
| US-130 HR Chatbot basic (Claude Haiku + RAG pgvector) | 8 | BE+FE+Mobile |
| US-132 OCR doc reader (KTP/NPWP) | 5 | BE |
| US-120 HR Dashboard widgets | 5 | BE+FE |
| Audit log search UI + export | 3 | FE |
| Sec hardening: pen-test fix, rate limit final tuning | 3 | BE+DevOps |
| Load test: 10K payroll run < 5min | 2 | DevOps |
| **Total** | **34** | |

**Sprint Goal:** Code freeze week-2, regression test, prep beta launch.

---

## MVP Beta Launch (week 25–28)
- Onboard 10 design partner tenants.
- Daily standup + bugfix sprints.
- Tax consultant final review.
- PSE Kominfo registration.
- Marketing site live.

---

## Sprint Summary

| Sprint | Theme | Pts | Cumulative |
|--------|-------|-----|------------|
| 0 | Foundation | — | — |
| 1 | Auth | 31 | 31 |
| 2 | Tenant + RBAC | 30 | 61 |
| 3 | Employee Core | 31 | 92 |
| 4 | Onboarding + Docs | 29 | 121 |
| 5 | Attendance Base | 29 | 150 |
| 6 | Attendance Anti-Fraud | 31 | 181 |
| 7 | Leave + Lembur | 28 | 209 |
| 8 | Workflow Engine | 29 | 238 |
| 9 | Payroll + PPh | 32 | 270 |
| 10 | Payroll Complete | 32 | 302 |
| 11 | Reim + Loan | 31 | 333 |
| 12 | AI + Dashboard + Hardening | 34 | 367 |
| **TOTAL** | — | **367** | — |

Buffer 9 pts (untuk story carry-over).

---

## Risk Register per Sprint

| Sprint | Risk | Mitigation |
|--------|------|------------|
| 1 | WebAuthn browser support | Polyfill + fallback TOTP |
| 5 | Mobile camera/GPS perf on low-end devices | Test on Galaxy A10 / Redmi 9A |
| 6 | Face model accuracy < 85% on Indonesian faces | Curate ID-faces test set |
| 6 | Self-host ArcFace too slow on CPU | GPU node pool or vendor fallback |
| 9 | PPh 21 TER edge cases (resign mid-year, non-NPWP) | Tax consultant pair-programming session |
| 10 | Bank file format vary across banks | Strategy pattern + validate with real bank file samples |
| 11 | OCR accuracy struk Indonesia | Train PaddleOCR with curated dataset; vendor fallback |
| 12 | Load test 10K karyawan timeout | Optimize: batch insert, parallelism, Hikari pool tuning |

---

## Cross-Sprint Tasks (continuous)

| Activity | Owner | Cadence |
|----------|-------|---------|
| Bug triage | PM | Daily 10min |
| Tax consultant review | BE Lead | Weekly Fri |
| Design review | Designer | 2x/sprint |
| Sprint planning | All | Mon week-1 |
| Sprint review + demo | All | Fri week-2 |
| Retrospective | All | Fri week-2 |
| Backlog grooming | PM + Tech Lead | Wed week-1 |
| Tech debt sprint | All | Every 6 sprints (1 week) |
| Security review | BE Lead + DevOps | Per release |
| Performance test | DevOps | Sprint 9, 10, 12 |

---

## Post-MVP Phase 2 Sprint Snapshot (Sprints 13-22)

| Sprint | Theme | Notable Stories |
|--------|-------|-----------------|
| 13-14 | Recruitment ATS Core | US-080 to 086, career page |
| 15 | AI Screen CV + Interview Scheduling | US-082, US-084 |
| 16-17 | Performance Mgmt OKR + Review | US-090 to 093 |
| 18 | Performance 360 + 1-on-1 | US-092, US-094 |
| 19 | Asset Mgmt | US-100 to 104 |
| 20 | Custom Report Builder | US-122 |
| 21 | AI Advance: Payroll Q&A + Fraud Bot | US-131, US-133 |
| 22 | Integration: Accounting + DJP/BPJS API | Direct e-Bupot submit, Accurate sync |

## Phase 3 (Sprints 23-27) — Enterprise

| Sprint | Theme |
|--------|-------|
| 23-24 | SSO SAML/OIDC + IT Admin tools |
| 25 | On-Prem deployment package |
| 26 | Multi-region active-active + Aurora Global |
| 27 | SOC 2 Type II audit prep |
