package com.pedestriamc.ghasts.enchantment;

import com.pedestriamc.ghasts.Ghasts;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpeedManager {

    private final Map<Integer, Double> speeds = new HashMap<>();

    private final double idleSpeed;

    private final double defaultSpeed;

    private final Enchantment enchantment;

    public SpeedManager(@NotNull Ghasts plugin) {
        FileConfiguration config = plugin.getConfig();
        idleSpeed = config.getDouble("idle-speed");
        defaultSpeed = config.getDouble("default-riding-speed");
        enchantment = loadEnchantment(config);
        loadLevels(config);
    }

    private Enchantment loadEnchantment(@NotNull FileConfiguration config) {
        if (!config.getBoolean("enchantment.enable")) {
            return null;
        }

        String keyString = config.getString("enchantment.name", "velocity");
        Key key = Key.key("pedestria:" + keyString);
        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(TypedKey.create(RegistryKey.ENCHANTMENT, key));
    }

    private void loadLevels(@NotNull FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("enchantment.levels");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                double speed = section.getDouble(key);
                speeds.put(level, speed);
            } catch(NumberFormatException ignored) {}
        }
    }

    @Nullable
    public Enchantment getEnchantment() {
        return enchantment;
    }

    public double getSpeed(int level) {
        if (speeds.containsKey(level)) {
            return speeds.get(level);
        } else {
            return getDefaultSpeed();
        }
    }

    public double getDefaultSpeed() {
        return defaultSpeed;
    }

    public double getIdleSpeed() {
        return idleSpeed;
    }

}
