package com.pedestriamc.ghasts.listeners;

import com.pedestriamc.ghasts.Ghasts;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public final class EntityDismountListener implements Listener {
  private final double idleSpeed;

  public EntityDismountListener(Ghasts plugin) {
    idleSpeed = plugin.getSpeedManager().getIdleSpeed();
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onEvent(EntityDismountEvent event) {
    if (!(event.getDismounted() instanceof HappyGhast ghast)) return;
    // The departing entity is still in getPassengers() when this event fires.
    if (ghast.getPassengers().stream()
        .anyMatch(
            passenger ->
                passenger instanceof Player
                    && !passenger.getUniqueId().equals(event.getEntity().getUniqueId()))) return;
    var attribute = ghast.getAttribute(Attribute.FLYING_SPEED);
    if (attribute != null) attribute.setBaseValue(idleSpeed);
  }
}
