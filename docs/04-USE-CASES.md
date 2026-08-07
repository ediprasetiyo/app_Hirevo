# Use Cases & Activity Diagrams — Hirevo HRIS

Mermaid diagrams. Each module: use case diagram (actors → use cases) + activity diagrams untuk flow utama.

---

## 1. Aktor Sistem

```mermaid
flowchart TB
  SA[Super Admin Tenant]
  HRA[HR Admin]
  HRM[HR Manager / Recruiter]
  FIN[Finance]
  MGR[Line Manager]
  EMP[Employee]
  CAN[Candidate]
  DIR[Director / Executive]
  AUD[Auditor]
  ITA[IT Admin Tenant]
  HRV[Hirevo Super Admin]
  SYS[3rd-party System]
  BOT[AI Bot / Scheduled Job]
```

---

## 2. Use Case Diagram per Modul

### 2.1 Authentication & Tenant
```mermaid
flowchart LR
  SA((Super Admin)) --> UC1[Sign up tenant]
  SA --> UC2[Invite user]
  SA --> UC3[Set RBAC]
  ITA((IT Admin)) --> UC4[Configure SSO]
  ITA --> UC5[Enforce MFA policy]
  HRA((HR Admin)) --> UC6[Login + MFA]
  EMP((Employee)) --> UC6
  EMP --> UC7[Reset password]
  EMP --> UC8[Manage trusted devices]
  HRV((Hirevo Admin)) --> UC9[Impersonate tenant — audited]
```

### 2.2 Employee Management
```mermaid
flowchart LR
  HRA((HR Admin)) --> UC10[Create employee]
  HRA --> UC11[Bulk import]
  HRA --> UC12[Edit employee]
  HRA --> UC13[Manage contract]
  HRA --> UC14[Upload document]
  HRA --> UC15[Process resign / terminate]
  HRA --> UC16[Manage org structure]
  MGR((Manager)) --> UC17[View team members]
  EMP((Employee)) --> UC18[View own profile]
  EMP --> UC19[Update phone / address]
```

### 2.3 Attendance ⭐
```mermaid
flowchart LR
  EMP((Employee)) --> UC20[Enroll face]
  EMP --> UC21[Clock in mobile]
  EMP --> UC22[Clock out]
  EMP --> UC23[Submit overtime]
  EMP --> UC24[View attendance history]
  EMP --> UC25[Request attendance correction]
  HRA((HR Admin)) --> UC26[Configure work_location & geofence]
  HRA --> UC27[Manage shift assignment]
  HRA --> UC28[Review fraud-flagged attendance]
  MGR --> UC29[Approve overtime / correction]
  BOT((Fraud Bot)) --> UC30[Nightly anomaly scan]
  SYS((WA Bot)) --> UC31[Clock in via WhatsApp]
```

### 2.4 Leave
```mermaid
flowchart LR
  EMP --> UC40[Submit leave request]
  EMP --> UC41[View balance]
  EMP --> UC42[Cancel pending leave]
  MGR --> UC43[Approve / reject leave]
  HRA --> UC44[Configure leave types]
  HRA --> UC45[Manage holidays]
  HRA --> UC46[Year-end carry-over]
  EMP --> UC47[View team calendar]
```

### 2.5 Payroll
```mermaid
flowchart LR
  HRA --> UC50[Create payroll period]
  FIN((Finance)) --> UC51[Run payroll]
  FIN --> UC52[Review payslips]
  FIN --> UC53[Approve payroll]
  FIN --> UC54[Generate bank file]
  FIN --> UC55[Send slip to employees]
  HRA --> UC56[Manage salary components]
  HRA --> UC57[Bulk salary adjustment]
  EMP --> UC58[Download slip gaji]
  EMP --> UC59[Download bukti potong]
  FIN --> UC60[Run THR]
  FIN --> UC61[Re-open period]
  AUD((Auditor)) --> UC62[Audit payroll history]
```

### 2.6 Reimbursement
```mermaid
flowchart LR
  EMP --> UC70[Submit reimbursement + photo]
  EMP --> UC71[Submit cash advance]
  EMP --> UC72[Settle cash advance]
  MGR --> UC73[Approve reimbursement]
  FIN --> UC74[Final approve & pay]
  HRA --> UC75[Configure category & limit]
  BOT --> UC76[Auto fraud check OCR]
```

### 2.7 Loan
```mermaid
flowchart LR
  EMP --> UC80[Request loan]
  EMP --> UC81[View loan dashboard]
  MGR --> UC82[Approve loan L1]
  FIN --> UC83[Approve & disburse]
  FIN --> UC84[Manage repayment]
  EMP --> UC85[Early settlement]
```

### 2.8 Recruitment (Phase 2)
```mermaid
flowchart LR
  HRM((Recruiter)) --> UC90[Create job posting]
  HRM --> UC91[Distribute to job boards]
  CAN((Candidate)) --> UC92[Apply via career page]
  BOT --> UC93[AI screen CV]
  HRM --> UC94[Move stage / reject]
  HRM --> UC95[Schedule interview]
  MGR --> UC96[Submit interview feedback]
  HRM --> UC97[Generate offer]
  CAN --> UC98[Accept / decline offer]
  HRA --> UC99[Onboard hired]
```

### 2.9 Performance (Phase 2)
```mermaid
flowchart LR
  HRA --> UC110[Create review cycle]
  EMP --> UC111[Set OKR]
  EMP --> UC112[Check-in KR weekly]
  EMP --> UC113[Self review]
  MGR --> UC114[Manager review]
  EMP --> UC115[Peer 360 feedback]
  HRA --> UC116[Calibration meeting]
  MGR --> UC117[1-on-1 notes]
```

### 2.10 Asset (Phase 2)
```mermaid
flowchart LR
  HRA --> UC120[Create asset]
  HRA --> UC121[Assign to employee]
  EMP --> UC122[Acknowledge receipt]
  EMP --> UC123[Report damage / loss]
  HRA --> UC124[Return asset]
  HRA --> UC125[Schedule maintenance]
  BOT --> UC126[Send maintenance reminder]
```

### 2.11 Dashboard & Reports
```mermaid
flowchart LR
  HRA --> UC130[View HR dashboard]
  DIR --> UC131[View executive dashboard]
  FIN --> UC132[View payroll report]
  HRA --> UC133[Build custom report]
  HRA --> UC134[Export PDF / Excel]
```

### 2.12 AI Assistant
```mermaid
flowchart LR
  EMP --> UC140[Chat with HR bot]
  EMP --> UC141[Ask payroll question]
  HRA --> UC142[OCR upload KTP / NPWP]
  BOT --> UC143[Auto fraud scoring]
  HRA --> UC144[AI generate JD]
```

---

## 3. Activity Diagrams — Flow Utama

### 3.1 Login + MFA (US-002)

```mermaid
flowchart TB
  Start([User opens app]) --> Input[Enter email + password]
  Input --> POST[POST /v1/auth/login]
  POST --> Verify{Credentials valid?}
  Verify -->|No| LogFail[Log audit: login_failed] --> Inc[Increment failed_logins]
  Inc --> Lock{Failed >= 5?}
  Lock -->|Yes| Lockout[Lock account 15min] --> ErrLock([Show: Account locked]) --> End1([End])
  Lock -->|No| Err([Show: Invalid credentials]) --> End2([End])

  Verify -->|Yes| MFAEnabled{MFA enabled?}
  MFAEnabled -->|No| Issue[Issue access + refresh token]
  MFAEnabled -->|Yes| NewDev{New device?}
  NewDev -->|Yes| NotifEmail[Send notif to other devices + email]
  NewDev -->|No| Challenge
  NotifEmail --> Challenge[Generate MFA challenge_id, store Redis 5min]
  Challenge --> Resp202[Return 202 + challenge_id]
  Resp202 --> InputMFA[User enters TOTP or WebAuthn]
  InputMFA --> Verify2{MFA valid?}
  Verify2 -->|No| Inc2[Inc mfa_failed]
  Inc2 --> Lock2{Failed >= 5?}
  Lock2 -->|Yes| Lockout
  Lock2 -->|No| Err2([Wrong code]) --> InputMFA
  Verify2 -->|Yes| Issue
  Issue --> RegDev[Register / update trusted_device]
  RegDev --> LogOK[Audit: login_success]
  LogOK --> Return([Return tokens + device_id])
  Return --> End([End])
```

### 3.2 Mobile Clock-In with Face + GPS + Anti-Fraud (US-020)

```mermaid
flowchart TB
  Start([Tap 'Clock In']) --> Perm{Camera + Location permission?}
  Perm -->|No| Ask[Request permission] --> Perm
  Perm -->|Yes| GetGPS[Get GPS coords + accuracy + mock_flag]
  GetGPS --> Acc{Accuracy ≤ 100m?}
  Acc -->|No| ErrAcc([Try again — weak signal]) --> End1([End])
  Acc -->|Yes| Live[Liveness challenge: blink + head turn]
  Live --> LiveOK{Pass?}
  LiveOK -->|No| ErrLive([Hold steady, try again]) --> Live
  LiveOK -->|Yes| Capture[Capture selfie frame]
  Capture --> Upload[POST /v1/attendance/clock-in<br/>multipart: photo + payload JSON]
  Upload --> SrvGeo[Server: validate geofence]
  SrvGeo --> InGeo{Within radius?}
  InGeo -->|No, not WFH today| Reject1[Reject + log] --> ErrGeo([Outside office area]) --> End2([End])
  InGeo -->|Yes / WFH allowed| FaceMatch[Compute embedding + match enrolled]
  FaceMatch --> Cos{Cosine ≥ 0.85?}
  Cos -->|No| Reject2[Reject + log] --> ErrFace([Face mismatch]) --> End3([End])
  Cos -->|Yes| Fraud[Run anti-fraud rules:<br/>mock_flag, speed teleport, IP geo]
  Fraud --> Score{fraud_score ≥ 70?}
  Score -->|Yes| Flag[Insert with status='pending_review']
  Flag --> Alert[Alert HR Admin via notif]
  Score -->|No| Insert[Insert attendance_log status=present/late]
  Insert --> Audit[Audit log + emit AttendanceClocked event]
  Flag --> Audit
  Audit --> Resp[Return success + summary]
  Resp --> NotifEmp[Push notif to employee]
  NotifEmp --> End([End])
```

### 3.3 Payroll Run End-to-End (US-040 + 043 + 044)

```mermaid
flowchart TB
  Start([Finance clicks 'Run Payroll']) --> Sel[Select period + scope]
  Sel --> Create[POST /v1/payroll/runs]
  Create --> Status1[status=draft]
  Status1 --> Calc[POST /v1/payroll/runs/:id/calculate]
  Calc --> Async[Publish PayrollCalculationRequested event]
  Async --> Worker[payroll-worker pulls job]
  Worker --> Loop[For each employee in scope]
  Loop --> Attend[Fetch attendance & overtime period]
  Attend --> Comp[Resolve salary_components + structure]
  Comp --> Earn[Calc earnings: base + tunjangan + lembur + THR / bonus]
  Earn --> Bpjs[Call bpjs-service via REST: calc 5 program]
  Bpjs --> Tax[Call tax-service: calc PPh 21 TER monthly / annual]
  Tax --> Deduct[Sum deductions: BPJS_ee + PPh + loan_installment + other]
  Deduct --> Net[net = gross - deductions]
  Net --> Insert[INSERT payslip + payslip_lines + tax_calc + bpjs_calc]
  Insert --> More{More employees?}
  More -->|Yes| Loop
  More -->|No| Aggregate[Compute run totals]
  Aggregate --> Status2[status=calculated]
  Status2 --> Notif1[Notify finance: ready to review]
  Notif1 --> Review[Finance reviews summary + individual slips]
  Review --> Edit{Any adjustment?}
  Edit -->|Yes| RecalcOne[Recalc single employee] --> Review
  Edit -->|No| ReqApp[Request approval]
  ReqApp --> Status3[status=reviewed]
  Status3 --> Approver[Different user — 4-eyes principle]
  Approver --> ApprDec{Approve?}
  ApprDec -->|Reject| Status4[status=cancelled] --> EndR([End])
  ApprDec -->|Approve| Status5[status=approved]
  Status5 --> ParallelGen[Parallel: PDF slip + bank file + bukti potong]
  ParallelGen --> Send[Send notif: email + push + WA]
  Send --> Status6[status=paid after pay_date]
  Status6 --> Audit[Audit log every transition]
  Audit --> End([End])
```

### 3.4 Reimbursement with OCR + Fraud Detection (US-060 + 061)

```mermaid
flowchart TB
  Start([Employee opens Reimburse form]) --> Cat[Select category]
  Cat --> Photo[Take photo of receipt]
  Photo --> Upload1[Upload to S3 + send to OCR service]
  Upload1 --> OCR[OCR extract: vendor, date, total, items]
  OCR --> Verify[Show extracted, user verifies / edits]
  Verify --> Submit[Submit reimbursement_request]
  Submit --> FraudJob[Async: fraud-detection job]

  subgraph FraudCheck [Fraud check pipeline]
    FraudJob --> ELA[Image manipulation ELA score]
    ELA --> pHash[Compute pHash + OCR text hash]
    pHash --> DupSearch[Search duplicates last 24mo this tenant]
    DupSearch --> Heur[Heuristics: round number, weekend, outlier]
    Heur --> Aggregate[Aggregate fraud_score]
  end

  Aggregate --> Decide{fraud_score?}
  Decide -->|< 40| AutoOK[status=submitted → workflow start]
  Decide -->|40-70| Review[status=pending_review, queue for HR]
  Decide -->|> 70| Block[status=rejected, alert HR + audit]

  AutoOK --> Wf[Approval workflow: manager → finance]
  Review --> HRDecide{HR approves manual?}
  HRDecide -->|Yes| Wf
  HRDecide -->|No| Block
  Wf --> Final{All approved?}
  Final -->|No| Rej([End: rejected])
  Final -->|Yes| Queue[Queue for next payroll run]
  Queue --> End([End])
```

### 3.5 Leave Request + Multi-Level Approval (US-030 + 031)

```mermaid
flowchart TB
  Start([Employee submits leave]) --> Validate[Validate: balance, conflict, min_notice]
  Validate --> OK{Valid?}
  OK -->|No| Err([Show validation error]) --> End1([End])
  OK -->|Yes| Create[INSERT leave_request status=pending]
  Create --> Wf[Lookup approval_workflow for module=leave]
  Wf --> Step1[Step 1: direct manager]
  Step1 --> Notif1[Send notif to manager]
  Notif1 --> Wait1{Manager action?}
  Wait1 -->|Reject| StatusRej[status=rejected] --> NotifEmp1[Notify employee]
  NotifEmp1 --> End2([End])
  Wait1 -->|Approve| Next{More steps?}
  Wait1 -->|Timeout 48h| Escalate[Escalate to HR / auto-approve per policy]
  Escalate --> Next
  Next -->|Yes| StepN[Next approver: HR / Director]
  StepN --> NotifN[Send notif]
  NotifN --> WaitN{Action?}
  WaitN -->|Reject| StatusRej
  WaitN -->|Approve| Next
  Next -->|No| Final[status=approved]
  Final --> UpdBal[Update leave_balance: pending → used]
  UpdBal --> Calendar[Push to team calendar]
  Calendar --> NotifEmp2[Notify employee: approved]
  NotifEmp2 --> Audit[Audit log]
  Audit --> End([End])
```

### 3.6 Recruitment — Apply to Hire (Phase 2)

```mermaid
flowchart TB
  Start([Candidate visits careers.acme.com]) --> Browse[Browse open positions]
  Browse --> Apply[Click Apply → fill form + upload CV]
  Apply --> Parse[Async: AI parse CV + create candidate]
  Parse --> Match[AI match score vs JD]
  Match --> Insert[Insert application, stage=Sourced]
  Insert --> NotifRec[Notify recruiter]
  NotifRec --> Screen[Recruiter screens → move to Screening]
  Screen --> Decide1{Pass?}
  Decide1 -->|No| Reject1[Move to Rejected + send polite email]
  Decide1 -->|Yes| Sched[Schedule interview round 1]
  Sched --> Cal[Sync Google Calendar + email candidate]
  Cal --> Interview[Conduct interview]
  Interview --> Feedback[Interviewers submit feedback]
  Feedback --> Decide2{Strong yes?}
  Decide2 -->|No| Reject1
  Decide2 -->|Yes| MoreRounds{More rounds?}
  MoreRounds -->|Yes| Sched
  MoreRounds -->|No| Offer[Generate offer letter]
  Offer --> Send[Send to candidate]
  Send --> Resp{Accept?}
  Resp -->|Decline| LogD[Log + analyze decline reason] --> End1([End])
  Resp -->|Accept| Hire[Create employee record]
  Hire --> Onboard[Trigger onboarding workflow: docs, IT setup, asset]
  Onboard --> End([End])
```

### 3.7 Employee Loan Request (US-070)

```mermaid
flowchart TB
  Start([Employee submits loan request]) --> Elig[Calculate eligibility]
  Elig --> Cek{cicilan total ≤ 30% take-home? & masa kerja OK?}
  Cek -->|No| Reject([Show: tidak eligible]) --> End1([End])
  Cek -->|Yes| Schedule[Generate schedule cicilan]
  Schedule --> Submit[Create loan record status=pending]
  Submit --> Wf[Workflow: manager → HR → Finance]
  Wf --> Approve{All approved?}
  Approve -->|No| StatusRej[status=rejected]
  StatusRej --> Notif1[Notify employee]
  Notif1 --> End2([End])
  Approve -->|Yes| Disburse[Finance disburse — manual transfer OR add to next payroll]
  Disburse --> Status[status=disbursed, create installments]
  Status --> Audit[Audit log]
  Audit --> NextPayroll[Each next payroll: auto-deduct installment]
  NextPayroll --> Done{All paid?}
  Done -->|No| NextPayroll
  Done -->|Yes| Closed[status=closed]
  Closed --> End([End])
```

### 3.8 Daily Fraud Scan Bot (US-133)

```mermaid
flowchart TB
  Cron([02:00 daily cron]) --> Loop[For each active tenant]
  Loop --> Attend[Scan yesterday attendance]
  Attend --> A1[Find: late_count outlier per employee]
  A1 --> A2[Find: clock-in pattern same coords always]
  A2 --> A3[Find: face match score borderline trending down]
  Loop --> Reim[Scan last 7 days reimbursement]
  Reim --> R1[Cross-employee duplicate receipts]
  R1 --> R2[Vendor outlier]
  Loop --> Loan[Scan loan exposure]
  Loan --> L1[Employees with cicilan > 25%]
  A3 --> Agg[Aggregate report]
  R2 --> Agg
  L1 --> Agg
  Agg --> Digest[Generate digest per tenant]
  Digest --> Send[Email + in-app to HR Admin]
  Send --> Audit[Audit job execution]
  Audit --> End([End])
```

### 3.9 Tenant Onboarding Wizard (US-004)

```mermaid
flowchart TB
  Start([New tenant signup confirmed]) --> Step1[Step 1: Company info]
  Step1 --> Step2[Step 2: Add branches]
  Step2 --> Step3[Step 3: Invite users + assign roles]
  Step3 --> Step4[Step 4: Choose payroll components template]
  Step4 --> Step5[Step 5: Upload karyawan Excel]
  Step5 --> Validate[Validate rows, show errors]
  Validate --> Fix{Errors?}
  Fix -->|Yes| Edit[Edit Excel + re-upload] --> Validate
  Fix -->|No| Import[Background job import]
  Import --> Wait[Show progress]
  Wait --> Done[Notify done]
  Done --> Checklist[Setup checklist 80% complete]
  Checklist --> Next[Suggest next: enroll faces, set work_location]
  Next --> End([End])
```

### 3.10 Year-End Tax Reconciliation (US-048)

```mermaid
flowchart TB
  Cron([January 1, 03:00 cron]) --> Loop[For each tenant with payroll active]
  Loop --> Employees[For each employee active in prev year]
  Employees --> Sum[Sum YTD gross, deductions, PPh paid]
  Sum --> Calc[Annual progressive PPh calc]
  Calc --> Diff{YTD paid vs annual?}
  Diff -->|Underpaid| Owe[Mark: employee owes Rp X — deduct next payroll]
  Diff -->|Overpaid| Refund[Mark: refund Rp X via next payroll]
  Diff -->|Equal| OK[No adjustment]
  Owe --> GenBP
  Refund --> GenBP
  OK --> GenBP[Generate Bukti Potong 1721-A1 PDF + e-Bupot XML]
  GenBP --> Upload[Upload to S3, set signed URL]
  Upload --> Notif[Notify HR + Employee: BP ready]
  Notif --> End([End])
```
