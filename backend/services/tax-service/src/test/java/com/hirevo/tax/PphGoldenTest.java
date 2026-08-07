package com.hirevo.tax;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Golden-file test runner for the PPh 21 rule engine.
 *
 * <p>Loads every {@code *.json} under {@code fixtures/pph21/}, executes each case
 * against the current engine, and asserts output matches the {@code expected} block
 * to the rupiah. When a fixture fails, the failure message includes the case id
 * and regulation citation so a reviewer can decide: fix engine, or update fixture
 * (only after confirming regulation actually changed).
 *
 * <p>Runner is <b>engine-agnostic</b> — it depends on {@code PphEngine} interface
 * from {@code hirevo-rule-engine}. Swap implementations without touching fixtures.
 */
@DisplayName("PPh 21 Golden Fixtures")
class PphGoldenTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Path FIXTURES = Paths.get("src/test/resources/fixtures/pph21");

  /** Stub — inject the real engine bean in the running test context. */
  private final PphEngineStub engine = new PphEngineStub();

  static Stream<GoldenCase> allCases() throws IOException {
    List<GoldenCase> cases = new ArrayList<>();
    try (var files = Files.list(FIXTURES)) {
      files.filter(p -> p.toString().endsWith(".json")).forEach(f -> {
        try {
          JsonNode root = MAPPER.readTree(f.toFile());
          String rulepackVersion = root.path("rulepack_version").asText();
          for (JsonNode c : root.path("cases")) {
            cases.add(new GoldenCase(
                f.getFileName().toString(),
                c.path("id").asText(),
                c.path("description").asText(),
                rulepackVersion,
                c.path("input"),
                c.path("expected")));
          }
        } catch (IOException e) {
          throw new RuntimeException("Failed to read fixture " + f, e);
        }
      });
    }
    return cases.stream();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("allCases")
  void goldenCase(GoldenCase gc) {
    // Error-expecting cases
    if (gc.expected.has("error")) {
      String expectedError = gc.expected.path("error").asText();
      try {
        engine.calculate(gc.input, gc.rulepackVersion);
      } catch (Exception ex) {
        assertThat(ex.getClass().getSimpleName())
            .as("[%s] expected %s", gc.id, expectedError)
            .isEqualTo(expectedError);
        return;
      }
      throw new AssertionError("[" + gc.id + "] expected " + expectedError + " but no exception thrown");
    }

    PphEngineStub.Result actual = engine.calculate(gc.input, gc.rulepackVersion);

    if (gc.expected.has("ter_category")) {
      assertThat(actual.terCategory)
          .as("[%s] TER category", gc.id)
          .isEqualTo(gc.expected.path("ter_category").asText());
    }
    if (gc.expected.has("ter_rate_percent")) {
      assertThat(actual.terRate.doubleValue())
          .as("[%s] TER rate", gc.id)
          .isCloseTo(gc.expected.path("ter_rate_percent").asDouble(), within(1e-6));
    }
    // Zero-rupiah tolerance on the money bottom line — engine is deterministic.
    assertThat(actual.pph21Amount)
        .as("[%s] %s — PPh 21 amount", gc.id, gc.description)
        .isEqualByComparingTo(new BigDecimal(gc.expected.path("pph21_amount").asText()));

    // Annual specifics
    if (gc.expected.has("pkp")) {
      assertThat(actual.pkp)
          .as("[%s] PKP", gc.id)
          .isEqualByComparingTo(new BigDecimal(gc.expected.path("pkp").asText()));
    }
    if (gc.expected.has("jabatan_deduction")) {
      assertThat(actual.jabatanDeduction)
          .as("[%s] biaya jabatan", gc.id)
          .isEqualByComparingTo(new BigDecimal(gc.expected.path("jabatan_deduction").asText()));
    }
    if (gc.expected.has("annual_pph21")) {
      assertThat(actual.annualPph21)
          .as("[%s] annual PPh 21", gc.id)
          .isEqualByComparingTo(new BigDecimal(gc.expected.path("annual_pph21").asText()));
    }
  }

  record GoldenCase(String file, String id, String description, String rulepackVersion,
                    JsonNode input, JsonNode expected) {
    @Override public String toString() { return id + " · " + description; }
  }

  /**
   * Placeholder engine for the scaffold — real implementation lives in
   * {@code com.hirevo.rule.tax.PphTerEngine} (Sprint 9). The stub exists so the
   * test class compiles alongside the fixtures. Swap for the real bean via
   * {@code @Autowired} once the engine is written.
   */
  static class PphEngineStub {
    Result calculate(JsonNode input, String rulepackVersion) {
      throw new UnsupportedOperationException(
          "PphEngineStub.calculate — implement in com.hirevo.rule.tax.PphTerEngine (Sprint 9). "
          + "Once implemented, replace this stub with the real bean via constructor injection.");
    }

    static class Result {
      String terCategory;
      BigDecimal terRate;
      BigDecimal pph21Amount;
      BigDecimal pkp;
      BigDecimal jabatanDeduction;
      BigDecimal annualPph21;
    }
  }
}
