package com.pedestriamc.ghasts;

import com.pedestriamc.ghasts.commands.main.GhastsBukkitCommand;
import com.pedestriamc.ghasts.enchantment.SpeedManager;
import com.pedestriamc.ghasts.listeners.EntityDismountListener;
import com.pedestriamc.ghasts.listeners.EntityMountListener;
import com.pedestriamc.ghasts.listeners.PrepareItemEnchantListener;
import com.pedestriamc.ghasts.messages.Messenger;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class Ghasts extends JavaPlugin {

    public static final String VERSION = "1.3";

    public static final int METRICS_ID = 26530;

    private SpeedManager manager;
    private Messenger messenger;
    private boolean enchantmentEnabled;

    private Ghasts() {}

    @Override
    public void onLoad() {
        getLogger().info("Loading...");
        updateConfig();
        saveDefaultConfig();
        enchantmentEnabled = getConfig().getBoolean("enchantment.enable");
        manager = new SpeedManager(this);
        messenger = new Messenger(getConfig());
    }

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, METRICS_ID);
        metrics.addCustomChart(new SimplePie("using_enchantment", () -> String.valueOf(enchantmentEnabled)));
        metrics.addCustomChart(new SimplePie("default_speed", () -> String.valueOf(manager.getDefaultSpeed())));
        registerListener(new EntityDismountListener(this));
        registerListener(new EntityMountListener(this));
        if (enchantmentEnabled) {
            registerListener(new PrepareItemEnchantListener(this));
        }

        registerGhastsCommand();
        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getLogger().info("Disabled.");
    }

    @NotNull
    public SpeedManager getSpeedManager() {
        return manager;
    }

    @NotNull
    public Messenger getMessenger() {
        return messenger;
    }

    public boolean isEnchantmentEnabled() {
        return enchantmentEnabled;
    }

    private void registerListener(@NotNull Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    // https://forums.papermc.io/threads/cant-register-command.802/
    private void registerGhastsCommand() {
        try {
            getServer().getCommandMap().register("ghasts", new GhastsBukkitCommand(this));
        } catch(Exception e) {
            getLogger().warning("Failed to register /ghasts command.");
            getLogger().warning(e.getMessage());
        }
    }

    private void updateConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (file.exists()) {
            try {
                ConfigUpdater.update(this, "config.yml", file);
            } catch(IOException e) {
                getLogger().warning("Failed to update config.yml");
            }
        }
    }

}
