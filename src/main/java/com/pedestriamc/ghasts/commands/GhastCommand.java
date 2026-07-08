package com.pedestriamc.ghasts.commands;

import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.messages.Message;
import com.pedestriamc.ghasts.messages.Messenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

@SuppressWarnings("all")
@Deprecated
public class GhastCommand implements CommandExecutor {

    private final Ghasts plugin;
    private final Messenger messenger;

    private final Component helpMessage;
    private final boolean enchantmentEnabled;

    public GhastCommand(@NotNull Ghasts plugin) {
        this.plugin = plugin;
        messenger = plugin.getMessenger();
        helpMessage = MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.help"));
        enchantmentEnabled = plugin.getConfig().getBoolean("enchantment.enabled");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> oneArg(sender, args);
            case 2 -> twoArgs(sender, args);
            default -> sendVersionMessage(sender);
        }

        return true;
    }

    private void sendVersionMessage(@NotNull CommandSender sender) {
        messenger.sendMessage(sender, Message.PLUGIN_VERSION, Map.of("{version}", Component.text(Ghasts.VERSION)));
    }

    // Expected args: help, reload, givebook, version
    private void oneArg(@NotNull CommandSender sender, @NotNull String[] args) {
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> {
                if (hasPermission(sender, "ghasts.manage")) {
                    sender.sendMessage(helpMessage);
                } else {
                    messenger.sendMessage(sender, Message.NO_PERMISSION);
                }
            }
            case "reload" -> {
                if (hasPermission(sender, "ghasts.reload")) {
                    messenger.sendMessage(sender, Message.RELOAD_UNSUPPORTED);
                } else {
                    messenger.sendMessage(sender, Message.NO_PERMISSION);
                }
            }
            case "givebook" -> {
                if (hasPermission(sender, "ghasts.givebook")) {
                    if (!(sender instanceof Player player)) {
                        messenger.sendMessage(sender, Message.CONSOLE_MUST_DEFINE_PLAYER);
                        return;
                    }

                    player.give(createBook(1, 1));
                    Map<String, Component> map = Map.of("{quantity}", Component.text(1), "{player}", Component.text(sender.getName()));
                    messenger.sendMessage(sender, Message.BOOK_GIVEN, map);
                } else {
                    messenger.sendMessage(sender, Message.NO_PERMISSION);
                }
            }
            default -> sendVersionMessage(sender);
        }
    }

    private void twoArgs(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("givebook") && hasPermission(sender, "ghasts.givebook")) {

        }
    }

    private @NotNull ItemStack createBook(int level, int amount) {
        ItemStack stack = ItemStack.of(Material.ENCHANTED_BOOK).asQuantity(amount);
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(plugin.getSpeedManager().getEnchantment(), level, true);
        stack.setItemMeta(meta);

        return stack;
    }

    private boolean hasPermission(@NotNull Permissible permissible, @NotNull String permission) {
        return permissible.isOp() ||
                permissible.hasPermission("*") ||
                permissible.hasPermission("ghasts.*") ||
                permissible.hasPermission(permission);
    }

}
