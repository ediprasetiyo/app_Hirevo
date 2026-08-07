# PPh 21 Golden Test Fixtures

Comprehensive test scenarios for the PPh 21 rule engine — the highest-stakes calculation in Hirevo. **Every fixture is a contract**: if the engine's output diverges from the expected value, either the engine changed behavior (bug or intentional) or the regulation changed (needs new rule-pack version).

## Scope

| File | Category | Cases |
|------|----------|-------|
| `01-ter-monthly-category-a.json` | TER Kategori A (TK/0, TK/1, K/0) | 20 |
| `02-ter-monthly-category-b.json` | TER Kategori B (TK/2, TK/3, K/1, K/2) | 20 |
| `03-ter-monthly-category-c.json` | TER Kategori C (K/3) | 15 |
| `04-annual-december.json` | Desember reconciliation (annual progressive) | 20 |
| `05-thr-bonus.json` | THR + bonus tahunan (PP 36/2021 + PMK 168) | 15 |
| `06-prorata-join-resign.json` | Karyawan masuk/keluar tengah tahun | 15 |
| `07-non-npwp.json` | Tanpa NPWP (pre-2024 penalty 20%) | 10 |
| `08-edge-cases.json` | Threshold, high-income, extreme scenarios | 15 |
| **Total** | — | **130** |

## Schema

```jsonc
{
  "$schema": "pph21-fixture-v1",
  "rulepack_version": "2024.01",       // must match tax_brackets_ter effective row
  "description": "…",
  "cases": [
    {
      "id": "TER-A-001",                // stable — never renumber
      "description": "…",
      "regulation": "PMK 168/2023 Pasal 3(1)",  // legal citation
      "input": {
        "period":            { "year": 2026, "month": 7 },
        "ptkp_code":         "TK/0",
        "has_npwp":          true,
        "tax_method":        "gross",   // gross | gross_up | net
        "gross_taxable":     8000000,
        "calculation_type":  "ter_monthly", // ter_monthly | annual_december | annual_resign | thr | bonus
        "ytd_gross":         null,        // required for annual_*
        "ytd_pph21_paid":    null
      },
      "expected": {
        "ter_category":      "A",
        "ter_rate_percent":  0.0175,
        "pph21_amount":      140000,      // rounded to nearest rupiah
        "calculation_notes": "8jt × 1.75% (TER A bracket 7.5-8.55jt)"
      }
    }
  ]
}
```

## Adding a new case

1. Bump the **id** monotonically within its category prefix (never reuse).
2. Cite the **regulation** (PMK/PP/Perpres pasal).
3. Compute expected value **by hand** or with an independent tool (Excel + DJP e-Bupot).
4. If a case fails after a rule-pack change, **do not blindly update the expected** — first confirm the change is intended (PMK amended) and bump `rulepack_version`.

## Runner

See `PphGoldenTest.java` — parameterized JUnit 5 test that iterates all fixtures.

## Rule pack version compatibility

Each fixture file declares `rulepack_version`. The runner asserts that the engine loads the same version. When DJP publishes a new PMK, add fixtures under a new `rulepack_version` — old fixtures remain for regression against historical periods.

## What is NOT tested here

- BPJS calculations → `bpjs-service/src/test/resources/fixtures/bpjs/`
- Overtime PP 35/2021 → `payroll-service/.../fixtures/overtime/`
- End-to-end payroll run → integration tests
