package com.pedestriamc.ghasts.enchantment;

import com.pedestriamc.ghasts.config.ConfigFiles;
import com.pedestriamc.ghasts.config.PluginSettings;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.tag.TagEntry;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlotGroup;

public final class EnchantmentBootstrapper implements PluginBootstrap {
  @Override
  public void bootstrap(BootstrapContext context) {
    PluginSettings settings = PluginSettings.from(ConfigFiles.load(context.getDataDirectory()));
    if (!settings.enchantmentEnabled()) {
      context.getLogger().info("Speed enchantment disabled.");
      return;
    }
    var key = EnchantmentKeys.create(Key.key("pedestria", settings.enchantmentName()));
    context
        .getLifecycleManager()
        .registerEventHandler(
            RegistryEvents.ENCHANTMENT
                .compose()
                .newHandler(
                    event -> {
                      event
                          .registry()
                          .register(
                              key,
                              builder ->
                                  builder
                                      .description(Component.text(settings.description()))
                                      .maxLevel(settings.speeds().maxLevel())
                                      .weight(settings.weight())
                                      .minimumCost(
                                          EnchantmentCost.of(
                                              settings.minimumCost(), settings.minimumModifier()))
                                      .maximumCost(
                                          EnchantmentCost.of(
                                              settings.maximumCost(), settings.maximumModifier()))
                                      .anvilCost(settings.anvilCost())
                                      .supportedItems(
                                          event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
                                      .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
                                      .activeSlots(EquipmentSlotGroup.BODY));
                      context.getLogger().info("Registered enchantment=" + key.key());
                    }));
    context
        .getLifecycleManager()
        .registerEventHandler(
            LifecycleEvents.TAGS
                .preFlatten(RegistryKey.ENCHANTMENT)
                .newHandler(
                    event -> {
                      var entries = Set.of(TagEntry.valueEntry(key));
                      event.registrar().addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, entries);
                      event.registrar().addToTag(EnchantmentTagKeys.TRADEABLE, entries);
                      event.registrar().addToTag(EnchantmentTagKeys.ON_TRADED_EQUIPMENT, entries);
                    }));
  }
}
