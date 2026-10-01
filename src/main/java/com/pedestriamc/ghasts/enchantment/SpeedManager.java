package com.pedestriamc.ghasts.enchantment;

import com.pedestriamc.ghasts.config.PluginSettings;
import com.pedestriamc.ghasts.domain.RidingSpeeds;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

public final class SpeedManager {
  private final RidingSpeeds speeds;
  private final @Nullable Enchantment enchantment;

  public SpeedManager(PluginSettings settings) {
    speeds = settings.speeds();
    if (settings.enchantmentEnabled()) {
      enchantment =
          RegistryAccess.registryAccess()
              .getRegistry(RegistryKey.ENCHANTMENT)
              .get(
                  TypedKey.create(
                      RegistryKey.ENCHANTMENT, Key.key("pedestria", settings.enchantmentName())));
      if (enchantment == null)
        throw new IllegalStateException(
            "Enchantment was not registered: pedestria:" + settings.enchantmentName());
    } else {
      enchantment = null;
    }
  }

  public @Nullable Enchantment getEnchantment() {
    return enchantment;
  }

  public double getSpeed(int level) {
    return speeds.forLevel(level);
  }

  public double getDefaultSpeed() {
    return speeds.riding();
  }

  public double getIdleSpeed() {
    return speeds.idle();
  }
}
