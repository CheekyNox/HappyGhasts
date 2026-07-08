package com.pedestriamc.ghasts.commands.main;

import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.messages.Message;
import com.pedestriamc.ghasts.messages.Messenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wiicart.commands.command.CartCommandExecutor;
import net.wiicart.commands.command.CommandData;
import net.wiicart.commands.permission.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class EnchantCommand implements CartCommandExecutor {

    private final Messenger messenger;

    // placeholders not applied yet obv
    private final Component finalMessage;

    private final Enchantment enchantment;
    private final int maxLevel;

    EnchantCommand(@NotNull Ghasts ghasts) {
        enchantment = ghasts.getSpeedManager().getEnchantment();

        int maxEnchantLevel = 0;
        if (enchantment != null) {
            maxEnchantLevel = enchantment.getMaxLevel();
        }
        maxLevel = maxEnchantLevel;

        messenger = ghasts.getMessenger();

        finalMessage = loadFinalMessageRaw(ghasts);
    }

    @Override
    public void onCommand(@NotNull CommandData data) {
        Player player = getPlayerAndNotify(data.sender());
        if (player == null) {
            return;
        }

        if (doesNotHavePermission(player)) {
            return;
        }

        if (data.args().length > 1) {
            messenger.sendMessage(player, Message.TOO_MANY_ARGS);
            return;
        }

        ItemStack item = getBookInMainHandAndNotify(player);
        if (item == null) {
            return;
        }

        int level = getEnchantmentLevel(player, data.args());
        if (level < 1) {
            return;
        }

        item.addEnchantment(enchantment, level);
        player.sendMessage(getFinalMessage(item, level));

    }

    @Nullable
    private Player getPlayerAndNotify(@NotNull CommandSender sender) {
        if (sender instanceof Player p) {
            return p;
        } else {
            sender.sendMessage("You must be a player to use this command!");
            return null;
        }
    }

    private boolean doesNotHavePermission(@NotNull CommandSender sender) {
        if (!Permissions.anyOfOrAdmin(sender, "ghasts.*", "ghasts.enchant")) {
            messenger.sendMessage(sender, Message.NO_PERMISSION);
            return true;
        }

        return false;
    }

    @Nullable
    private ItemStack getBookInMainHandAndNotify(@NotNull Player player) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        if (enchantment.canEnchantItem(stack)) {
            return stack;
        } else {
            messenger.sendMessage(player, Message.INVALID_ITEM);
            return null;
        }
    }

    // Returns -1 if invalid
    private int getEnchantmentLevel(@NotNull Player sender, @NotNull String[] args) {
        if (args.length == 0) {
            return 1;
        }

        try {
            int level = Integer.parseInt(args[0]);
            if (level >= 1 && level <= maxLevel) {
                return level;
            }
        } catch(NumberFormatException ignored) {}

        messenger.sendMessage(sender, Message.INVALID_LEVEL, Map.of("{max}", Component.text(maxLevel)));
        return -1;
    }

    @NotNull
    private Component getFinalMessage(@NotNull ItemStack item, int level) {
        return finalMessage
                .replaceText(builder -> builder
                        .matchLiteral("{item}")
                        .replacement(item.displayName())
                )
                .replaceText(builder -> builder
                        .matchLiteral("{enchantment}")
                        .replacement(enchantment.displayName(level))
                )
        ;
    }

    @NotNull
    private Component loadFinalMessageRaw(@NotNull Ghasts ghasts) {
        return messenger.prefix().append(MiniMessage.miniMessage().deserialize(
                ghasts.getConfig().getString(
                        "messages.enchant-success",
                        "<gray>Enchanted {item} with {enchantment}.</gray>"
                )
        ));
    }

}
