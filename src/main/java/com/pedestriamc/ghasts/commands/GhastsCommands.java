package com.pedestriamc.ghasts.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.messages.Message;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public final class GhastsCommands {
  private final Ghasts plugin;

  public GhastsCommands(Ghasts plugin) {
    this.plugin = plugin;
  }

  public void register(Commands commands) {
    int maxLevel = plugin.getSettings().speeds().maxLevel();
    commands.register(
        Commands.literal("ghasts")
            .executes(
                context ->
                    send(context.getSource().getSender(), Message.VERSION, versionPlaceholders()))
            .then(
                Commands.literal("version")
                    .executes(
                        context ->
                            send(
                                context.getSource().getSender(),
                                Message.VERSION,
                                versionPlaceholders())))
            .then(
                Commands.literal("help")
                    .executes(
                        context -> send(context.getSource().getSender(), Message.HELP, Map.of())))
            .then(
                Commands.literal("reload")
                    .requires(source -> source.getSender().hasPermission("ghasts.reload"))
                    .executes(
                        context ->
                            send(
                                context.getSource().getSender(),
                                Message.RELOAD_UNSUPPORTED,
                                Map.of())))
            .then(
                Commands.literal("enchant")
                    .requires(source -> source.getSender().hasPermission("ghasts.enchant"))
                    .executes(context -> enchant(context.getSource().getSender(), 1))
                    .then(
                        Commands.argument("level", IntegerArgumentType.integer(1, maxLevel))
                            .executes(
                                context ->
                                    enchant(
                                        context.getSource().getSender(),
                                        IntegerArgumentType.getInteger(context, "level")))))
            .build(),
        List.of());
  }

  private int enchant(CommandSender sender, int level) {
    if (!sender.hasPermission("ghasts.enchant"))
      return send(sender, Message.NO_PERMISSION, Map.of());
    if (!(sender instanceof Player player))
      return send(sender, Message.CONSOLE_PLAYER_ONLY, Map.of());
    var enchantment = plugin.getSpeedManager().getEnchantment();
    if (enchantment == null) return send(sender, Message.ENCHANTMENT_DISABLED, Map.of());
    if (level < 1 || level > enchantment.getMaxLevel())
      return send(
          sender,
          Message.INVALID_LEVEL,
          Map.of("{max}", Component.text(enchantment.getMaxLevel())));
    ItemStack original = player.getInventory().getItemInMainHand();
    boolean book =
        original.getType() == Material.BOOK || original.getType() == Material.ENCHANTED_BOOK;
    if (!book && !enchantment.canEnchantItem(original))
      return send(sender, Message.INVALID_ITEM, Map.of());
    ItemStack item = original.clone();
    if (book) {
      item = item.withType(Material.ENCHANTED_BOOK);
      EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
      meta.addStoredEnchant(enchantment, level, false);
      item.setItemMeta(meta);
    } else {
      item.addEnchantment(enchantment, level);
    }
    player.getInventory().setItemInMainHand(item);
    plugin
        .getLogger()
        .info(
            "Enchanted player="
                + player.getUniqueId()
                + " item="
                + item.getType()
                + " level="
                + level);
    return send(
        sender,
        Message.ENCHANT_SUCCESS,
        Map.of("{item}", item.displayName(), "{enchantment}", enchantment.displayName(level)));
  }

  private int send(CommandSender sender, Message message, Map<String, Component> placeholders) {
    plugin.getMessenger().sendMessage(sender, message, placeholders);
    return 1;
  }

  private Map<String, Component> versionPlaceholders() {
    return Map.of(
        "{build}",
        Component.text(Ghasts.BUILD_ID),
        "{version}",
        Component.text(plugin.getPluginMeta().getVersion()));
  }
}
