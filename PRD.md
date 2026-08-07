# PRD — Hirevo HRIS

**Status:** Draft v1.0
**Tanggal:** 2026-06-16
**Owner Produk:** Edi Prasetiyo
**Target Rilis MVP:** Q4 2026

---

## 1. Ringkasan Eksekutif

**Hirevo** adalah Human Resource Information System (HRIS) berbasis cloud (SaaS multi-tenant) yang dirancang khusus untuk pasar Indonesia, melayani segmen mulai dari UMKM (5–50 karyawan), perusahaan menengah (50–500), hingga enterprise (>500). Hirevo menggabungkan modul HR inti (employee data, attendance, payroll, leave) dengan modul talent (recruitment, performance, learning) dalam satu platform yang **patuh regulasi Indonesia** (PPh 21 TER, BPJS Kesehatan & Ketenagakerjaan, UU Cipta Kerja, PP 35/2021).

### 1.1 Visi
Menjadi platform HRIS #1 untuk perusahaan Indonesia yang ingin **mengelola karyawan tanpa pusing regulasi**, dengan harga terjangkau untuk UMKM dan skalabilitas untuk enterprise.

### 1.2 Misi
1. Mengotomasi 90% pekerjaan administratif HR (absensi, payroll, slip gaji, BPJS, PPh 21).
2. Memberikan compliance Indonesia *out-of-the-box* — bukan add-on.
3. Membuat HRIS *affordable* untuk UMKM (mulai Rp 15.000/karyawan/bulan).
4. Menyediakan mobile-first experience untuk karyawan lapangan.

---

## 2. Latar Belakang & Masalah

### 2.1 Pain Points Pasar Indonesia
| Segmen | Pain Point Utama |
|--------|------------------|
| **UMKM (5–50 kary.)** | Masih pakai Excel/WA grup; payroll manual rawan salah; tidak paham PPh 21 TER 2024 |
| **Menengah (50–500)** | HRIS lokal (Talenta, Mekari) mahal; fitur recruitment terbatas; integrasi finance ribet |
| **Enterprise (>500)** | Vendor global (SAP SF, Workday) overkill & mahal; kurang fleksibel untuk komponen gaji khas ID (uang makan, transport, lembur PP 35) |

### 2.2 Kompetisi
- **Talenta by Mekari** — market leader, harga premium, fokus mid-market.
- **Gadjian / Hadirr** — kuat di payroll & absensi, recruitment lemah.
- **LinovHR** — enterprise, instalasi on-premise.
- **Sleekr (now Mekari)** — sudah merged.
- **BambooHR, Deel, Rippling** — global, tidak handle PPh 21 / BPJS dengan baik.

### 2.3 Diferensiasi Hirevo
1. **Pricing transparan & murah untuk UMKM** (freemium di bawah 10 karyawan).
2. **Compliance engine** Indonesia yang di-update otomatis (TER, UMR per kota, tarif BPJS).
3. **WhatsApp-native** — absensi & approval bisa via WA Bot (penting untuk blue-collar).
4. **AI Assistant** — generate JD, screening CV, draft kontrak, ringkasan performance review.
5. **Modular** — UMKM bisa pakai modul Payroll saja; Enterprise pakai full suite.

---

## 3. Target Pengguna & Persona

### 3.1 Segmen
- **Tier 1 — UMKM** (5–50 karyawan): toko retail, F&B, klinik, agency kecil.
- **Tier 2 — SMB** (50–500): manufaktur skala menengah, startup growth-stage, jaringan retail.
- **Tier 3 — Enterprise** (500+): grup perusahaan, manufaktur besar, BUMN, BPR/multifinance.

### 3.2 Persona
1. **Bu Sari — Owner UMKM** (toko 12 karyawan). Tidak paham PPh 21, ingin gajian beres tanggal 25 tanpa pusing. Pakai HP, jarang buka laptop.
2. **Pak Andi — HR Manager SMB** (perusahaan 200 karyawan). Butuh laporan untuk direksi, kelola rekrutmen aktif, ingin integrasi ke Accurate/Jurnal.
3. **Ibu Linda — HR Director Enterprise** (3000 karyawan, 8 cabang). Butuh approval matrix kompleks, audit trail, SSO, custom report builder.
4. **Rudi — Karyawan**. Cek slip gaji, ajukan cuti, absen pakai HP. Tidak mau install banyak app.
5. **Pak Budi — Direktur**. Lihat dashboard headcount, payroll cost, turnover dari HP.

---

## 4. Tujuan & Metrik Sukses

### 4.1 Tujuan Bisnis (Year 1)
- 500 perusahaan berbayar (target 70% UMKM, 25% SMB, 5% Enterprise).
- 25.000 employee seats aktif.
- MRR Rp 750 juta.
- NPS ≥ 45.
- Churn bulanan < 3%.

### 4.2 Metrik Produk
| Metrik | Target |
|--------|--------|
| Time-to-first-payroll (onboarding) | < 7 hari |
| Akurasi perhitungan PPh 21 vs DJP | 100% |
| Uptime | 99.9% |
| Mobile DAU/MAU karyawan | > 60% |
| Response time API p95 | < 400ms |
| Adopsi modul kedua (cross-sell) | > 40% akun |

---

## 5. Scope MVP vs Roadmap

### 5.1 MVP (Rilis Q4 2026) — "Hirevo Core"
1. **Multi-tenant Auth & Onboarding** (subdomain `acme.hirevo.id`)
2. **Employee Database** (data pribadi, kontrak, dokumen, struktur organisasi)
3. **Attendance** (mobile selfie + GPS, WFH/WFO, shift sederhana)
4. **Leave Management** (cuti tahunan, sakit, izin, approval workflow)
5. **Payroll Indonesia** (PPh 21 TER, BPJS Kes/JHT/JP/JKK/JKM, THR, lembur PP 35)
6. **Slip Gaji digital** (PDF + email + mobile)
7. **Mobile App karyawan** (Flutter — absen, slip gaji, cuti)
8. **Backoffice Web HR** (Vue/Next.js)
9. **WhatsApp Bot** (absen & approval cuti)

### 5.2 Fase 2 (Q1–Q2 2027)
- Recruitment (ATS, career page, AI screening CV)
- Performance Management (OKR, 360 review)
- Reimbursement & Cash Advance
- Integrasi Accurate, Jurnal, Xero
- Custom approval workflow builder

### 5.3 Fase 3 (Q3–Q4 2027)
- Learning Management System (LMS)
- Succession Planning
- Compensation & Benefit benchmarking
- Engagement Survey + Pulse
- SSO (SAML/OIDC) untuk Enterprise
- On-premise / Private Cloud deployment option

### 5.4 Out of Scope (sementara)
- Akuntansi/general ledger penuh (akan diintegrasikan, bukan dibuat ulang).
- Manajemen aset perusahaan.
- Project management / timesheet billable (kecuali ada permintaan kuat).

---

## 6. Detail Fungsional per Modul

### 6.1 Multi-Tenant & Onboarding
- Self-signup → buat workspace → trial 14 hari (semua fitur).
- Subdomain otomatis + custom domain (paket Enterprise).
- Wizard onboarding: upload data karyawan (template Excel), set komponen gaji, set jam kerja, undang HR admin.
- Setup checklist progress bar (gamified — penting untuk aktivasi UMKM).

### 6.2 Employee Database
- Profil lengkap: NIK, NPWP, BPJS, rekening bank, status PTKP (TK/0 s/d K/3), keluarga.
- Riwayat kontrak (PKWT/PKWTT/Outsource/Magang/Harian Lepas).
- Dokumen (KTP, ijazah, kontrak) — storage S3-compatible, max 5GB/akun starter.
- Struktur organisasi (drag-drop org chart).
- Custom field per tenant.
- Audit log perubahan data (siapa, kapan, apa).

### 6.3 Attendance
- **Web clock-in** + **Mobile clock-in** (selfie + GPS + foto).
- **Geofencing** per lokasi kerja (radius 50–500m).
- **Anti-spoofing**: deteksi mock GPS, liveness detection (selfie blinking) — paket Pro+.
- **Shift management**: shift pagi/siang/malam, rotasi mingguan, shift fleksibel.
- **Lembur**: pengajuan + approval, perhitungan otomatis sesuai PP 35/2021 (1.5x jam pertama, 2x jam berikutnya, dst).
- **WhatsApp clock-in**: kirim lokasi & foto ke WA Bot.
- **Mesin fingerprint** (Fase 2): integrasi via API/file import (Solution X100, Fingerspot, dll).

### 6.4 Leave Management
- Master jenis cuti: tahunan (12 hari), sakit, melahirkan (3 bulan), menikah (3 hari), dll — preset UU Ketenagakerjaan.
- Saldo cuti otomatis (prorata untuk karyawan baru, carry-over policy).
- Approval workflow: 1–N level (atasan langsung → HR → Direktur).
- Kalender tim (lihat siapa cuti minggu ini).
- Pengajuan via mobile / WA Bot.

### 6.5 Payroll Indonesia (modul paling kompleks)
**Komponen gaji:**
- Gaji pokok, tunjangan tetap (jabatan, transport, makan), tunjangan tidak tetap.
- Lembur otomatis (terhubung modul attendance).
- Potongan: BPJS Kes (1% kary + 4% perusahaan), JHT (2%+3.7%), JP (1%+2%), JKK & JKM (perusahaan), PPh 21, kasbon, koperasi, denda.
- THR & Bonus Tahunan (pro-rata sesuai PP 36/2021).

**PPh 21:**
- Skema TER (Tarif Efektif Rata-rata) bulanan + perhitungan tahunan Desember.
- Support gross, gross-up, dan nett.
- Status PTKP otomatis dari data karyawan.
- Generate **bukti potong 1721-A1** (PDF) dan **e-SPT/e-Bupot** compatible export.

**BPJS:**
- Upah ditanggung sesuai batas atas (UMR provinsi untuk Kes, Rp 9jt untuk JP per 2026).
- File ekspor SIPP/EDABU (CSV format BPJS).

**Run payroll:**
- Draft → Review → Approve → Disburse.
- Bank file output: BCA, Mandiri, BRI, BNI, CIMB (format payroll CSV/TXT per bank).
- Re-open period (dengan audit trail).

**Slip gaji:**
- PDF brand tenant (logo, warna).
- Kirim email + push mobile + (opsional) WhatsApp.
- Password-protected PDF (default: tanggal lahir).

### 6.6 Mobile App Karyawan (Flutter)
- Login dengan email atau NIK + PIN/biometric.
- Beranda: hari ini hadir/cuti/lembur, pengumuman.
- Absen (selfie + GPS).
- Ajukan cuti, lembur, izin.
- Slip gaji (3 bulan terakhir gratis, history full di paket Pro).
- Direktori karyawan.
- Pengumuman & inbox.
- Push notification (FCM).
- Mode offline: cache absen, sync ketika online.

### 6.7 Backoffice Web (HR & Admin)
- Dashboard: headcount, attendance rate, payroll cost MoM, upcoming birthdays.
- Modul-modul (Employee, Attendance, Leave, Payroll, Settings).
- Role-based access (Super Admin, HR Admin, Manager, Finance, Auditor read-only).
- Custom report builder (Fase 2).
- Bulk action (import, export, edit massal).

### 6.8 WhatsApp Bot
- Powered oleh WhatsApp Cloud API (Meta Business).
- Karyawan kirim "absen" → bot tanya foto + lokasi → log ke sistem.
- Approval cuti via tombol Approve/Reject di WA.
- Reminder: "Hari ini gajian, slip sudah dikirim."
- Per tenant pakai phone number sendiri (BYOPN) atau shared (paket Starter).

### 6.9 AI Assistant (Hirevo AI)
*Powered oleh LLM (Claude / Groq llama 3, tergantung cost).*
- Generate Job Description dari role + level.
- Screening CV: ranking kandidat vs JD.
- Draft kontrak kerja PKWT/PKWTT dari template + variabel.
- Ringkasan performance review.
- Tanya jawab regulasi Indonesia ("berapa pesangon PHK efisiensi untuk masa kerja 5 tahun?").

### 6.10 Recruitment / ATS (Fase 2)
- Career page custom (subdomain `careers.acme.com`).
- Job posting → distribusi ke LinkedIn/Glints/JobStreet (via API/manual).
- Pipeline kanban (Sourced → Screening → Interview → Offer → Hired).
- Email & WA template ke kandidat.
- Interview scheduling (sync Google Calendar).
- Onboarding handoff ke Employee Database.

### 6.11 Performance Management (Fase 2)
- Set OKR per quarter, cascading dari company → dept → individu.
- 1-on-1 meeting notes.
- 360 feedback (peer, atasan, bawahan, self).
- Calibration meeting & review cycle.

---

## 7. Arsitektur Teknis (Usulan)

### 7.1 Stack
- **Backend**: NestJS (Node 20) atau Laravel 11. *Rekomendasi: NestJS + Prisma untuk konsistensi monorepo, mengingat user sudah pakai Turborepo di proyek WA Admin.*
- **Database**: PostgreSQL 16 (multi-tenant: schema-per-tenant untuk Enterprise, shared schema + tenant_id untuk SMB/UMKM — strategi hybrid).
- **Cache & Queue**: Redis + BullMQ.
- **Object Storage**: Cloudflare R2 / Wasabi (lebih murah dari S3 untuk pasar ID).
- **Frontend Web (HR)**: Next.js 15 + Tailwind + shadcn/ui.
- **Mobile**: Flutter (re-use stack dari aplikasi IPL).
- **Monorepo**: Turborepo (apps: api, web, mobile, wa-bot; packages: ui, db, types, payroll-engine, tax-engine).
- **Infra**: Hetzner / IDCloudHost untuk app server, Cloudflare CDN, managed PostgreSQL (Neon/Supabase atau self-hosted dengan Patroni).
- **AI**: Groq llama-3 (cheap, fast) untuk task ringan; Claude Haiku untuk dokumen panjang.

### 7.2 Multi-Tenancy Strategy
- **Tenant resolution**: subdomain → `tenant_id` di middleware.
- **UMKM/SMB (≤1000 karyawan)**: shared database, `tenant_id` di setiap tabel, RLS PostgreSQL.
- **Enterprise**: dedicated schema atau dedicated DB (cluster terpisah), provisioning via Terraform.
- **Row-Level Security** Postgres enforced sebagai *defense-in-depth*.

### 7.3 Payroll Engine (Critical Path)
Engine perhitungan gaji adalah *core domain*. Harus:
- Punya **test suite** unit + golden-file dengan minimal 200 skenario (TER untuk semua status PTKP, lembur shift, prorata masuk/keluar, THR pro-rata, BPJS dengan upah > batas atas, dst).
- **Deterministic & idempotent**: re-run payroll periode sama menghasilkan output identik.
- Versioned **rule pack**: kalau DJP/BPJS ubah tarif, deploy rule pack baru tanpa redeploy aplikasi.
- Audit log: setiap perhitungan menyimpan formula & input yang dipakai (untuk dispute kary).

### 7.4 Compliance Update Mechanism
- Tim Hirevo monitor PMK, Permenaker, regulasi BPJS.
- Update tarif via dashboard internal → push ke semua tenant.
- Karyawan/HR dapat *changelog notification* ("Tarif PPh 21 TER bulan Januari telah di-update sesuai PMK 168/2023").

### 7.5 Security & Compliance
- **Data residency**: server di Indonesia (mandatori PP 71/2019 untuk PSE Privat).
- **Daftar PSE Kominfo** sebelum go-live publik.
- **Enkripsi**: TLS 1.3, at-rest AES-256, field-level encryption untuk NIK/NPWP/rekening.
- **Backup**: harian incremental, mingguan full, retensi 90 hari; PITR 7 hari.
- **Audit log**: immutable (append-only), simpan 5 tahun (untuk audit pajak).
- **Penetration test**: tahunan oleh third-party.
- **Rate limiting**, **2FA wajib untuk HR Admin**, **session management**.
- **UU PDP** (Pelindungan Data Pribadi 2022): consent management, data subject access request (DSAR) flow, DPO.

### 7.6 Skalabilitas
- Target: 1 tenant Enterprise dengan 10.000 karyawan bisa run payroll < 5 menit.
- Payroll dijalankan sebagai **batch job** di worker terpisah (BullMQ + dedicated worker pool).
- Read-replica Postgres untuk reporting.

---

## 8. UX Principles
1. **Mobile-first** untuk karyawan, **desktop-first** untuk HR.
2. **Bahasa Indonesia default**, English secondary.
3. **Empty state edukatif** — UMKM banyak yang baru pertama kali pakai HRIS.
4. **Wizard, bukan form** untuk task kompleks (setup payroll pertama, run payroll bulanan).
5. **No-blame errors**: "Kami tidak menemukan NPWP karyawan X. Mau kami treat sebagai TK/0 + 20% lebih tinggi?" — bukan error code.
6. **Aksesibilitas**: WCAG 2.1 AA untuk web; font size adjustable di mobile.

---

## 9. Pricing & Packaging (Usulan)

| Paket | Target | Harga/karyawan/bulan | Min. Seat | Fitur |
|-------|--------|----------------------|-----------|-------|
| **Free** | Coba-coba | Rp 0 | s/d 5 kary | Employee DB + Attendance + Leave (no payroll) |
| **Starter** | UMKM | Rp 15.000 | 5 | Core + Payroll PPh 21 + BPJS + Mobile |
| **Growth** | SMB | Rp 35.000 | 25 | Starter + Recruitment + Performance + WA Bot + AI (limit) |
| **Pro** | Mid-large | Rp 60.000 | 100 | Growth + Custom Workflow + Advanced Report + Integrasi |
| **Enterprise** | 500+ | Custom (~Rp 90rb+) | 500 | Semua + SSO + SLA 99.95% + Dedicated Support + On-Prem opsional |

Add-ons: WhatsApp Cloud API quota, AI credit pack, payroll outsourcing service.

---

## 10. Go-To-Market

### 10.1 Acquisition Channel
- **Content marketing** Indo-SEO ("cara hitung PPh 21 TER 2026", "rumus lembur PP 35").
- **Kalkulator gratis** (PPh 21, lembur, pesangon) sebagai lead magnet.
- **Partnership** dengan konsultan pajak, kantor akuntan UMKM, asosiasi (HIPMI, APINDO, Kadin daerah).
- **Reseller program** untuk konsultan HR.
- **Webinar bulanan** topik regulasi.
- **Direct sales** untuk Enterprise.

### 10.2 Onboarding
- Self-serve untuk Starter & Growth.
- Onboarding manager untuk Pro & Enterprise (migrasi data, training).
- Migrasi gratis dari Talenta/Gadjian (import tool).

---

## 11. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Salah hitung PPh 21 → tenant kena denda DJP | Reputasi hancur | Test suite 200+ skenario; insurance liability; audit kuartalan oleh konsultan pajak |
| Regulasi berubah mendadak (UU Cipta Kerja, omnibus baru) | Engine perlu refactor | Rule-pack architecture; dedicated regulatory analyst |
| Vendor WhatsApp Cloud API ubah harga/policy | WA Bot mati | Fallback ke Twilio + Telegram Bot; abstraksi `MessagingProvider` |
| Kompetitor (Mekari) drop harga | Tekanan margin | Diferensiasi via AI & UMKM affordability, bukan price war murni |
| Data breach (sensitif: NIK, gaji) | Tuntutan PDP + reputasi | SOC 2 Type II tahun ke-2; bug bounty; encryption-at-rest field-level |
| Payroll lambat untuk Enterprise besar | Tenant churn | Benchmark sejak awal; worker horizontal scaling; partitioning DB |
| Sulit dapat akuisisi UMKM (HR-illiterate) | Growth pelan | WhatsApp-first; bahasa simpel; channel partner konsultan pajak |

---

## 12. Tim & Timeline (Indikasi MVP)

### 12.1 Tim Minimal MVP (6 orang)
- 1 Product Manager (Edi)
- 2 Backend engineer (NestJS + Postgres)
- 1 Frontend engineer (Next.js)
- 1 Mobile engineer (Flutter)
- 1 Designer (sharing dgn aplikasi lain)
- + Konsultan pajak (part-time advisor)

### 12.2 Timeline MVP (6 bulan, Juli – Desember 2026)
- **M1 (Jul)**: Setup monorepo, auth multi-tenant, employee DB, design system.
- **M2 (Aug)**: Attendance (web + mobile), leave management.
- **M3 (Sep)**: Payroll engine v1 + PPh 21 TER + BPJS (fokus akurasi).
- **M4 (Oct)**: Slip gaji, bank file, mobile app polish.
- **M5 (Nov)**: WA Bot, AI assistant, onboarding wizard, billing.
- **M6 (Dec)**: Beta tertutup (10 tenant), bug fixing, daftar PSE, soft launch.

---

## 13. Asumsi & Open Questions

### Asumsi
- Tim sudah familiar dengan Turborepo + NestJS + Flutter (terbukti dari proyek lain).
- Budget infra awal ~Rp 5 juta/bulan (Hetzner + Cloudflare + Groq + WA API).
- Konsultan pajak tersedia advisory (~Rp 10jt/bulan retainer).

### Open Questions (perlu keputusan)
1. **Backend stack**: NestJS atau Laravel? (NestJS = konsistensi monorepo; Laravel = ekosistem Indonesia lebih luas, banyak dev tersedia.)
2. **Multi-tenancy DB strategy**: konfirmasi hybrid (shared + dedicated) — atau full shared dengan partitioning saja?
3. **Branding & legal entity**: PT Hirevo sudah ada atau akan didirikan? Penting untuk daftar PSE Kominfo.
4. **First paying customer / design partner**: ada calon dari network?
5. **WhatsApp BSP** mana yang dipakai (Meta direct, 360dialog, Wati, Wablas)?
6. **Pricing currency**: lock Rupiah atau IDR-equivalent USD untuk hedging?

---

## 14. Lampiran (akan ditambahkan terpisah)
- A. Sample bukti potong 1721-A1
- B. Spesifikasi file bank (BCA, Mandiri, BRI, BNI)
- C. Mock-up UI Dashboard HR & Mobile karyawan
- D. ERD database (employee, payroll_run, attendance, leave)
- E. API contract (OpenAPI 3.1)
- F. Daftar regulasi yang harus dipatuhi (PMK, Permenaker, dll)

---

*Dokumen ini adalah draft awal. Direview tiap akhir sprint dan diversioning di Git.*
