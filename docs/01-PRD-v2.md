# PRD v2 — Hirevo HRIS

**Version:** 2.0
**Tanggal:** 2026-06-16
**Status:** Approved for development
**Supersedes:** [PRD v1](../PRD.md)

---

## 1. Executive Summary

**Hirevo HRIS** adalah SaaS HRIS enterprise-grade multi-tenant untuk pasar Indonesia (UMKM s/d Enterprise). Diferensiasi: compliance Indonesia **out-of-the-box** (PPh 21 TER, BPJS, UU 13/2003 + Cipta Kerja, PP 35/2021), **mobile-first**, **AI-powered**, dan **anti-fraud** (face recognition + anti-mock GPS untuk attendance, OCR + duplicate detection untuk reimbursement).

### 1.1 Goals
- Otomasi 90% admin HR (attendance, payroll, leave, BPJS, PPh 21).
- Compliance ID **versioned rule-pack** — update tarif tanpa redeploy.
- Multi-tenant aman (RLS + per-tenant encryption keys).
- Mobile-friendly untuk karyawan lapangan (Flutter, offline-capable).
- AI assistant: HR chatbot, payroll Q&A, OCR receipt, fraud detection.

### 1.2 Non-Goals (MVP)
- Akuntansi GL penuh (integrate ke Accurate/Jurnal, bukan rebuild).
- Project timesheet billable.
- Personal finance / pinjaman P2P (Loan modul hanya pinjaman internal karyawan).

---

## 2. Personas

| Persona | Segmen | Pain | Hirevo Value |
|---------|--------|------|--------------|
| **Bu Sari** — Owner UMKM (12 kary, toko retail) | UMKM | Pakai Excel, takut salah PPh 21 | Onboarding 1 jam, payroll auto, free up to 5 kary |
| **Pak Andi** — HR Manager (200 kary, manufaktur) | SMB | Rekrut aktif, payroll kompleks (shift, lembur) | ATS, payroll engine, integrasi Accurate |
| **Ibu Linda** — HR Director (3000 kary, 8 cabang) | Enterprise | Approval matrix kompleks, audit | Workflow builder, SSO, audit trail 5 thn, dedicated infra |
| **Rudi** — Karyawan operator pabrik | All | Absen ribet, slip gaji susah | Mobile app + WA bot, slip gaji push notif |
| **Pak Budi** — Direktur | All | Visibilitas headcount & cost | Mobile dashboard real-time |
| **Mas Doni** — IT Admin tenant | Enterprise | SSO, audit, security | SAML/OIDC, device binding, MFA wajib admin |

---

## 3. Scope — 14 Modul

| # | Modul | MVP | Phase 2 | Phase 3 |
|---|-------|-----|---------|---------|
| 1 | **Employee Management** | ✅ | — | — |
| 2 | **Attendance Management** (GPS + Face + Anti-Mock) | ✅ | Shift swap, fingerprint integration | Biometric kiosk |
| 3 | **Leave Management** | ✅ | Carry-over policy builder | Leave forecasting AI |
| 4 | **Payroll Management** | ✅ | Multi-currency expat | Outsource payroll-as-a-service |
| 5 | **PPh 21 Calculation** | ✅ | e-Bupot direct submit | TER rate AI advisor |
| 6 | **BPJS Calculation** | ✅ | SIPP/EDABU direct API | Klaim JHT helper |
| 7 | **Reimbursement** (OCR + fraud) | ✅ | Corporate card sync | AI policy violation detect |
| 8 | **Employee Loan** | ✅ | Loan marketplace (3rd party) | — |
| 9 | **Recruitment ATS** | — | ✅ | AI interview scoring |
| 10 | **Performance Management** | — | ✅ | Continuous calibration AI |
| 11 | **Asset Management** | — | ✅ | RFID/QR scan |
| 12 | **Employee Self-Service** (mobile) | ✅ | Voice commands | — |
| 13 | **HR Dashboard** | ✅ | Custom report builder | Predictive turnover |
| 14 | **AI Assistant** | ✅ (basic) | Payroll Q&A, OCR | Multi-agent HR advisor |

**MVP rilis:** Q4 2026 (modul 1–8, 12, 13, 14-basic).
**Phase 2:** Q1–Q2 2027 (modul 9, 10, 11 + enhancement).
**Phase 3:** Q3–Q4 2027 (AI advance + Enterprise features).

---

## 4. Functional Requirements (per modul)

### 4.1 Employee Management
- Profil lengkap (NIK, NPWP, BPJS, keluarga, pendidikan, dokumen).
- Riwayat kontrak (PKWT/PKWTT/Magang/Outsource).
- Struktur organisasi drag-drop.
- Bulk import (Excel template).
- Field-level encryption (NIK, NPWP, rekening).
- **Audit:** semua perubahan tercatat (siapa, kapan, before/after).

### 4.2 Attendance Management ⭐ (Fraud-Hardened)
**Wajib:**
- **GPS Validation**: koordinat dikirim dengan akurasi `accuracy_meters` (reject jika > 100m).
- **Geofencing**: per work_location, radius configurable 50–500m. Reject di luar radius (atau flag jika WFH allowed).
- **Face Recognition**: enroll wajah saat onboarding (3 angle), match saat clock-in (threshold cosine similarity ≥ 0.85).
- **Liveness Detection**: deteksi blink + head movement (passive — TensorFlow Lite di mobile, server verify).
- **Anti Fake GPS**: deteksi mock location flag (Android `isFromMockProvider`), Magisk/root detection.
- **Anti Mock Location**: cross-check kecepatan teleportasi (>500km/h antara 2 absen impossible), IP geolocation cross-check.
- Output: `attendance_logs.fraud_score` (0-100), `is_anomaly`, `anomaly_reasons[]`.

**Modes:**
- Mobile (selfie + GPS) — primary.
- Web (kantor only, IP whitelist).
- WhatsApp Bot (kirim foto + lokasi).
- Fingerprint mesin (import via SDK/file).

### 4.3 Leave Management
- 12+ jenis cuti preset UU 13/2003 (tahunan, sakit, melahirkan, menikah, dll).
- Saldo otomatis prorata, carry-over policy.
- Approval workflow N-level.
- Kalender tim, conflict detection.
- Pengajuan via mobile / web / WA.

### 4.4 Payroll Management
**Wajib:**
- Komponen: gaji pokok, tunjangan tetap (jabatan, transport, makan), tunjangan tidak tetap (lembur, insentif).
- **Lembur** sesuai PP 35/2021: hari biasa (1.5x jam-1, 2x jam berikutnya), hari libur (2x s/d 7 jam, 3x jam-8, 4x jam ke-9 dst).
- **THR**: 1x gaji untuk masa kerja ≥ 12 bln, prorata 1–12 bln.
- **Bonus** & insentif.
- **BPJS** auto-deduct (per modul 4.6).
- **PPh 21** auto-calc (per modul 4.5).
- **Slip gaji PDF** branded tenant, password-protected (default DOB), email + push + WA.
- **Bukti Potong 1721-A1** annual.
- **Bank file** export: BCA, Mandiri, BRI, BNI, CIMB (format CSV/TXT/XML per bank).
- Workflow: Draft → Calculate → Review → Approve → Disburse.
- Re-open period dengan audit trail.

### 4.5 PPh 21 Calculation
- **TER (PMK 168/2023)**: bulanan kategori A/B/C berdasarkan PTKP.
- **Annual progressive** (Desember + resign).
- 4 metode: Gross, Gross-Up, Nett, Mixed.
- Non-NPWP handling (sebelum 2024).
- Biaya jabatan (5% max Rp 6jt/thn).
- Output: payslip line + bukti potong PDF + e-Bupot XML.
- **Rule-pack versioned** — DJP ubah tarif, deploy rule-pack baru (Postgres rows, bukan code redeploy).

### 4.6 BPJS Calculation
- **Kesehatan** (5%: 4% er + 1% ee, cap UMP/12jt).
- **JHT** (5.7%: 3.7% er + 2% ee, no cap).
- **JP** (3%: 2% er + 1% ee, cap Rp 10.042.300 per 2026).
- **JKK** (0.24%–1.74% er, per industry_risk).
- **JKM** (0.3% er).
- Output: payslip line + file SIPP (BPJS-TK) + EDABU (BPJS-Kes).

### 4.7 Reimbursement ⭐ (AI-Hardened)
- Kategori: transport, makan dinas, internet, medical, training, dll dengan limit bulanan/tahunan.
- **OCR Receipt**: upload foto → extract (vendor, tanggal, total, items). Engine: Azure Form Recognizer / Google Document AI / self-host Tesseract+LayoutLM.
- **Fraud Detection**:
  - Image manipulation detection (ELA — Error Level Analysis).
  - **Duplicate receipt detection**: perceptual hash (pHash) + OCR-text hash, cross-check semua reimbursement tenant 24 bulan terakhir.
  - Round-number heuristic, weekend transaction flag.
  - Score 0–100, auto-flag if > 70.
- Workflow approval N-level.
- Settle via payroll run berikutnya (atau direct transfer).

### 4.8 Employee Loan
- Jenis: kasbon, pinjaman jangka pendek (1–24 bulan), pinjaman pendidikan, dll.
- Bunga: 0% / flat / efektif (configurable per tenant).
- Cicilan auto-deduct di payroll setiap bulan.
- Workflow approval (atasan + finance).
- Dashboard: outstanding per karyawan, total exposure.
- Early settlement.
- **Eligibility check**: total cicilan ≤ 30% take-home pay (configurable).

### 4.9 Recruitment ATS *(Phase 2)*
- Career page custom (`careers.<tenant>.com`).
- Job posting → distribusi LinkedIn, Glints, JobStreet (API/manual).
- Pipeline kanban configurable.
- **AI Screening**: ranking CV vs JD (embed + cosine).
- Interview scheduling + Google Calendar sync.
- Offer letter generator (template).
- Onboarding handoff ke Employee module.

### 4.10 Performance Management *(Phase 2)*
- OKR cascading (company → dept → individual).
- Review cycles (quarterly/annual/probation).
- 360 feedback.
- 1-on-1 notes.
- Calibration meeting + rating distribution.

### 4.11 Asset Management *(Phase 2)*
- Master asset: laptop, kendaraan, ID card, seragam, telpon.
- Assignment ke karyawan, riwayat.
- Return + maintenance schedule.
- Depreciation tracking.
- QR/barcode scan via mobile.
- Loss/damage report → auto-create reimbursement.

### 4.12 Employee Self-Service (Mobile-First)
- Beranda: hari ini, pengumuman.
- Absen + GPS + selfie.
- Cuti, lembur, izin, reimbursement, pinjaman.
- Slip gaji + bukti potong (download).
- Direktori karyawan, struktur org.
- Inbox + push notif.
- Bahasa: ID/EN, offline mode.

### 4.13 HR Dashboard
- Headcount real-time (per dept, branch, status).
- Attendance rate harian/bulanan.
- Payroll cost MoM, YoY.
- Turnover & retention.
- Upcoming birthdays, anniversaries, contract expiry.
- Customizable widgets (Phase 2: report builder).

### 4.14 AI Assistant
- **HR Chatbot**: tanya regulasi ID, kebijakan internal, status cuti/reimbursement. Powered by LLM (Claude Haiku / Groq llama-3) + RAG ke knowledge base tenant.
- **Payroll Assistant**: "kenapa PPh saya bulan ini naik?" → AI baca payslip + jelaskan.
- **Fraud Detection**: agent yang scan attendance/reimbursement anomaly nightly.
- **OCR Document Reader**: KTP, NPWP, ijazah, kontrak → auto-extract ke form.

---

## 5. Non-Functional Requirements

### 5.1 Performance
- API p95 < 400ms (read), < 800ms (write).
- Payroll 10.000 kary: run < 5 menit.
- Mobile cold start < 2 detik.
- Dashboard load < 1.5 detik.

### 5.2 Availability
- Starter/Growth: 99.5% (≤ 3.6 jam/bln downtime).
- Pro: 99.9%.
- Enterprise: 99.95% (≤ 22 menit/bln) + SLA.

### 5.3 Security ⭐
- **MFA**: TOTP, WebAuthn, SMS (Indonesia: only as fallback). Wajib untuk HR/Finance/Admin role.
- **RBAC** + ABAC hybrid.
- **Audit Trail** immutable 5 thn (payroll 10 thn).
- **Encryption**: TLS 1.3 in-transit, AES-256-GCM at-rest, field-level (NIK/NPWP/rekening) dengan envelope encryption (KMS).
- **Device Binding**: register device, max N device per user, revoke remotely.
- **Session Management**: refresh + access token, inactivity timeout, force logout, concurrent session limit.
- **OWASP Top 10** compliance.
- **PSE Kominfo** terdaftar.
- **UU PDP 27/2022** — consent, DSAR flow, DPO.

### 5.4 Scalability
- Horizontal scaling: stateless services + Redis session.
- Postgres read-replica + partitioning (attendance, payslip_lines, audit_logs).
- Async payroll via Kafka/RabbitMQ + dedicated worker pool.
- CDN untuk static + payslip PDF.

### 5.5 Compliance ID
- PSE Privat Kominfo.
- Data residency: server di Indonesia (AWS Jakarta / GCP Jakarta / IDCloudHost).
- UU PDP 27/2022.
- UU ITE.
- UU Ketenagakerjaan 13/2003 + UU Cipta Kerja 6/2023.
- PMK 168/2023 (PPh 21 TER), Perpres 64/2020 (BPJS Kes), PP 84/2013 (JHT), PP 45/2015 (JP).

---

## 6. Success Metrics (Year 1)

| KPI | Target |
|-----|--------|
| Paying tenants | 500 |
| Employee seats aktif | 25.000 |
| MRR | Rp 750 juta |
| NPS | ≥ 45 |
| Monthly churn | < 3% |
| Time-to-first-payroll | < 7 hari |
| Payroll calc accuracy vs DJP | 100% |
| Mobile DAU/MAU | > 60% |
| Fraud detection precision | > 90% (low false-positive) |
| Uptime | meet SLA per tier |

---

## 7. Pricing

| Tier | Target | /kary/bln | Min seat | Notable |
|------|--------|-----------|----------|---------|
| Free | Try | Rp 0 | s/d 5 | No payroll |
| Starter | UMKM | Rp 15.000 | 5 | Payroll + Mobile + WA |
| Growth | SMB | Rp 35.000 | 25 | + ATS + Performance + AI (limited) |
| Pro | Mid-large | Rp 60.000 | 100 | + Custom workflow + Reports + Loan + Asset |
| Enterprise | 500+ | ~Rp 90.000+ | 500 | + SSO + Dedicated infra + SLA 99.95% + On-prem option |

Add-ons: WhatsApp Cloud API quota, AI credit pack, OCR pack, payroll outsourcing service.

---

## 8. Constraints & Assumptions

**Constraints:**
- Server **harus di Indonesia** (PP 71/2019 untuk PSE Privat).
- Mobile: Android 8+ (covers ~95% ID market), iOS 14+.
- Bahasa default Indonesia, EN secondary.

**Assumptions:**
- Team tersedia: 1 PM + 4 BE Java + 2 FE + 2 Mobile + 1 DevOps + 1 Designer + tax consultant (advisor).
- Budget infra MVP: ~Rp 15 jt/bln (EKS cluster + RDS + Redis + OpenSearch + R2/S3).
- Domain partner pajak tersedia (retainer Rp 10 jt/bln).

---

## 9. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Salah PPh 21 → denda | Reputation killer | 200+ golden tests, quarterly tax-consultant audit, liability insurance |
| Mock GPS bypass | Fraud, tenant churn | Multi-layer: device flag + speed check + IP geo + face match + behavioral pattern |
| Regulasi berubah (PMK baru) | Engine refactor | Rule-pack architecture; dedicated regulatory analyst |
| Data breach (NIK, gaji) | PDP lawsuit | Envelope encryption, KMS, bug bounty, SOC 2 Type II Y2 |
| Performance Enterprise (10K kary payroll) | Tenant churn | Benchmark sejak day-1, partitioning, async workers |
| Vendor lock-in WA Cloud API | Service down | Abstract `MessagingProvider`, fallback Twilio + Telegram |
| AI hallucination (HR chatbot) | Wrong policy info | RAG dengan citation, disclaimer, fallback to human |

---

## 10. Open Questions (perlu keputusan)

1. **Cloud primary**: AWS Jakarta vs GCP Jakarta? *Rekomendasi: AWS (ecosystem matang, EKS, RDS, KMS, Cognito-as-fallback).*
2. **Face Recognition vendor**: AWS Rekognition vs Azure Face vs self-host (FaceNet/ArcFace)? *Rekomendasi: self-host (cost + data residency).*
3. **OCR vendor**: AWS Textract vs Google Doc AI vs self-host? *Rekomendasi: hybrid — self-host PaddleOCR untuk struk Indonesia, fallback ke vendor.*
4. **Build vs buy MFA**: Auth0/Cognito vs self-build di Spring Security? *Rekomendasi: self-build (cost & customization), use libraries (Spring Security + WebAuthn4J).*
5. **WhatsApp BSP**: Meta direct vs 360dialog vs Wati? *Rekomendasi: Meta direct (cost) + 360dialog fallback.*
6. **Mobile push**: FCM only atau ditambah APNs direct? FCM cukup untuk iOS via abstraction.
7. **PT legal entity** untuk daftar PSE Kominfo — perlu didirikan dulu.
