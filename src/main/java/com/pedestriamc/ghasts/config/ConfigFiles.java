package com.pedestriamc.ghasts.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigFiles {
  private ConfigFiles() {}

  public static YamlConfiguration load(Path directory) {
    try {
      Files.createDirectories(directory);
      Path path = directory.resolve("config.yml");
      YamlConfiguration config = read(path, "config.yml");
      int version = config.getInt("config-version", 1);
      if (config.contains("config-version") && !config.isInt("config-version"))
        throw new IllegalArgumentException("config-version must be an integer");
      if (version < 1 || version > 2)
        throw new IllegalArgumentException("Unsupported config-version: " + version);
      YamlConfiguration messages = read(directory.resolve("messages.yml"), "messages.yml");
      if (version == 1 && Files.exists(path)) {
        Path backup = directory.resolve("config.yml.v1.bak");
        if (!Files.exists(backup)) Files.copy(path, backup);
        if (!Files.exists(directory.resolve("messages.yml"))) {
          var legacy = config.getConfigurationSection("messages");
          if (legacy != null)
            legacy
                .getValues(true)
                .forEach(
                    (key, value) -> {
                      if (!(value instanceof org.bukkit.configuration.ConfigurationSection))
                        messages.set(key, value);
                    });
          messages.set(
              "console-player-only",
              config.getString(
                  "messages.console-player-only", messages.getString("console-player-only")));
          // Replace the obsolete built-in help while preserving custom help.
          String help = messages.getString("help", "");
          if (help.contains("/givebook") && help.contains("Provides the version"))
            messages.set("help", defaults("messages.yml").getString("help"));
          String oldVersion = messages.getString("version", "");
          if (oldVersion.contains("<light-gray>"))
            messages.set("version", oldVersion.replace("light-gray", "gray"));
        }
        config.set("messages", null);
      }
      config.set("config-version", 2);
      config.options().copyDefaults(true);
      messages.options().copyDefaults(true);
      PluginSettings.from(config);
      write(directory.resolve("messages.yml"), messages);
      write(path, config);
      return config;
    } catch (IOException | InvalidConfigurationException exception) {
      throw new IllegalStateException(
          "Cannot load plugin configuration directory=" + directory, exception);
    }
  }

  public static YamlConfiguration loadMessages(Path directory) {
    try {
      return read(directory.resolve("messages.yml"), "messages.yml");
    } catch (IOException | InvalidConfigurationException exception) {
      throw new IllegalStateException("Cannot load messages.yml directory=" + directory, exception);
    }
  }

  private static YamlConfiguration read(Path path, String resource)
      throws IOException, InvalidConfigurationException {
    YamlConfiguration config = new YamlConfiguration();
    if (Files.exists(path)) config.load(path.toFile());
    YamlConfiguration defaults = defaults(resource);
    if (resource.equals("config.yml") && config.isSet("enchantment.levels"))
      defaults.set("enchantment.levels", null);
    config.setDefaults(defaults);
    return config;
  }

  private static YamlConfiguration defaults(String resource)
      throws IOException, InvalidConfigurationException {
    try (InputStream stream = ConfigFiles.class.getClassLoader().getResourceAsStream(resource)) {
      if (stream == null) throw new IOException("Missing resource " + resource);
      YamlConfiguration config = new YamlConfiguration();
      config.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return config;
    }
  }

  private static void write(Path path, YamlConfiguration config) throws IOException {
    Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
    try {
      config.save(temporary.toFile());
      try {
        Files.move(
            temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }
}
