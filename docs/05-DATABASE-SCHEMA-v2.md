# Database Schema v2 — Hirevo HRIS

**Database:** PostgreSQL 16
**Strategy:** Multi-tenant hybrid (shared schema + RLS for SMB, dedicated schema for Enterprise)
**Migration tool:** Liquibase (XML changeset per release)

This document **extends** [ERD.md](../ERD.md) (v1) with:
- ✅ Existing modules from ERD v1 (Tenancy, Org, Employee, Attendance, Leave, Payroll, Tax, BPJS, Reimbursement, Recruitment, Performance, Workflow, System) — **lihat ERD.md untuk DDL detail**.
- 🆕 **New modules** for v2: Employee Loan, Asset Management, MFA & Device Binding, Face Recognition, OCR Receipt, AI Chat, Webhook subscriptions, Outbox pattern.
- 🆕 **Enhancements** to existing tables: fraud_score, hash-chain audit, encrypted fields, partition strategy.

---

## 1. Schema Per Bounded Context (Java/Spring services)

Setiap microservice punya **logical schema** terpisah dalam database physical yang sama (untuk SMB) atau database terpisah (Enterprise):

```sql
CREATE SCHEMA iam;
CREATE SCHEMA tenant;
CREATE SCHEMA employee;
CREATE SCHEMA attendance;
CREATE SCHEMA leave_mgmt;
CREATE SCHEMA payroll;
CREATE SCHEMA tax;
CREATE SCHEMA bpjs;
CREATE SCHEMA reimburse;
CREATE SCHEMA loan;
CREATE SCHEMA recruitment;
CREATE SCHEMA performance;
CREATE SCHEMA asset;
CREATE SCHEMA workflow;
CREATE SCHEMA notif;
CREATE SCHEMA document;
CREATE SCHEMA ai;
CREATE SCHEMA audit;
CREATE SCHEMA integration;
CREATE SCHEMA reporting;
```

Cross-schema FK **dilarang** (loose coupling). Cross-service reference pakai UUID + event consistency.

---

## 2. MFA & Device Binding (iam schema) 🆕

```sql
-- ============ MFA DEVICES ============
CREATE TABLE iam.user_mfa_methods (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  user_id         UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
  method_type     VARCHAR(20) NOT NULL
                  CHECK (method_type IN ('totp','webauthn','sms','email')),
  display_name    VARCHAR(100),                       -- 'YubiKey 5C', 'Authy on Phone'
  -- TOTP
  totp_secret_encrypted BYTEA,                         -- AES-GCM, KMS-wrapped
  -- WebAuthn
  webauthn_credential_id BYTEA UNIQUE,
  webauthn_public_key    BYTEA,
  webauthn_sign_count    BIGINT DEFAULT 0,
  webauthn_aaguid        UUID,
  webauthn_transports    TEXT[],
  -- SMS
  phone_number    VARCHAR(20),
  -- meta
  is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
  is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
  last_used_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at      TIMESTAMPTZ
);
CREATE INDEX idx_mfa_user ON iam.user_mfa_methods(user_id) WHERE revoked_at IS NULL;

CREATE TABLE iam.mfa_recovery_codes (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
  code_hash       VARCHAR(255) NOT NULL,               -- Argon2id hash
  used_at         TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE iam.mfa_challenges (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
  method_id       UUID REFERENCES iam.user_mfa_methods(id),
  challenge       BYTEA NOT NULL,                       -- random nonce (WebAuthn)
  purpose         VARCHAR(30) NOT NULL                  -- 'login','enroll','step_up'
                  CHECK (purpose IN ('login','enroll','step_up','password_reset')),
  attempts        INT NOT NULL DEFAULT 0,
  expires_at      TIMESTAMPTZ NOT NULL,
  consumed_at     TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============ TRUSTED DEVICES ============
CREATE TABLE iam.trusted_devices (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  user_id             UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
  device_fingerprint  VARCHAR(255) NOT NULL,
  device_name         VARCHAR(100),
  platform            VARCHAR(20)
                      CHECK (platform IN ('android','ios','web','desktop')),
  os_version          VARCHAR(50),
  app_version         VARCHAR(50),
  push_token          VARCHAR(500),                    -- FCM
  push_token_provider VARCHAR(20),                    -- 'fcm','apns'
  last_ip             INET,
  last_geo_country    VARCHAR(2),
  last_geo_city       VARCHAR(100),
  trust_level         VARCHAR(20) NOT NULL DEFAULT 'new'
                      CHECK (trust_level IN ('trusted','new','suspicious','compromised')),
  is_root_jailbroken  BOOLEAN,                          -- detected from app
  enrolled_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_active_at      TIMESTAMPTZ,
  revoked_at          TIMESTAMPTZ,
  revoked_reason      VARCHAR(50),
  UNIQUE (user_id, device_fingerprint)
);
CREATE INDEX idx_trusted_user_active ON iam.trusted_devices(user_id) WHERE revoked_at IS NULL;

-- ============ REFRESH TOKENS (rotating) ============
CREATE TABLE iam.refresh_tokens (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  user_id             UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
  device_id           UUID REFERENCES iam.trusted_devices(id) ON DELETE CASCADE,
  token_hash          VARCHAR(255) NOT NULL UNIQUE,    -- SHA-256
  parent_id           UUID REFERENCES iam.refresh_tokens(id),  -- rotation chain
  expires_at          TIMESTAMPTZ NOT NULL,
  revoked_at          TIMESTAMPTZ,
  revoked_reason      VARCHAR(50),                      -- 'rotated','logout','admin','suspicious'
  ip_address          INET,
  user_agent          TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_user_active ON iam.refresh_tokens(user_id) WHERE revoked_at IS NULL;

-- ============ FAILED LOGIN TRACKING ============
CREATE TABLE iam.login_attempts (
  id              BIGSERIAL,
  tenant_id       UUID,
  email_attempted VARCHAR(255),
  user_id         UUID,
  ip_address      INET NOT NULL,
  user_agent      TEXT,
  success         BOOLEAN NOT NULL,
  failure_reason  VARCHAR(50),
  mfa_required    BOOLEAN NOT NULL DEFAULT FALSE,
  mfa_success     BOOLEAN,
  device_fingerprint VARCHAR(255),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_login_email_time ON iam.login_attempts(email_attempted, created_at DESC);
CREATE INDEX idx_login_ip_time ON iam.login_attempts(ip_address, created_at DESC);
```

---

## 3. Face Recognition (attendance schema) 🆕

```sql
CREATE TABLE attendance.face_enrollments (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  employee_id         UUID NOT NULL,                    -- ref employee.employees
  enrollment_status   VARCHAR(20) NOT NULL DEFAULT 'pending'
                      CHECK (enrollment_status IN ('pending','active','revoked')),
  embedding_model     VARCHAR(50) NOT NULL,             -- 'arcface_resnet50_v2'
  embedding_version   INT NOT NULL,                     -- bump on model upgrade → re-enroll
  embedding_vector    BYTEA NOT NULL,                   -- encrypted (KMS), 512-dim float32
  embedding_norm      NUMERIC(8,6),
  quality_score       NUMERIC(5,4),
  reference_image_s3_key TEXT,                          -- raw image (purged after 30 days)
  reference_purge_at  TIMESTAMPTZ,
  enrolled_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  enrolled_by         UUID,
  revoked_at          TIMESTAMPTZ,
  UNIQUE (employee_id, embedding_version)
);
CREATE INDEX idx_face_employee ON attendance.face_enrollments(employee_id) WHERE enrollment_status='active';

CREATE TABLE attendance.face_match_logs (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  attendance_log_id   UUID,                              -- nullable, set if matched
  employee_id         UUID NOT NULL,
  candidate_embedding BYTEA,                             -- encrypted, kept 24h then purged
  cosine_similarity   NUMERIC(6,5),
  liveness_score      NUMERIC(5,4),
  is_match            BOOLEAN NOT NULL,
  reject_reason       VARCHAR(50),                       -- 'low_similarity','liveness_fail','quality'
  device_id           UUID,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_facelog_employee_time ON attendance.face_match_logs(employee_id, created_at DESC);
```

---

## 4. Attendance Enhancements 🆕

```sql
-- Extend attendance_logs (DDL ada di ERD.md, ini delta column)
ALTER TABLE attendance.attendance_logs
  ADD COLUMN gps_accuracy_meters NUMERIC(6,2),
  ADD COLUMN is_mock_location BOOLEAN DEFAULT FALSE,
  ADD COLUMN device_id UUID REFERENCES iam.trusted_devices(id),
  ADD COLUMN fraud_score INT DEFAULT 0,                   -- 0-100
  ADD COLUMN fraud_signals JSONB,                          -- {mock_gps: true, speed_anomaly: 600, ...}
  ADD COLUMN face_match_log_id UUID REFERENCES attendance.face_match_logs(id),
  ADD COLUMN liveness_verified BOOLEAN DEFAULT FALSE,
  ADD COLUMN ip_address INET,
  ADD COLUMN ip_geo_country VARCHAR(2);
```

---

## 5. Reimbursement Enhancements 🆕

```sql
-- OCR jobs (reimburse schema)
CREATE TABLE reimburse.ocr_jobs (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  reimbursement_item_id UUID,                           -- linked after creation
  source_image_s3_key TEXT NOT NULL,
  provider            VARCHAR(20) NOT NULL,             -- 'paddleocr','google_doc_ai','azure'
  status              VARCHAR(20) NOT NULL DEFAULT 'queued'
                      CHECK (status IN ('queued','processing','done','failed')),
  extracted_data      JSONB,                             -- {vendor, date, total, items, tax, raw_text}
  confidence_scores   JSONB,                             -- per-field confidence
  processing_time_ms  INT,
  error               TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  completed_at        TIMESTAMPTZ
);

-- Receipt fingerprints untuk dup detection (perceptual hash + OCR hash)
CREATE TABLE reimburse.receipt_fingerprints (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  reimbursement_item_id UUID NOT NULL,                   -- ref reimburse.reimbursement_items
  phash               BIT(64) NOT NULL,                   -- perceptual hash 64-bit
  ocr_text_hash       VARCHAR(64) NOT NULL,               -- SHA-256 of normalized OCR text
  vendor              VARCHAR(255),
  receipt_date        DATE,
  amount              NUMERIC(18,2),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_phash ON reimburse.receipt_fingerprints USING gist (phash bit_minmax_ops);  -- for Hamming distance search
CREATE INDEX idx_text_hash ON reimburse.receipt_fingerprints(tenant_id, ocr_text_hash);

-- Fraud signals
CREATE TABLE reimburse.fraud_signals (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  reimbursement_request_id UUID NOT NULL,
  signal_type         VARCHAR(50) NOT NULL,              -- 'ela_manipulation','duplicate_phash','duplicate_text','outlier_amount','weekend_txn','round_number'
  severity            VARCHAR(20) NOT NULL CHECK (severity IN ('low','medium','high','critical')),
  details             JSONB,                              -- {duplicate_request_id, hamming_distance, ...}
  detected_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Extend reimbursement_requests
ALTER TABLE reimburse.reimbursement_requests
  ADD COLUMN fraud_score INT DEFAULT 0,
  ADD COLUMN ocr_confidence NUMERIC(5,4),
  ADD COLUMN review_status VARCHAR(20)                    -- 'auto_pass','manual_review','blocked'
             CHECK (review_status IN ('auto_pass','manual_review','blocked'));
```

---

## 6. Employee Loan (loan schema) 🆕

```sql
CREATE TABLE loan.loan_types (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  code            VARCHAR(50) NOT NULL,
  name            VARCHAR(255) NOT NULL,                  -- 'Kasbon','Pinjaman Pendidikan','Multiguna'
  max_amount      NUMERIC(18,2),
  max_tenor_months INT,
  interest_rate_percent NUMERIC(6,4) DEFAULT 0,
  interest_method VARCHAR(20) DEFAULT 'flat'              -- 'flat','effective','none'
                  CHECK (interest_method IN ('flat','effective','none')),
  min_service_months INT DEFAULT 0,                       -- min masa kerja
  max_dti_percent NUMERIC(5,2) DEFAULT 30,                -- debt-to-income cap
  requires_collateral BOOLEAN DEFAULT FALSE,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (tenant_id, code)
);

CREATE TABLE loan.loans (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  employee_id         UUID NOT NULL,
  loan_type_id        UUID NOT NULL REFERENCES loan.loan_types(id),
  loan_no             VARCHAR(50) UNIQUE,
  principal_amount    NUMERIC(18,2) NOT NULL,
  interest_rate_percent NUMERIC(6,4) NOT NULL,
  interest_method     VARCHAR(20) NOT NULL,
  tenor_months        INT NOT NULL,
  monthly_installment NUMERIC(18,2) NOT NULL,
  total_payable       NUMERIC(18,2) NOT NULL,
  purpose             TEXT,
  status              VARCHAR(20) NOT NULL DEFAULT 'pending'
                      CHECK (status IN ('pending','approved','rejected','disbursed','active','settled','defaulted','cancelled')),
  approval_instance_id UUID,
  disbursed_at        TIMESTAMPTZ,
  disbursement_method VARCHAR(20),                        -- 'bank_transfer','payroll'
  first_installment_date DATE,
  outstanding_amount  NUMERIC(18,2) NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_loans_employee_status ON loan.loans(employee_id, status);

CREATE TABLE loan.loan_installments (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  loan_id             UUID NOT NULL REFERENCES loan.loans(id) ON DELETE CASCADE,
  installment_no      INT NOT NULL,
  due_date            DATE NOT NULL,
  principal_amount    NUMERIC(18,2) NOT NULL,
  interest_amount     NUMERIC(18,2) NOT NULL,
  total_amount        NUMERIC(18,2) NOT NULL,
  status              VARCHAR(20) NOT NULL DEFAULT 'scheduled'
                      CHECK (status IN ('scheduled','paid','partial','overdue','waived')),
  paid_amount         NUMERIC(18,2) DEFAULT 0,
  paid_at             TIMESTAMPTZ,
  payroll_run_id      UUID,                                -- ref payroll.payroll_runs (loose)
  payslip_id          UUID,
  UNIQUE (loan_id, installment_no)
);

CREATE TABLE loan.loan_repayments (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  loan_id             UUID NOT NULL REFERENCES loan.loans(id) ON DELETE CASCADE,
  installment_id      UUID REFERENCES loan.loan_installments(id),
  amount              NUMERIC(18,2) NOT NULL,
  repayment_type      VARCHAR(20) NOT NULL                 -- 'payroll_deduction','direct','early_settlement','waived'
                      CHECK (repayment_type IN ('payroll_deduction','direct','early_settlement','waived')),
  notes               TEXT,
  recorded_by         UUID,
  recorded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 7. Asset Management (asset schema) 🆕

```sql
CREATE TABLE asset.asset_categories (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  code            VARCHAR(50) NOT NULL,
  name            VARCHAR(255) NOT NULL,                  -- 'Laptop','Kendaraan','ID Card','Telpon','Seragam'
  depreciation_method VARCHAR(20)                          -- 'straight_line','double_declining','none'
                      CHECK (depreciation_method IN ('straight_line','double_declining','none')),
  useful_life_months  INT,
  parent_id           UUID REFERENCES asset.asset_categories(id),
  UNIQUE (tenant_id, code)
);

CREATE TABLE asset.assets (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  asset_code          VARCHAR(50) NOT NULL,                -- internal code, scannable QR
  qr_code_url         TEXT,
  category_id         UUID REFERENCES asset.asset_categories(id),
  name                VARCHAR(255) NOT NULL,
  brand               VARCHAR(100),
  model               VARCHAR(100),
  serial_number       VARCHAR(100),
  purchase_date       DATE,
  purchase_price      NUMERIC(18,2),
  current_value       NUMERIC(18,2),
  vendor              VARCHAR(255),
  warranty_until      DATE,
  condition           VARCHAR(20) DEFAULT 'good'
                      CHECK (condition IN ('new','good','fair','damaged','lost','disposed')),
  status              VARCHAR(20) DEFAULT 'available'
                      CHECK (status IN ('available','assigned','in_maintenance','retired','lost')),
  branch_id           UUID,
  photo_url           TEXT,
  notes               TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (tenant_id, asset_code)
);
CREATE INDEX idx_assets_status ON asset.assets(tenant_id, status);

CREATE TABLE asset.asset_assignments (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  asset_id            UUID NOT NULL REFERENCES asset.assets(id) ON DELETE CASCADE,
  employee_id         UUID NOT NULL,
  assigned_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  assigned_by         UUID,
  acknowledged_at     TIMESTAMPTZ,
  acknowledged_signature_url TEXT,                          -- e-signature image
  expected_return_date DATE,
  condition_at_assignment VARCHAR(20),
  notes               TEXT,
  -- return
  returned_at         TIMESTAMPTZ,
  returned_by         UUID,
  condition_at_return VARCHAR(20),
  damage_notes        TEXT,
  damage_photo_url    TEXT
);

CREATE TABLE asset.asset_maintenance_logs (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  asset_id            UUID NOT NULL REFERENCES asset.assets(id) ON DELETE CASCADE,
  maintenance_type    VARCHAR(50),                          -- 'service','repair','calibration'
  scheduled_date      DATE,
  performed_date      DATE,
  vendor              VARCHAR(255),
  cost                NUMERIC(18,2),
  description         TEXT,
  attachment_url      TEXT,
  next_maintenance_date DATE,
  status              VARCHAR(20)
                      CHECK (status IN ('scheduled','in_progress','completed','cancelled'))
);

CREATE TABLE asset.asset_depreciation_schedule (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id           UUID NOT NULL,
  asset_id            UUID NOT NULL REFERENCES asset.assets(id) ON DELETE CASCADE,
  period_year         INT NOT NULL,
  period_month        INT NOT NULL,
  depreciation_amount NUMERIC(18,2) NOT NULL,
  book_value_after    NUMERIC(18,2) NOT NULL,
  UNIQUE (asset_id, period_year, period_month)
);
```

---

## 8. AI Chat & Knowledge Base (ai schema) 🆕

```sql
CREATE TABLE ai.chat_sessions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  user_id         UUID NOT NULL,
  title           VARCHAR(255),
  context_type    VARCHAR(30) NOT NULL                    -- 'hr_chatbot','payroll_assistant','recruitment_assistant'
                  CHECK (context_type IN ('hr_chatbot','payroll_assistant','recruitment_assistant','general')),
  model           VARCHAR(50),                             -- 'claude-haiku-4-5','llama-3-70b-groq'
  total_tokens    INT DEFAULT 0,
  total_cost_usd  NUMERIC(10,6) DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_message_at TIMESTAMPTZ
);

CREATE TABLE ai.chat_messages (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  session_id      UUID NOT NULL REFERENCES ai.chat_sessions(id) ON DELETE CASCADE,
  role            VARCHAR(20) NOT NULL CHECK (role IN ('system','user','assistant','tool')),
  content         TEXT NOT NULL,
  tool_calls      JSONB,
  citations       JSONB,                                    -- [{doc_id, snippet, score}]
  input_tokens    INT,
  output_tokens   INT,
  latency_ms      INT,
  user_feedback   VARCHAR(20),                              -- 'helpful','not_helpful','wrong'
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_chat_msg_session ON ai.chat_messages(session_id, created_at);

-- Knowledge base for RAG (FAQ, policies, employee handbook)
CREATE TABLE ai.knowledge_documents (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  source_type     VARCHAR(30),                              -- 'manual','policy_pdf','faq','regulation'
  title           VARCHAR(500) NOT NULL,
  content         TEXT NOT NULL,
  source_url      TEXT,
  language        VARCHAR(5) DEFAULT 'id',
  version         INT DEFAULT 1,
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ai.knowledge_chunks (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  document_id     UUID NOT NULL REFERENCES ai.knowledge_documents(id) ON DELETE CASCADE,
  chunk_index     INT NOT NULL,
  content         TEXT NOT NULL,
  embedding       vector(1024),                              -- pgvector — bge-m3 1024-dim
  token_count     INT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_chunks_tenant ON ai.knowledge_chunks(tenant_id);
CREATE INDEX idx_chunks_embedding ON ai.knowledge_chunks USING hnsw (embedding vector_cosine_ops);

-- AI fraud / anomaly scores (cross-domain)
CREATE TABLE ai.anomaly_scores (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  entity_type     VARCHAR(50) NOT NULL,                     -- 'attendance','reimbursement','loan','candidate'
  entity_id       UUID NOT NULL,
  model_version   VARCHAR(20),
  score           NUMERIC(5,2) NOT NULL,                    -- 0-100
  signals         JSONB NOT NULL,
  decision        VARCHAR(20)                                -- 'auto_pass','review','block'
                  CHECK (decision IN ('auto_pass','review','block')),
  reviewed_at     TIMESTAMPTZ,
  reviewed_by     UUID,
  review_outcome  VARCHAR(20),                               -- 'confirmed_fraud','false_positive','inconclusive'
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_anomaly_entity ON ai.anomaly_scores(entity_type, entity_id);
```

---

## 9. Outbox & Event Streaming (per-service) 🆕

```sql
-- Outbox pattern (one per service schema)
CREATE TABLE payroll.outbox_events (
  id              BIGSERIAL PRIMARY KEY,
  tenant_id       UUID NOT NULL,
  aggregate_type  VARCHAR(50) NOT NULL,                     -- 'payroll_run','payslip'
  aggregate_id    UUID NOT NULL,
  event_type      VARCHAR(100) NOT NULL,                    -- 'PayrollApproved','PayslipGenerated'
  payload         JSONB NOT NULL,
  occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published       BOOLEAN NOT NULL DEFAULT FALSE,
  published_at    TIMESTAMPTZ,
  attempts        INT DEFAULT 0
);
CREATE INDEX idx_outbox_unpublished ON payroll.outbox_events(occurred_at) WHERE NOT published;

-- Debezium / poller picks unpublished rows → Kafka topic
```

---

## 10. Webhook Subscriptions (integration schema) 🆕

```sql
CREATE TABLE integration.webhook_subscriptions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  name            VARCHAR(255) NOT NULL,
  target_url      TEXT NOT NULL,
  secret          VARCHAR(255) NOT NULL,                    -- HMAC-SHA256 signing
  event_types     TEXT[] NOT NULL,                          -- ['employee.created','payroll.approved']
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_success_at TIMESTAMPTZ,
  last_failure_at TIMESTAMPTZ,
  failure_count   INT DEFAULT 0
);

CREATE TABLE integration.webhook_deliveries (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  subscription_id UUID NOT NULL REFERENCES integration.webhook_subscriptions(id) ON DELETE CASCADE,
  event_type      VARCHAR(100),
  payload         JSONB,
  http_status     INT,
  response_body   TEXT,
  attempt_no      INT DEFAULT 1,
  next_retry_at   TIMESTAMPTZ,
  delivered_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 11. Audit Enhancement — Hash Chain (audit schema) 🆕

```sql
-- (existing audit_logs from ERD.md) + delta:
ALTER TABLE audit.audit_logs
  ADD COLUMN trace_id UUID,
  ADD COLUMN impersonator_user_id UUID,
  ADD COLUMN service VARCHAR(50),
  ADD COLUMN prev_hash BYTEA,
  ADD COLUMN record_hash BYTEA;

-- Tamper detection job state
CREATE TABLE audit.chain_verification_runs (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ended_at        TIMESTAMPTZ,
  records_verified BIGINT,
  broken_at_id    BIGINT,                                    -- audit_log id where chain broke
  status          VARCHAR(20) CHECK (status IN ('running','ok','broken','error'))
);
```

---

## 12. Reporting Materialized Views (reporting schema) 🆕

```sql
-- Headcount snapshot daily (refresh nightly)
CREATE MATERIALIZED VIEW reporting.mv_headcount_daily AS
SELECT
  e.tenant_id,
  d.name AS department,
  b.name AS branch,
  COUNT(*) FILTER (WHERE e.status='active') AS active_count,
  COUNT(*) FILTER (WHERE e.status='probation') AS probation_count,
  CURRENT_DATE AS snapshot_date
FROM employee.employees e
LEFT JOIN employee.departments d ON d.id = (
  SELECT department_id FROM employee.employment_contracts
  WHERE employee_id = e.id AND status='active' LIMIT 1
)
LEFT JOIN employee.branches b ON b.id = d.branch_id
WHERE e.deleted_at IS NULL
GROUP BY e.tenant_id, d.name, b.name;

CREATE UNIQUE INDEX ON reporting.mv_headcount_daily(tenant_id, department, branch, snapshot_date);

-- Payroll cost monthly
CREATE MATERIALIZED VIEW reporting.mv_payroll_cost_monthly AS
SELECT
  pr.tenant_id,
  pp.period_year,
  pp.period_month,
  pr.company_id,
  SUM(pr.total_gross) AS total_gross,
  SUM(pr.total_pph21) AS total_pph,
  SUM(pr.total_bpjs_employee + pr.total_bpjs_employer) AS total_bpjs,
  SUM(pr.total_net) AS total_net,
  COUNT(*) AS run_count
FROM payroll.payroll_runs pr
JOIN payroll.payroll_periods pp ON pp.id = pr.payroll_period_id
WHERE pr.status IN ('approved','paid')
GROUP BY pr.tenant_id, pp.period_year, pp.period_month, pr.company_id;

-- Refresh schedule via pg_cron / scheduled job
```

---

## 13. PostgreSQL Extensions Required

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;        -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pg_trgm;          -- fuzzy search (employee, candidate)
CREATE EXTENSION IF NOT EXISTS vector;           -- pgvector for AI embeddings
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pg_partman;       -- partition automation
CREATE EXTENSION IF NOT EXISTS pg_cron;          -- in-DB scheduled jobs
CREATE EXTENSION IF NOT EXISTS postgis;          -- (optional) for geofence advanced
```

---

## 14. Partitioning Strategy

| Table | Partition By | Retention | Tool |
|-------|--------------|-----------|------|
| `attendance.attendance_logs` | RANGE month | 3 years | pg_partman |
| `attendance.face_match_logs` | RANGE month | 90 days | pg_partman + drop |
| `payroll.payslip_lines` | RANGE year | 10 years | manual |
| `audit.audit_logs` | RANGE month | 5–10 years | pg_partman + archive to S3 |
| `iam.login_attempts` | RANGE month | 1 year | pg_partman + drop |
| `notif.notifications` | RANGE month | 6 months | pg_partman + drop |
| `reimburse.ocr_jobs` | RANGE month | 1 year | pg_partman + drop |

---

## 15. RLS Templates (apply per service)

```sql
-- Liquibase changeset
DO $$
DECLARE
  schema_name TEXT;
  tbl RECORD;
BEGIN
  FOR schema_name IN VALUES ('iam'),('tenant'),('employee'),('attendance'),
                            ('leave_mgmt'),('payroll'),('tax'),('bpjs'),
                            ('reimburse'),('loan'),('recruitment'),('performance'),
                            ('asset'),('workflow'),('notif'),('document'),
                            ('ai'),('audit'),('integration'),('reporting')
  LOOP
    FOR tbl IN
      SELECT table_name
      FROM information_schema.columns
      WHERE table_schema = schema_name AND column_name = 'tenant_id'
    LOOP
      EXECUTE format('ALTER TABLE %I.%I ENABLE ROW LEVEL SECURITY;', schema_name, tbl.table_name);
      EXECUTE format(
        'CREATE POLICY tenant_isolation ON %I.%I USING (tenant_id = current_setting(''app.current_tenant_id'', true)::uuid);',
        schema_name, tbl.table_name
      );
    END LOOP;
  END LOOP;
END $$;
```

---

## 16. Indexing Cheatsheet (additional to ERD.md)

| Query pattern | Index |
|---------------|-------|
| Employee search by name (fuzzy) | `CREATE INDEX ON employee.employees USING gin (full_name gin_trgm_ops);` |
| Attendance: get this month for employee | `(tenant_id, employee_id, work_date)` — already in ERD |
| Payslip download by employee | `(employee_id, created_at DESC)` |
| Audit search by entity | `(tenant_id, entity_type, entity_id, created_at DESC)` |
| Loan active per employee | `(employee_id, status) WHERE status='active'` |
| Face match recent | `(employee_id, created_at DESC)` |
| Receipt phash search (Hamming dist ≤ 5) | gist + bit_minmax_ops |

---

## 17. Sample Liquibase Changeset Structure

```
liquibase/
├── master.xml
├── iam/
│   ├── 001-users-roles.xml
│   ├── 002-sessions.xml
│   ├── 003-mfa.xml
│   ├── 004-trusted-devices.xml
│   └── 005-refresh-tokens.xml
├── employee/
│   ├── 001-companies-branches.xml
│   ├── 002-employees.xml
│   └── 003-contracts.xml
├── attendance/...
├── payroll/...
└── seed/
    ├── 001-ptkp-statuses.xml
    ├── 002-tax-brackets-ter.xml
    ├── 003-tax-brackets-annual.xml
    ├── 004-bpjs-programs-rates.xml
    ├── 005-banks.xml
    ├── 006-holidays-2026-2027.xml
    └── 007-default-leave-types.xml
```

Reference master.xml chains all module masters; per-tenant migrations run on Enterprise provisioning.
