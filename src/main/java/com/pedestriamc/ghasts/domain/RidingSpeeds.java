package com.pedestriamc.ghasts.domain;

import java.util.Map;

public record RidingSpeeds(double idle, double riding, Map<Integer, Double> levels) {
  public RidingSpeeds {
    validateSpeed("idle-speed", idle);
    validateSpeed("default-riding-speed", riding);
    levels = Map.copyOf(levels);
    if (levels.isEmpty())
      throw new IllegalArgumentException("enchantment.levels must not be empty");
    levels.forEach(
        (level, speed) -> {
          if (level < 1 || level > 255)
            throw new IllegalArgumentException("enchantment.levels: level must be 1..255");
          validateSpeed("enchantment.levels." + level, speed);
        });
  }

  public double forLevel(int level) {
    return levels.getOrDefault(level, riding);
  }

  public int maxLevel() {
    return levels.keySet().stream().mapToInt(Integer::intValue).max().orElseThrow();
  }

  private static void validateSpeed(String key, double value) {
    if (!Double.isFinite(value) || value < 0 || value > 1)
      throw new IllegalArgumentException(key + " must be a finite number in 0..1");
  }
}
