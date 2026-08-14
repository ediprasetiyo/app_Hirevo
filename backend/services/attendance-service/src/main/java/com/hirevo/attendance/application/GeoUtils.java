package com.hirevo.attendance.application;

import java.math.BigDecimal;

/** Haversine great-circle distance — good enough accuracy for geofence radii of tens-hundreds of meters. */
public final class GeoUtils {

  private static final double EARTH_RADIUS_METERS = 6_371_000;

  private GeoUtils() {}

  public static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
    double phi1 = Math.toRadians(lat1.doubleValue());
    double phi2 = Math.toRadians(lat2.doubleValue());
    double dPhi = Math.toRadians(lat2.subtract(lat1).doubleValue());
    double dLambda = Math.toRadians(lon2.subtract(lon1).doubleValue());

    double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
        + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_METERS * c;
  }
}
