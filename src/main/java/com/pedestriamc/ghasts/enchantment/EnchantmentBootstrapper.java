package com.pedestriamc.ghasts.enchantment;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

// https://docs.papermc.io/paper/dev/registries/#mutating-registries
@SuppressWarnings({"unused", "UnstableApiUsage"})
public class EnchantmentBootstrapper implements PluginBootstrap {

    public EnchantmentBootstrapper() {
        System.out.println("[Ghasts] Enchantment Bootstrapper loading...");
    }

    // Attempts to get the max enchantment level from the config, returns 3 if no levels are found.
    public static int getMaxLevel(@NotNull FileConfiguration config) {
        ConfigurationSection enchantments = config.getConfigurationSection("enchantment.levels");
        if (enchantments == null) {
            return 3;
        }

        int highest = 1;
        for (String key : enchantments.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                if (level > highest) {
                    highest = level;
                }
            } catch(NumberFormatException ignored) {}
        }

        return highest;
    }

    // https://jd.papermc.io/paper/1.21.7/io/papermc/paper/registry/data/EnchantmentRegistryEntry.Builder.html
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        EnchantmentData data = getData(context.getDataDirectory());

        if (!data.isEnabled) {
            System.out.println("[Ghasts] Enchantment disabled in config.yml, skipping...");
            return;
        }

        TypedKey<Enchantment> key = EnchantmentKeys.create(Key.key(data.keyString()));
        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                    event.registry().register(
                            key, builder -> builder
                                    .description(Component.text(data.description()))
                                    .maxLevel(data.maxLevel())
                                    .weight(data.weight())
                                    .minimumCost(data.minCost())
                                    .maximumCost(data.maxCost())
                                    .anvilCost(data.anvilCost())
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
                                    .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
                                    .activeSlots(EquipmentSlotGroup.ANY)
                    );
                    System.out.println("[Ghasts] Registered enchantment " + data.keyString);
                })
        );

        Set<TagEntry<Enchantment>> set = Set.of(TagEntry.valueEntry(key));
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
                    event.registrar().addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, set);
                    event.registrar().addToTag(EnchantmentTagKeys.TRADEABLE, set);
                    event.registrar().addToTag(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, set);
                    System.out.println("[Ghasts] Added enchantment to registries");
                })
        );
    }

    /**
     * Reads config.yml data into an EnchantmentData record
     * @param path The path to the plugin folder
     * @return A populated EnchantmentData
     */
    @Contract("_ -> new")
    private @NotNull EnchantmentData getData(@NotNull Path path) {
        File file = path.resolve("config.yml").toFile();
        if (file.exists()) {
            System.out.println("[Ghasts] config.yml found.");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            return new EnchantmentData(
                    "pedestria:" + config.getString("enchantment.name", "velocity"),
                    config.getString("enchantment.description", "Velocity"),
                    getMaxLevel(config),
                    config.getInt("enchantment.weight"),
                    EnchantmentCost.of(config.getInt("enchantment.cost.minimum", 1), config.getInt("enchantment.cost.minimum-modifier", 11)),
                    EnchantmentCost.of(config.getInt("enchantment.cost.maximum", 15), config.getInt("enchantment.cost.maximum-modifier", 10)),
                    config.getInt("enchantment.cost.anvil"),
                    config.getBoolean("enchantment.enable", true)
            );
        } else {
            System.out.println("[Ghasts] config.yml not found, using default values.");
            return new EnchantmentData(
                    "pedestria:velocity",
                    "Velocity",
                    3,
                    3,
                    EnchantmentCost.of(1, 11),
                    EnchantmentCost.of(15, 10),
                    3,
                    true
            );
        }
    }

    private record EnchantmentData(@NotNull String keyString, @NotNull String description, int maxLevel, int weight, EnchantmentCost minCost, EnchantmentCost maxCost, int anvilCost, boolean isEnabled) {

        @Override
        public String keyString() {
            return keyString.isBlank() ? "velocity" : keyString;
        }

        @Override
        public String description() {
            return description.isBlank() ? "Velocity" : description;
        }

        @Override
        public int weight() {
            return weight > 0 && weight <= 1024 ? weight : 3;
        }
    }
}
