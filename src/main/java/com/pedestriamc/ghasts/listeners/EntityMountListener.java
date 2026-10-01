package com.pedestriamc.ghasts.listeners;

import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.enchantment.SpeedManager;
import com.pedestriamc.ghasts.messages.Message;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityMountListener implements Listener {

  private final SpeedManager manager;
  private final Ghasts plugin;

  public EntityMountListener(@NotNull Ghasts plugin) {
    this.plugin = plugin;
    manager = plugin.getSpeedManager();
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onEvent(@NotNull EntityMountEvent event) {
    Entity entity = event.getMount();
    if (!(entity instanceof HappyGhast ghast)) {
      return;
    }

    if (plugin.isWorldDisabled(ghast.getWorld().getName())) {
      event.setCancelled(true);
      if (event.getEntity() instanceof Player player) {
        plugin.getMessenger().sendMessage(player, Message.WORLD_DISABLED);
      }
      return;
    }

    int level = getEnchantmentLevelIfPresent(ghast.getEquipment().getItem(EquipmentSlot.BODY));
    AttributeInstance attribute = ghast.getAttribute(Attribute.FLYING_SPEED);
    if (attribute != null) {
      attribute.setBaseValue(manager.getSpeed(level));
    }
  }

  private int getEnchantmentLevelIfPresent(@NotNull ItemStack itemStack) {
    Enchantment enchantment = manager.getEnchantment();
    if (enchantment != null && itemStack.getEnchantments().containsKey(enchantment)) {
      return itemStack.getEnchantments().get(enchantment);
    }

    return -1;
  }
}
