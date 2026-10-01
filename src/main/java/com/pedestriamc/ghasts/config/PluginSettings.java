package com.pedestriamc.ghasts.config;

import com.pedestriamc.ghasts.domain.RidingSpeeds;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record PluginSettings(
    RidingSpeeds speeds,
    Set<String> disabledWorlds,
    boolean metrics,
    boolean enchantmentEnabled,
    String enchantmentName,
    String description,
    int weight,
    int minimumCost,
    int minimumModifier,
    int maximumCost,
    int maximumModifier,
    int anvilCost) {
  public PluginSettings {
    disabledWorlds = Set.copyOf(disabledWorlds);
    if (!enchantmentName.matches("[a-z0-9._/-]+"))
      throw new IllegalArgumentException(
          "enchantment.name must use lowercase letters, digits or . _ / -");
    if (description.isBlank())
      throw new IllegalArgumentException("enchantment.description must not be blank");
    if (weight < 1 || weight > 1024)
      throw new IllegalArgumentException("enchantment.weight must be 1..1024");
    validateCost("minimum", minimumCost);
    validateCost("minimum-modifier", minimumModifier);
    validateCost("maximum", maximumCost);
    validateCost("maximum-modifier", maximumModifier);
    validateCost("anvil", anvilCost);
    if (minimumCost < 1)
      throw new IllegalArgumentException("enchantment.cost.minimum must be positive");
    long levelOffset = speeds.maxLevel() - 1L;
    long min = minimumCost + levelOffset * minimumModifier;
    long max = maximumCost + levelOffset * maximumModifier;
    if (maximumCost < minimumCost || max < min || max > Integer.MAX_VALUE - 1L)
      throw new IllegalArgumentException(
          "enchantment.cost: maximum must cover minimum at every level and fit in an integer");
  }

  public int maximumEnchantability() {
    return maximumCost + (speeds.maxLevel() - 1) * maximumModifier;
  }

  public static PluginSettings from(FileConfiguration config) {
    ConfigurationSection section = config.getConfigurationSection("enchantment.levels");
    if (section == null) throw new IllegalArgumentException("enchantment.levels must be a mapping");
    Map<Integer, Double> levels = new HashMap<>();
    for (String key : section.getKeys(false)) {
      int level;
      try {
        level = Integer.parseInt(key);
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException(
            "enchantment.levels." + key + ": expected an integer level", exception);
      }
      if (!key.equals(Integer.toString(level)))
        throw new IllegalArgumentException(
            "enchantment.levels." + key + ": use canonical integer keys");
      levels.put(level, number(config, "enchantment.levels." + key));
    }
    if (!config.isList("disabled-worlds")
        || config.getList("disabled-worlds").stream()
            .anyMatch(value -> !(value instanceof String name) || name.isBlank()))
      throw new IllegalArgumentException("disabled-worlds must be a list of nonempty world names");
    return new PluginSettings(
        new RidingSpeeds(
            number(config, "idle-speed"), number(config, "default-riding-speed"), levels),
        Set.copyOf(config.getStringList("disabled-worlds")),
        bool(config, "metrics"),
        bool(config, "enchantment.enable"),
        string(config, "enchantment.name"),
        string(config, "enchantment.description"),
        integer(config, "enchantment.weight"),
        integer(config, "enchantment.cost.minimum"),
        integer(config, "enchantment.cost.minimum-modifier"),
        integer(config, "enchantment.cost.maximum"),
        integer(config, "enchantment.cost.maximum-modifier"),
        integer(config, "enchantment.cost.anvil"));
  }

  private static void validateCost(String key, int value) {
    if (value < 0)
      throw new IllegalArgumentException("enchantment.cost." + key + " must be nonnegative");
  }

  private static double number(FileConfiguration config, String key) {
    if (!(config.get(key) instanceof Number value))
      throw new IllegalArgumentException(key + " must be numeric");
    return value.doubleValue();
  }

  private static int integer(FileConfiguration config, String key) {
    if (!config.isInt(key)) throw new IllegalArgumentException(key + " must be an integer");
    return config.getInt(key);
  }

  private static boolean bool(FileConfiguration config, String key) {
    if (!config.isBoolean(key)) throw new IllegalArgumentException(key + " must be true or false");
    return config.getBoolean(key);
  }

  private static String string(FileConfiguration config, String key) {
    if (!config.isString(key)) throw new IllegalArgumentException(key + " must be a string");
    return config.getString(key);
  }
}
