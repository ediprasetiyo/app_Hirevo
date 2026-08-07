# User Stories — Hirevo HRIS

**Format:** `Sebagai <role>, saya ingin <capability>, sehingga <value>.`
**Acceptance Criteria:** Given/When/Then. INVEST principles.
**Story Points:** Fibonacci (1, 2, 3, 5, 8, 13).
**Priority:** P0 (MVP must), P1 (MVP should), P2 (Phase 2), P3 (Phase 3).

---

## Epic 1 — Tenant Onboarding & Auth

### US-001 [P0, 5pt] Sign up tenant baru
**Sebagai** calon pelanggan, saya ingin mendaftar akun Hirevo dalam < 3 menit, **sehingga** saya bisa langsung coba aplikasi.

**AC:**
- Given saya di landing page, When saya isi (nama company, email, password, jumlah karyawan), Then tenant tercipta dengan plan `free`, trial 14 hari, subdomain auto-generate.
- Tenant pertama otomatis di-assign role `super_admin`.
- Email verifikasi terkirim dalam 30 detik.
- Subdomain `{slug}.hirevo.id` aktif tanpa propagation delay.

### US-002 [P0, 8pt] Login dengan MFA
**Sebagai** HR Admin, saya ingin login dengan MFA (TOTP/WebAuthn), **sehingga** akun saya aman dari pencurian password.

**AC:**
- Given saya enrol TOTP/WebAuthn, When login dengan password valid, Then sistem minta kode MFA sebelum issue token.
- 5 gagal MFA → akun lock 15 menit.
- 10x recovery code di-generate saat enroll, hanya bisa dilihat sekali.
- WebAuthn support YubiKey, Touch ID, Windows Hello.

### US-003 [P0, 5pt] Device binding & manage
**Sebagai** user, saya ingin lihat & revoke device yang terhubung ke akun saya, **sehingga** saya bisa logout dari HP yang hilang.

**AC:**
- Setiap login pertama dari device baru, kirim notif ke email + push ke device lain.
- Settings → Trusted Devices list (nama, OS, last active, IP/kota).
- Revoke device → refresh token invalidated, force re-login.

### US-004 [P1, 3pt] Wizard onboarding tenant
**Sebagai** Super Admin baru, saya ingin dipandu setup awal, **sehingga** saya tidak bingung.

**AC:**
- Wizard 5 langkah: Company info → Branches → Roles & users → Salary components template → Upload karyawan.
- Progress bar persistent, bisa skip & lanjut nanti.
- Setup checklist dashboard hingga 100%.

### US-005 [P0, 5pt] Invite & manage users
**Sebagai** Super Admin, saya ingin invite user lain (HR, finance, manager), **sehingga** mereka bisa kerja sama di Hirevo.

**AC:**
- Invite via email, link expire 7 hari.
- Set role saat invite (multi-role allowed).
- Role custom (clone + edit permission) di paket Pro+.

---

## Epic 2 — Employee Management

### US-010 [P0, 8pt] Buat karyawan baru
**Sebagai** HR Admin, saya ingin tambah karyawan baru lengkap dengan data pribadi & kontrak.

**AC:**
- Form tab: Personal · Employment · Tax · BPJS · Bank · Documents.
- NIK 16 digit validated, NPWP 15/16 digit, auto-encrypted at-rest.
- Hire date set status `probation` jika ada probation_until.
- Wajib link ke 1 contract aktif sebelum save.
- Audit log capture (created_by, timestamp).

### US-011 [P0, 5pt] Bulk import karyawan
**Sebagai** HR Admin, saya ingin import 100+ karyawan via Excel.

**AC:**
- Download template (xlsx) dengan validation hint per kolom.
- Upload → preview (first 10 rows) → confirm → background job.
- Error report per-row (excel highlight merah).
- Progress notification (toast + email).

### US-012 [P0, 3pt] Edit data karyawan dengan audit trail
**AC:** Setiap field critical (salary, position, manager, NIK) — audit log mencatat siapa, kapan, before/after.

### US-013 [P0, 5pt] Struktur organisasi visual
**Sebagai** HR Admin, saya ingin lihat & edit org chart drag-drop.

**AC:** Org chart hierarchical, drag karyawan/dept ke parent baru, save → re-compute manager_id.

### US-014 [P1, 3pt] Upload dokumen karyawan
**AC:** Upload KTP, NPWP, ijazah, kontrak (max 10MB each, jenis: pdf/jpg/png). Signed URL expire 5 menit untuk download.

### US-015 [P0, 5pt] Resign / terminate flow
**Sebagai** HR Admin, saya ingin proses resign karyawan dengan exit checklist.

**AC:**
- Resign date + reason wajib.
- Auto-create checklist: return asset, BPJS deactivate, final pay calc, exit interview.
- Status → `resigned`, akses app revoked di tanggal resign.

---

## Epic 3 — Attendance Management ⭐

### US-020 [P0, 13pt] Clock-in mobile dengan GPS + selfie + face recognition + liveness
**Sebagai** karyawan, saya ingin absen dari HP dengan aman dari fraud.

**AC:**
- Given saya buka mobile app, When tap "Clock In", Then app minta camera + location permission.
- Liveness challenge: blink + head turn (TFLite on-device).
- Frame selfie + GPS coords + accuracy + timestamp dikirim ke server.
- Server verify: face embedding cosine similarity ≥ 0.85 vs enrolled, GPS within geofence, mock GPS flag = false.
- Jika lolos: `attendance_logs` insert, status `present`/`late`, push notif sukses.
- Jika gagal: tampilkan reason ramah ("Wajah kurang jelas, coba lagi"), tidak insert.
- `fraud_score` >=70 → status `pending_review` + alert HR.

### US-021 [P0, 8pt] Enroll wajah karyawan
**Sebagai** karyawan baru, saya ingin enroll wajah sekali, **sehingga** bisa absen face-match.

**AC:**
- 3 angle: depan, kiri 30°, kanan 30°.
- Quality check (resolution, lighting, no mask).
- Embedding di-store encrypted (per-tenant key); raw image dihapus setelah 30 hari.

### US-022 [P0, 5pt] Geofencing per work_location
**Sebagai** HR Admin, saya ingin set lokasi kantor + radius, **sehingga** karyawan harus absen dari area kantor.

**AC:** Pilih titik di map (Leaflet/Google Maps), radius slider 50–500m. Multiple lokasi support. Validasi saat clock-in: jika di luar radius semua lokasi, reject (kecuali assigned WFH today).

### US-023 [P0, 8pt] Anti mock-location detection
**AC:**
- Android: read `Location.isFromMockProvider()` → flag.
- iOS: check `CMMotionManager` consistency, jailbreak detection via library.
- Server: cross-check 2 clock-in terakhir untuk kecepatan teleport (>500 km/h = impossible).
- Cross-check IP geolocation country vs GPS.
- Aggregate ke `fraud_score`.

### US-024 [P0, 3pt] Clock-out + work duration
**AC:** Symmetric flow dengan clock-in. Hitung `worked_minutes` (clock_out - clock_in - break).

### US-025 [P0, 5pt] Lihat history attendance & koreksi
**Sebagai** karyawan, saya ingin lihat 30 hari history & ajukan koreksi.

**AC:** Calendar view per bulan. Tap hari → detail (in/out, foto, lokasi). Tombol "Ajukan koreksi" → form alasan + bukti → workflow approval.

### US-026 [P0, 5pt] Pengajuan lembur dengan kalkulasi otomatis PP 35
**AC:** Form (tanggal, jam mulai, jam selesai, alasan) → approval. Saat approved, kalkulasi sesuai PP 35/2021 (1.5x jam-1, 2x dst). Hasil masuk payslip otomatis.

### US-027 [P1, 5pt] Shift management & assignment
**AC:** HR set shift (pagi/siang/malam), assign per karyawan per minggu, bulk assign via Excel.

### US-028 [P0, 3pt] Clock-in via WhatsApp Bot
**AC:** Karyawan kirim "absen masuk" ke bot → bot minta foto + lokasi → log ke sistem.

---

## Epic 4 — Leave Management

### US-030 [P0, 5pt] Pengajuan cuti
**AC:** Form (jenis cuti, tgl mulai, tgl akhir, alasan, attachment opsional). Validasi: saldo cukup, no conflict cuti aktif, min_notice_days. Submit → workflow approval.

### US-031 [P0, 5pt] Approval cuti (manager) + delegasi
**AC:** Manager dapat notif push + email + WA. Approve/Reject dengan komentar. Bulk approve (multiple selection). Delegasi jika manager cuti.

### US-032 [P0, 3pt] Saldo cuti otomatis (prorata + carry-over)
**AC:** Karyawan baru di-prorata sisa tahun. Carry-over max 6 hari, expire 31 Mar tahun berikutnya (configurable).

### US-033 [P0, 3pt] Kalender tim — lihat siapa cuti
**AC:** Calendar view tim. Filter per department. Hide salary-sensitive info.

### US-034 [P0, 5pt] Master leave_types preset UU 13/2003
**AC:** Saat onboarding, auto-seed: tahunan 12, sakit dgn surdok, melahirkan 3 bln, menikah 3 hr, istri melahirkan 2 hr, bereavement, ibadah, haid (opt-in).

### US-035 [P1, 5pt] Leave conflict warning
**AC:** Saat ajukan cuti, jika > X% tim di-dept sudah cuti di tgl yang sama → warning "Tim Anda 50% cuti tgl ini, lanjut?".

---

## Epic 5 — Payroll Management

### US-040 [P0, 13pt] Jalankan payroll bulanan
**Sebagai** Finance, saya ingin run payroll bulanan dalam < 10 menit.

**AC:**
- Pilih period → pilih karyawan (all/filter) → klik Calculate.
- Status: draft → calculating → calculated → reviewed → approved → paid.
- Calculating async (Kafka job), notify progress per 100 karyawan.
- Calculated: tampilkan summary (total gross/net/PPh/BPJS), preview slip per karyawan.
- Re-calculate per karyawan tanpa redo all.
- Reviewed → Approved (different user, 4-eyes principle) → generate bank file + slip PDF + send notif.

### US-041 [P0, 8pt] Komponen gaji configurable
**AC:** Master `salary_components` per tenant (code, name, category, formula, taxable, bpjs base, prorate). Formula engine (JSONLogic): contoh `{"if":[{">":[{"var":"hari_hadir"},20]}, gaji_pokok, prorata]}`.

### US-042 [P0, 5pt] Salary structure per karyawan + history
**AC:** Setiap karyawan punya N komponen aktif. Tracking effective_from/to (tidak overwrite). Bulk salary adjustment (e.g. annual raise 10%).

### US-043 [P0, 13pt] PPh 21 TER otomatis
**AC:**
- Tarik PTKP & TER kategori dari `employee_tax_profiles`.
- Hitung gross taxable × TER% per bulan.
- Desember & resign: hitung annual progressive, koreksi ke ytd.
- Output: payslip line + `tax_calculations` record (snapshot input).
- Test suite: 200+ scenario (PTKP × bonus × THR × non-NPWP).

### US-044 [P0, 13pt] BPJS calc otomatis (5 program)
**AC:** Per program: pakai rate berlaku + cap. Calc employee + employer share. Output ke payslip line + `bpjs_calculations`.

### US-045 [P0, 8pt] THR otomatis (PP 36/2021)
**AC:**
- Eligible: masa kerja ≥ 1 bln.
- ≥ 12 bln: 1× upah bulanan.
- 1–12 bln: prorata.
- Run THR sebagai payroll_run type=`thr`, pay date max H-7 lebaran.

### US-046 [P0, 5pt] Lembur otomatis dari attendance
**AC:** Saat run payroll, ambil semua approved overtime di period, calc per PP 35, masuk payslip.

### US-047 [P0, 8pt] Slip gaji PDF + send
**AC:**
- PDF branded (logo tenant, warna).
- Password = DOB ddmmyyyy (default, configurable).
- Layout: header company, periode, karyawan info, table earnings, deductions, summary, footer.
- Send: email + push notif + (opt) WA.
- Karyawan download dari mobile / web.

### US-048 [P0, 8pt] Bukti Potong 1721-A1
**AC:** Generate annual (Jan tahun berikutnya). PDF format DJP. XML e-Bupot export. Bulk download zip.

### US-049 [P0, 5pt] Bank file export
**AC:** Pilih bank (BCA/Mandiri/BRI/BNI/CIMB) → generate file sesuai format → download. Include total record + total amount.

### US-050 [P0, 5pt] Re-open & adjustment payroll period
**AC:** Period closed bisa di-reopen oleh super_admin dgn reason. Adjustment di run baru tipe `adjustment`. Audit trail wajib.

### US-051 [P1, 5pt] Payroll preview & simulator
**AC:** "What-if": ubah salary X karyawan, preview impact total payroll & PPh tanpa commit.

---

## Epic 6 — Reimbursement ⭐

### US-060 [P0, 8pt] Pengajuan reimbursement dengan OCR
**Sebagai** karyawan, saya ingin foto struk → auto-extract → submit.

**AC:**
- Mobile: tap kategori → ambil foto struk → OCR extract (vendor, date, total, items) → user verify/edit → submit.
- OCR <3 detik (Google Doc AI / self-host PaddleOCR).
- Confidence < 0.7 per field → highlight kuning, user wajib verify.

### US-061 [P0, 13pt] Fraud detection reimbursement
**AC:**
- Image manipulation (ELA) check → suspicion score.
- Duplicate detection: pHash + OCR-text → cross-tenant search 24 bln.
- Heuristic: round number, weekend, outlier amount per vendor, exceed monthly category limit.
- Aggregate `fraud_score` 0-100. >70: auto-block + alert HR. 40-70: queue for review.

### US-062 [P0, 5pt] Approval reimbursement
**AC:** Workflow N-level, default: atasan langsung → finance. Bukti foto thumbnail di approval screen.

### US-063 [P0, 3pt] Settle via payroll
**AC:** Approved reimbursement queue. Saat run payroll, auto-include as komponen `reimbursement` (non-taxable per kategori).

### US-064 [P0, 3pt] Kategori reimbursement dengan limit
**AC:** HR set kategori + monthly/yearly limit + require_receipt + taxable flag. Validasi saat submit.

### US-065 [P1, 3pt] Cash advance (kasbon dinas)
**AC:** Request → approve → disburse (manual / payroll). Settle dengan reimbursement actual, return excess / pay shortfall.

---

## Epic 7 — Employee Loan

### US-070 [P0, 8pt] Pengajuan pinjaman karyawan
**AC:**
- Form: jenis pinjaman, jumlah, tenor (bulan), tujuan.
- Eligibility check: cicilan total ≤ 30% take-home (auto-compute), masa kerja ≥ 1 thn (configurable).
- Preview schedule cicilan (flat/efektif).
- Workflow approval: atasan → HR → Finance.

### US-071 [P0, 5pt] Auto-deduct cicilan di payroll
**AC:** Saat run payroll, auto-insert deduction line per active loan. Track remaining balance.

### US-072 [P0, 3pt] Dashboard exposure pinjaman
**AC:** Finance lihat total outstanding per karyawan, per dept, aging. Export Excel.

### US-073 [P1, 3pt] Early settlement & restructure
**AC:** Karyawan request early settlement (full / partial), Finance approve, adjust schedule.

---

## Epic 8 — Recruitment ATS (Phase 2)

### US-080 [P2, 8pt] Buat job posting + career page
### US-081 [P2, 5pt] Submit aplikasi via career page
### US-082 [P2, 13pt] AI screening CV (match score vs JD)
### US-083 [P2, 8pt] Pipeline kanban + drag stage
### US-084 [P2, 8pt] Interview scheduling + Google Calendar
### US-085 [P2, 5pt] Generate offer letter dari template
### US-086 [P2, 5pt] Hire → onboarding handoff ke Employee module

---

## Epic 9 — Performance Management (Phase 2)

### US-090 [P2, 8pt] Set OKR cascading
### US-091 [P2, 5pt] Check-in KR weekly
### US-092 [P2, 13pt] Review cycle (self + manager + 360)
### US-093 [P2, 5pt] Calibration meeting tool
### US-094 [P2, 3pt] 1-on-1 notes + action items

---

## Epic 10 — Asset Management (Phase 2)

### US-100 [P2, 5pt] Master asset + QR code generator
### US-101 [P2, 5pt] Assign asset ke karyawan + acknowledge
### US-102 [P2, 3pt] Return asset + condition check
### US-103 [P2, 5pt] Maintenance schedule + reminder
### US-104 [P2, 3pt] Depreciation tracking

---

## Epic 11 — Employee Self-Service (Mobile)

### US-110 [P0, 5pt] Beranda mobile (hari ini + pengumuman)
### US-111 [P0, 3pt] Slip gaji + bukti potong download
### US-112 [P0, 3pt] Direktori karyawan + struktur org
### US-113 [P0, 5pt] Push notif (FCM)
### US-114 [P0, 3pt] Offline mode (cache absen, sync online)
### US-115 [P1, 3pt] Bahasa ID/EN switcher

---

## Epic 12 — HR Dashboard

### US-120 [P0, 5pt] Dashboard headcount + attendance + payroll widget
**AC:** 6 widget default: headcount by dept, attendance rate, payroll cost MoM, leave usage, upcoming birthdays, recent hires/resigns.

### US-121 [P0, 3pt] Filter dashboard by branch & period
### US-122 [P1, 13pt] Custom report builder (drag-drop)
### US-123 [P0, 5pt] Export dashboard ke PDF/Excel

---

## Epic 13 — AI Assistant

### US-130 [P0, 13pt] HR Chatbot (RAG ke knowledge base tenant)
**AC:** Chat UI mobile + web. Karyawan tanya cuti, slip gaji, kebijakan. Bot jawab + cite source (FAQ doc, policy). Fallback "saya tidak tahu, kontak HR" jika confidence rendah.

### US-131 [P1, 8pt] Payroll Assistant (explain my slip)
**AC:** "Kenapa PPh saya naik?" → AI baca payslip + bandingkan bulan lalu + jelaskan.

### US-132 [P0, 8pt] OCR Document Reader (KTP, NPWP, ijazah)
**AC:** Saat onboarding karyawan baru, HR foto KTP → auto-fill form (NIK, nama, DOB, alamat).

### US-133 [P1, 13pt] Fraud Detection Agent (nightly scan)
**AC:** Cron 02:00 daily, scan attendance + reimbursement, flag anomaly, kirim digest ke HR Admin.

---

## Epic 14 — Workflow & Approval

### US-140 [P0, 8pt] Configure approval workflow per modul
**AC:** Workflow builder visual. Steps: approver type (manager/role/specific user/level). Auto-escalate after N jam.

### US-141 [P0, 3pt] Approval inbox terpusat
**AC:** Inbox menampilkan semua pending: leave, overtime, reimbursement, loan, cash advance. Bulk action.

### US-142 [P0, 3pt] Delegate approval (manager cuti)
**AC:** Manager set delegate sebelum cuti. Auto-route ke delegate.

---

## Epic 15 — Audit & Compliance

### US-150 [P0, 5pt] View audit log (filter by entity/user/date)
### US-151 [P1, 3pt] Export audit log untuk audit eksternal
### US-152 [P0, 5pt] PDP — Data Subject Access Request flow
**AC:** Karyawan request semua data → generate zip semua data karyawan + email.

### US-153 [P0, 3pt] Right to be forgotten (resigned > 2 thn)
**AC:** Background job hapus dokumen + anonymize PII setelah retention period, tetap simpan record minimal (payroll history) sesuai UU pajak.

---

## Story Mapping — MVP Total

| Epic | Stories MVP | Total Points |
|------|-------------|--------------|
| 1 Tenant/Auth | 5 | 26 |
| 2 Employee | 6 | 29 |
| 3 Attendance ⭐ | 9 | 55 |
| 4 Leave | 5 | 21 |
| 5 Payroll | 11 | 96 |
| 6 Reimbursement | 5 | 32 |
| 7 Loan | 3 | 16 |
| 11 Self-Service | 5 | 19 |
| 12 Dashboard | 3 | 13 |
| 13 AI (basic) | 2 | 21 |
| 14 Workflow | 3 | 14 |
| 15 Audit/Compliance | 4 | 16 |
| **TOTAL MVP** | **61** | **358 pts** |

Dengan velocity 30 pts/sprint × 12 sprints = 360 pts. **Pas.**

Phase 2 (Recruitment + Performance + Asset + AI advance) = ~250 pts → 8 sprints.
Phase 3 (Enterprise features, custom report builder, multi-region DR) = ~150 pts → 5 sprints.
