package com.pedestriamc.ghasts.listeners;

import com.pedestriamc.ghasts.config.PluginSettings;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public final class PrepareItemEnchantListener implements Listener {
  private final int minimum;
  private final int maximum;

  public PrepareItemEnchantListener(PluginSettings settings) {
    minimum = settings.minimumCost();
    maximum = settings.maximumEnchantability();
  }

  @EventHandler
  void onEvent(PrepareItemEnchantEvent event) {
    if (!event.getItem().getType().name().endsWith("_HARNESS")) return;
    var meta = event.getItem().getItemMeta();
    if (meta.hasEnchantable()) return;
    // Harnesses are initially non-enchantable, so the preparation event may be cancelled.
    meta.setEnchantable(ThreadLocalRandom.current().nextInt(minimum, maximum + 1));
    event.getItem().setItemMeta(meta);
  }
}
