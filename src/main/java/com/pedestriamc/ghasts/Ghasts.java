package com.pedestriamc.ghasts;

import com.pedestriamc.ghasts.commands.GhastsCommands;
import com.pedestriamc.ghasts.config.ConfigFiles;
import com.pedestriamc.ghasts.config.PluginSettings;
import com.pedestriamc.ghasts.enchantment.SpeedManager;
import com.pedestriamc.ghasts.listeners.EntityDismountListener;
import com.pedestriamc.ghasts.listeners.EntityMountListener;
import com.pedestriamc.ghasts.listeners.PrepareItemEnchantListener;
import com.pedestriamc.ghasts.messages.Messenger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Objects;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Ghasts extends JavaPlugin {
  public static final long BUILD_ID = 855403833L;
  public static final int METRICS_ID = 26530;
  private @Nullable SpeedManager manager;
  private @Nullable Messenger messenger;
  private @Nullable PluginSettings settings;

  @Override
  public void onLoad() {
    settings = PluginSettings.from(ConfigFiles.load(getDataPath()));
    manager = new SpeedManager(settings);
    messenger = new Messenger(ConfigFiles.loadMessages(getDataPath()));
  }

  @Override
  public void onEnable() {
    PluginSettings settings = getSettings();
    SpeedManager manager = getSpeedManager();
    if (settings.metrics()) {
      Metrics metrics = new Metrics(this, METRICS_ID);
      metrics.addCustomChart(
          new SimplePie("using_enchantment", () -> String.valueOf(settings.enchantmentEnabled())));
      metrics.addCustomChart(
          new SimplePie("default_speed", () -> String.valueOf(manager.getDefaultSpeed())));
    }
    var events = getServer().getPluginManager();
    events.registerEvents(new EntityDismountListener(this), this);
    events.registerEvents(new EntityMountListener(this), this);
    if (settings.enchantmentEnabled())
      events.registerEvents(new PrepareItemEnchantListener(settings), this);
    getLifecycleManager()
        .registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> new GhastsCommands(this).register(event.registrar()));
    getLogger().info("Enabled build=" + BUILD_ID + " version=" + getPluginMeta().getVersion());
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
    getLogger().info("Disabled.");
  }

  public SpeedManager getSpeedManager() {
    return Objects.requireNonNull(manager, "Speed settings are unavailable before onLoad");
  }

  public Messenger getMessenger() {
    return Objects.requireNonNull(messenger, "Messages are unavailable before onLoad");
  }

  public PluginSettings getSettings() {
    return Objects.requireNonNull(settings, "Settings are unavailable before onLoad");
  }

  public boolean isWorldDisabled(String worldName) {
    return getSettings().disabledWorlds().contains(worldName);
  }
}
