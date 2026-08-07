/**
 * Rule-pack engines for Indonesian regulatory calculations.
 *
 * <p>Sub-packages:
 * <ul>
 *   <li>{@code tax} — PPh 21 TER (PMK 168/2023) + annual progressive.
 *   <li>{@code bpjs} — 5 programs with versioned rates & caps.
 *   <li>{@code overtime} — PP 35/2021 multipliers.
 * </ul>
 *
 * <p>Rule packs are versioned via {@code effective_from/to} in the database
 * (see tables {@code tax.tax_brackets_ter}, {@code bpjs.bpjs_rates}) — new
 * regulations ship as data changes, not code releases.
 */
package com.hirevo.rule;
