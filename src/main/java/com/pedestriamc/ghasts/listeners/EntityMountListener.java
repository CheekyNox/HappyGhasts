package com.pedestriamc.ghasts.listeners;

import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.enchantment.SpeedManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityMountListener implements Listener {

    private final SpeedManager manager;

    public EntityMountListener(@NotNull Ghasts plugin) {
        manager = plugin.getSpeedManager();
    }

    @EventHandler
    void onEvent(@NotNull EntityMountEvent event) {
        Entity entity = event.getMount();
        if (!(entity instanceof HappyGhast ghast)) {
            return;
        }

        int level = getEnchantmentLevelIfPresent(ghast.getEquipment().getItem(EquipmentSlot.BODY));
        AttributeInstance attribute = ghast.getAttribute(Attribute.FLYING_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(manager.getSpeed(level));
        }
    }

    /**
     * Provides the harness's enchantment level if present.
     * @return The harness's enchantment level, otherwise -1.
     */
    private int getEnchantmentLevelIfPresent(@NotNull ItemStack itemStack) {
        Enchantment enchantment = manager.getEnchantment();
        if (enchantment != null && itemStack.getEnchantments().containsKey(enchantment)) {
            return itemStack.getEnchantments().get(enchantment);
        }

        return -1;
    }
}
