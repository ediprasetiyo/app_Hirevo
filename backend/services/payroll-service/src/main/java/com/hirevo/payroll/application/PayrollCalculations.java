package com.hirevo.payroll.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BPJS + PPh21 math for one employee's monthly payslip.
 *
 * <p><b>These are simplified approximations for demo/MVP purposes — not the
 * exact government lookup tables.</b> A production system must:
 * <ul>
 *   <li>Load the official PMK 168/2023 monthly TER bracket tables (categories
 *       TER A/B/C, ~40+ brackets each) instead of the 5-bracket approximation
 *       used here, and apply the year-end recalculation (PPh21 Pasal 17)
 *       required by law instead of treating each month as final.</li>
 *   <li>Track PTKP category from actual marital-status + dependents-count
 *       data (dependents aren't captured in employee-service yet — this
 *       falls back to single/married inferred from maritalStatus only).</li>
 *   <li>Use the current-year BPJS wage caps and rates (JHT/JP/JKK/JKM are
 *       combined into one flat rate here instead of computed separately per
 *       scheme, and JKK's risk-class-dependent rate is fixed at the lowest
 *       tier).</li>
 * </ul>
 */
public final class PayrollCalculations {

  private PayrollCalculations() {}

  private static final BigDecimal BPJS_KES_CAP = new BigDecimal("12000000");
  private static final BigDecimal BPJS_KES_EMPLOYEE_RATE = new BigDecimal("0.01");
  private static final BigDecimal BPJS_KES_EMPLOYER_RATE = new BigDecimal("0.04");

  private static final BigDecimal BPJS_TK_CAP = new BigDecimal("12000000");
  // JHT 2% + JP 1%
  private static final BigDecimal BPJS_TK_EMPLOYEE_RATE = new BigDecimal("0.03");
  // JHT 3.7% + JP 2% + JKK 0.24% (lowest risk tier) + JKM 0.3%
  private static final BigDecimal BPJS_TK_EMPLOYER_RATE = new BigDecimal("0.0624");

  public record BpjsResult(BigDecimal employeeAmount, BigDecimal employerAmount) {}

  public static BpjsResult bpjsKesehatan(BigDecimal base) {
    BigDecimal capped = base.min(BPJS_KES_CAP);
    return new BpjsResult(
        capped.multiply(BPJS_KES_EMPLOYEE_RATE).setScale(0, RoundingMode.HALF_UP),
        capped.multiply(BPJS_KES_EMPLOYER_RATE).setScale(0, RoundingMode.HALF_UP));
  }

  public static BpjsResult bpjsKetenagakerjaan(BigDecimal base) {
    BigDecimal capped = base.min(BPJS_TK_CAP);
    return new BpjsResult(
        capped.multiply(BPJS_TK_EMPLOYEE_RATE).setScale(0, RoundingMode.HALF_UP),
        capped.multiply(BPJS_TK_EMPLOYER_RATE).setScale(0, RoundingMode.HALF_UP));
  }

  /** Monthly PTKP-like category, inferred from marital status only (no dependents data yet). */
  public static String terCategory(String maritalStatus) {
    return "married".equalsIgnoreCase(maritalStatus) ? "TER B" : "TER A";
  }

  private record Bracket(BigDecimal upTo, BigDecimal rate) {}

  // Simplified 5-bracket approximation of the monthly TER schedule, per category.
  private static final Bracket[] TER_A = {
      new Bracket(new BigDecimal("5400000"), BigDecimal.ZERO),
      new Bracket(new BigDecimal("6200000"), new BigDecimal("0.005")),
      new Bracket(new BigDecimal("10050000"), new BigDecimal("0.015")),
      new Bracket(new BigDecimal("21850000"), new BigDecimal("0.03")),
      new Bracket(null, new BigDecimal("0.05")),
  };

  private static final Bracket[] TER_B = {
      new Bracket(new BigDecimal("6200000"), BigDecimal.ZERO),
      new Bracket(new BigDecimal("7300000"), new BigDecimal("0.005")),
      new Bracket(new BigDecimal("11600000"), new BigDecimal("0.015")),
      new Bracket(new BigDecimal("24100000"), new BigDecimal("0.03")),
      new Bracket(null, new BigDecimal("0.05")),
  };

  /** Approximate monthly PPh21 (TER method) on gross taxable income. */
  public static BigDecimal pph21(BigDecimal grossTaxable, String terCategory) {
    Bracket[] brackets = "TER B".equals(terCategory) ? TER_B : TER_A;
    for (Bracket b : brackets) {
      if (b.upTo() == null || grossTaxable.compareTo(b.upTo()) <= 0) {
        return grossTaxable.multiply(b.rate()).setScale(0, RoundingMode.HALF_UP);
      }
    }
    return BigDecimal.ZERO;
  }
}
