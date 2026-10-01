package com.pedestriamc.ghasts.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigFilesTest {
  @TempDir Path directory;

  @Test
  void cleanInstallCreatesBothFilesWithValidDefaults() {
    var settings = PluginSettings.from(ConfigFiles.load(directory));
    assertEquals(0.075, settings.speeds().riding());
    assertTrue(settings.disabledWorlds().isEmpty());
    assertTrue(Files.exists(directory.resolve("messages.yml")));
  }

  @Test
  void migrationPreservesWorldsSpeedsAndCustomMessagesAndBacksUpOnce() throws Exception {
    String old =
        """
                default-riding-speed: 0.2
                disabled-worlds: [lobby, lobby]
                messages:
                  world-disabled: '<red>Custom denial</red>'
                """;
    Files.writeString(directory.resolve("config.yml"), old);
    var settings = PluginSettings.from(ConfigFiles.load(directory));
    assertEquals(0.2, settings.speeds().riding());
    assertEquals(java.util.Set.of("lobby"), settings.disabledWorlds());
    assertEquals(
        "<red>Custom denial</red>",
        ConfigFiles.loadMessages(directory).getString("world-disabled"));
    assertEquals(old, Files.readString(directory.resolve("config.yml.v1.bak")));
    ConfigFiles.load(directory);
    assertEquals(old, Files.readString(directory.resolve("config.yml.v1.bak")));
  }

  @Test
  void existingMessagesWinOverLegacyMessages() throws Exception {
    Files.writeString(directory.resolve("config.yml"), "messages:\n  world-disabled: legacy\n");
    Files.writeString(directory.resolve("messages.yml"), "world-disabled: current\n");
    ConfigFiles.load(directory);
    assertEquals("current", ConfigFiles.loadMessages(directory).getString("world-disabled"));
  }

  @Test
  void invalidConfigurationDoesNotOverwriteOriginal() throws Exception {
    String invalid = "idle-speed: -1\n";
    Files.writeString(directory.resolve("config.yml"), invalid);
    var exception = assertThrows(IllegalArgumentException.class, () -> ConfigFiles.load(directory));
    assertTrue(java.util.Objects.requireNonNull(exception.getMessage()).contains("idle-speed"));
    assertEquals(invalid, Files.readString(directory.resolve("config.yml")));
  }

  @Test
  void malformedYamlHasAnExplicitFileError() throws Exception {
    Files.writeString(directory.resolve("config.yml"), "enchantment: [\n");
    assertThrows(IllegalStateException.class, () -> ConfigFiles.load(directory));
  }

  @Test
  void invalidCostsRejectIntegerOverflow() {
    var config = ConfigFiles.load(directory);
    config.set("enchantment.cost.maximum", Integer.MAX_VALUE);
    assertThrows(IllegalArgumentException.class, () -> PluginSettings.from(config));
    config.set("enchantment.cost.maximum", 15);
    config.set("enchantment.cost.minimum", 0);
    assertThrows(IllegalArgumentException.class, () -> PluginSettings.from(config));
  }

  @Test
  void customLevelSetIsPreservedWithoutAddingDefaultLevels() throws Exception {
    Files.writeString(directory.resolve("config.yml"), "enchantment:\n  levels:\n    1: 0.2\n");
    var settings = PluginSettings.from(ConfigFiles.load(directory));
    assertEquals(java.util.Map.of(1, 0.2), settings.speeds().levels());
  }

  @Test
  void wrongTypesAndRegistryKeysAreRejected() {
    var config = ConfigFiles.load(directory);
    config.set("disabled-worlds", java.util.List.of(7));
    assertThrows(IllegalArgumentException.class, () -> PluginSettings.from(config));
    config.set("disabled-worlds", java.util.List.of());
    config.set("enchantment.name", "Invalid key");
    assertThrows(IllegalArgumentException.class, () -> PluginSettings.from(config));
  }
}
