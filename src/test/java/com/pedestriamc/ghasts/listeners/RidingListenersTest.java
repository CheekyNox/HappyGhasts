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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class RidingListenersTest {
  @Test
  void deniedWorldCancelsEnchantedMountWithoutChangingSpeed() {
    assertMountRestriction(true, true);
  }

  @Test
  void deniedWorldAllowsOrdinaryGhast() {
    assertMountRestriction(true, false);
  }

  @Test
  void allowedWorldAllowsEnchantedGhast() {
    assertMountRestriction(false, true);
  }

  private void assertMountRestriction(boolean disabledWorld, boolean enchanted) {
    Ghasts plugin = mock(Ghasts.class);
    SpeedManager manager = mock(SpeedManager.class);
    when(plugin.getSpeedManager()).thenReturn(manager);
    HappyGhast ghast = mock(HappyGhast.class);
    EntityEquipment equipment = mock(EntityEquipment.class);
    ItemStack harness = mock(ItemStack.class);
    when(ghast.getEquipment()).thenReturn(equipment);
    when(equipment.getItem(EquipmentSlot.BODY)).thenReturn(harness);

    Player player = mock(Player.class);
    World world = mock(World.class);
    when(ghast.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("lobby");
    when(plugin.isWorldDisabled("lobby")).thenReturn(disabledWorld);
    when(plugin.getMessenger()).thenReturn(mock(Messenger.class));
    var event = new EntityMountEvent(player, ghast);
    EntityMountListener listener = spy(new EntityMountListener(plugin));
    doReturn(enchanted ? 1 : -1).when(listener).getEnchantmentLevelIfPresent(harness);
    doNothing().when(listener).applySpeed(any(), anyInt());
    listener.onEvent(event);
    assertEquals(disabledWorld && enchanted, event.isCancelled());
    if (event.isCancelled()) {
      verify(listener, never()).applySpeed(any(), anyInt());
    } else {
      verify(listener).applySpeed(ghast, enchanted ? 1 : -1);
      verifyNoInteractions(plugin.getMessenger());
    }
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
