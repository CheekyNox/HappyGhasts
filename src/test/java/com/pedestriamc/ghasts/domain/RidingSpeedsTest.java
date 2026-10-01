package com.pedestriamc.ghasts.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RidingSpeedsTest {
  @Test
  void validatesFiniteRangeAndLevelBounds() {
    for (double invalid : new double[] {-0.1, 1.01, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertThrows(
          IllegalArgumentException.class, () -> new RidingSpeeds(invalid, 0.1, Map.of(1, 0.1)));
      assertThrows(
          IllegalArgumentException.class, () -> new RidingSpeeds(0.1, invalid, Map.of(1, 0.1)));
      assertThrows(
          IllegalArgumentException.class, () -> new RidingSpeeds(0.1, 0.1, Map.of(1, invalid)));
    }
    assertThrows(IllegalArgumentException.class, () -> new RidingSpeeds(0, 1, Map.of()));
    assertThrows(IllegalArgumentException.class, () -> new RidingSpeeds(0, 1, Map.of(0, 0.1)));
    assertThrows(IllegalArgumentException.class, () -> new RidingSpeeds(0, 1, Map.of(256, 0.1)));
  }

  @Test
  void usesDefaultSpeedForUnenchantedHarnessAndSnapshotsLevels() {
    var levels = new HashMap<>(Map.of(1, 0.2));
    var speeds = new RidingSpeeds(0, 1, levels);
    levels.put(1, 0.8);
    assertEquals(0.2, speeds.forLevel(1));
    assertEquals(1, speeds.forLevel(-1));
  }
}
