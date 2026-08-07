package com.hirevo.core.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

/**
 * Central clock — inject this instead of {@link Instant#now()} for testability.
 * Default zone: Asia/Jakarta (WIB, UTC+7).
 */
@Component
public class HirevoClock {

  public static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

  private final Clock delegate;

  public HirevoClock() {
    this(Clock.system(JAKARTA));
  }

  public HirevoClock(Clock delegate) {
    this.delegate = delegate;
  }

  public Instant now() {
    return delegate.instant();
  }

  public ZonedDateTime nowJakarta() {
    return ZonedDateTime.now(delegate.withZone(JAKARTA));
  }

  public LocalDate today() {
    return LocalDate.now(delegate.withZone(JAKARTA));
  }
}
