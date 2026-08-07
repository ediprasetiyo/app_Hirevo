# Hirevo Design System

Design tokens + wireframes for Hirevo HRIS UI (web + mobile).

## Files

| Path | Purpose |
|------|---------|
| `tokens/base.json` | DTCG-format base tokens (colors, type, spacing, radius, shadow) |
| `tokens/light.json` | Semantic tokens for light theme |
| `tokens/dark.json` | Semantic tokens for dark theme (overrides) |
| `tokens/brand.json` | Brand accent — swap this file per tenant white-label |
| `wireframes/*.svg` | Low-fidelity SVG mockups for key screens |

## Format

Tokens follow the [DTCG](https://tr.designtokens.org/format/) spec so they load in:
- **Tokens Studio for Figma** (import JSON → sync design ↔ code)
- **Style Dictionary** (transform → Tailwind config + Flutter theme)

## Pipeline

```
design/tokens/*.json
   │
   ├──► Tokens Studio  ──► Figma variables (designers)
   │
   └──► Style Dictionary build:
          ├──► web/tailwind.config.ts (CSS variables)
          ├──► mobile/lib/app/theme.dart (Flutter ColorScheme + TextTheme)
          └──► docs/tokens.md (reference)
```

## Wireframes

Wireframes are **low-fi** — they establish layout intent, not final visual design. Real high-fidelity mocks live in Figma and evolve independently. Keep these SVGs updated when the underlying layout changes so they stay honest reference material.

| File | Screen | Platform |
|------|--------|----------|
| `01-login.svg` | Login + MFA screen | Web |
| `02-hr-dashboard.svg` | HR dashboard (headcount, payroll, attendance widgets) | Web |
| `03-payroll-run.svg` | Payroll run wizard summary step | Web |
| `04-employee-detail.svg` | Employee detail with tabbed sidebar | Web |
| `05-mobile-home.svg` | Employee mobile home screen | Mobile |
| `06-mobile-clock-in.svg` | Mobile clock-in with face + GPS | Mobile |
| `07-mobile-slip-gaji.svg` | Mobile payslip view | Mobile |
| `08-reimburse-approve.svg` | Reimbursement approval with fraud signals | Web |

## Brand Placeholder

The base palette uses neutral grays + a Hirevo brand purple (`#5B4FE6`). To white-label per tenant Enterprise, only `tokens/brand.json` needs updating; downstream tokens reference it via `{brand.accent.500}`.

## Contrast

All semantic pairs meet **WCAG 2.1 AA**: 4.5:1 for body text, 3:1 for large text and UI components. Validated in `tokens/contrast-report.md` (generated).
