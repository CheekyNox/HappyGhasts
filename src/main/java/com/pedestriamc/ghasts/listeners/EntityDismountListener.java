package com.pedestriamc.ghasts.listeners;

import com.pedestriamc.ghasts.Ghasts;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.HappyGhast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDismountListener implements Listener {

    private final double idleSpeed;

    public EntityDismountListener(@NotNull Ghasts plugin) {
        idleSpeed = plugin.getSpeedManager().getIdleSpeed();
    }

    @EventHandler
    void onEvent(@NotNull EntityDismountEvent event) {
        if (event.getDismounted() instanceof HappyGhast ghast) {
            AttributeInstance instance = ghast.getAttribute(Attribute.FLYING_SPEED);
            if (instance != null) {
                instance.setBaseValue(idleSpeed);
            }
        }
    }
}
