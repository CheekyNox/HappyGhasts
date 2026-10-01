package com.pedestriamc.ghasts.listeners;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.enchantment.SpeedManager;
import com.pedestriamc.ghasts.messages.Messenger;
import java.util.List;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.junit.jupiter.api.Test;

class RidingListenersTest {
  @Test
  void deniedWorldCancelsMountBeforeReadingEquipmentOrChangingSpeed() {
    Ghasts plugin = mock(Ghasts.class);
    HappyGhast ghast = mock(HappyGhast.class);
    Player player = mock(Player.class);
    World world = mock(World.class);
    when(ghast.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("lobby");
    when(plugin.isWorldDisabled("lobby")).thenReturn(true);
    when(plugin.getMessenger()).thenReturn(mock(Messenger.class));
    var event = new EntityMountEvent(player, ghast);
    new EntityMountListener(plugin).onEvent(event);
    assertTrue(event.isCancelled());
    verify(ghast, never()).getEquipment();
    verify(ghast, never()).getAttribute(any());
  }

  @Test
  void dismountKeepsSpeedWhileAnotherPlayerRemains() {
    Ghasts plugin = mock(Ghasts.class);
    SpeedManager manager = mock(SpeedManager.class);
    when(plugin.getSpeedManager()).thenReturn(manager);
    HappyGhast ghast = mock(HappyGhast.class);
    Player departing = mock(Player.class);
    Player remaining = mock(Player.class);
    when(departing.getUniqueId()).thenReturn(UUID.randomUUID());
    when(remaining.getUniqueId()).thenReturn(UUID.randomUUID());
    when(ghast.getPassengers()).thenReturn(List.of(departing, remaining));
    new EntityDismountListener(plugin).onEvent(new EntityDismountEvent(departing, ghast));
    verify(ghast, never()).getAttribute(any());
  }

  @Test
  void ridingHandlersRespectCancelledEvents() throws Exception {
    assertTrue(
        EntityMountListener.class
            .getDeclaredMethod("onEvent", EntityMountEvent.class)
            .getAnnotation(EventHandler.class)
            .ignoreCancelled());
    assertTrue(
        EntityDismountListener.class
            .getDeclaredMethod("onEvent", EntityDismountEvent.class)
            .getAnnotation(EventHandler.class)
            .ignoreCancelled());
  }
}
