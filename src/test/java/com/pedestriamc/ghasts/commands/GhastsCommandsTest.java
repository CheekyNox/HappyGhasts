package com.pedestriamc.ghasts.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.pedestriamc.ghasts.Ghasts;
import com.pedestriamc.ghasts.config.ConfigFiles;
import com.pedestriamc.ghasts.config.PluginSettings;
import com.pedestriamc.ghasts.messages.Message;
import com.pedestriamc.ghasts.messages.Messenger;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GhastsCommandsTest {
  @TempDir Path directory;

  @Test
  void enchantAndReloadBranchesRequirePermission() {
    var plugin = mock(Ghasts.class);
    when(plugin.getSettings()).thenReturn(PluginSettings.from(ConfigFiles.load(directory)));
    var root = register(plugin);
    var source = mock(CommandSourceStack.class);
    var sender = mock(CommandSender.class);
    when(source.getSender()).thenReturn(sender);
    assertFalse(root.getChild("enchant").canUse(source));
    assertFalse(root.getChild("reload").canUse(source));
    when(sender.hasPermission("ghasts.enchant")).thenReturn(true);
    assertTrue(root.getChild("enchant").canUse(source));
    assertFalse(root.getChild("reload").canUse(source));
  }

  @Test
  void consoleEnchantGetsConfigurablePlayerOnlyMessage() throws Exception {
    var plugin = mock(Ghasts.class);
    when(plugin.getSettings()).thenReturn(PluginSettings.from(ConfigFiles.load(directory)));
    var messenger = mock(Messenger.class);
    when(plugin.getMessenger()).thenReturn(messenger);
    var sender = mock(CommandSender.class);
    when(sender.hasPermission("ghasts.enchant")).thenReturn(true);
    var source = mock(CommandSourceStack.class);
    when(source.getSender()).thenReturn(sender);
    var dispatcher = new CommandDispatcher<CommandSourceStack>();
    dispatcher.getRoot().addChild(register(plugin));
    dispatcher.execute("ghasts enchant", source);
    verify(messenger).sendMessage(eq(sender), eq(Message.CONSOLE_PLAYER_ONLY), anyMap());
    verify(plugin, never()).getSpeedManager();
  }

  @Test
  void brigadierRejectsOutOfRangeLevelBeforeExecuting() {
    var plugin = mock(Ghasts.class);
    when(plugin.getSettings()).thenReturn(PluginSettings.from(ConfigFiles.load(directory)));
    var sender = mock(CommandSender.class);
    when(sender.hasPermission("ghasts.enchant")).thenReturn(true);
    var source = mock(CommandSourceStack.class);
    when(source.getSender()).thenReturn(sender);
    var dispatcher = new CommandDispatcher<CommandSourceStack>();
    dispatcher.getRoot().addChild(register(plugin));
    assertThrows(
        com.mojang.brigadier.exceptions.CommandSyntaxException.class,
        () -> dispatcher.execute("ghasts enchant 0", source));
    assertThrows(
        com.mojang.brigadier.exceptions.CommandSyntaxException.class,
        () -> dispatcher.execute("ghasts enchant 4", source));
    verify(plugin, never()).getSpeedManager();
  }

  private LiteralCommandNode<CommandSourceStack> register(Ghasts plugin) {
    var result = new AtomicReference<LiteralCommandNode<CommandSourceStack>>();
    var registrar = mock(Commands.class);
    when(registrar.register(any(), anyCollection()))
        .thenAnswer(
            invocation -> {
              result.set(invocation.getArgument(0));
              return Set.of("ghasts");
            });
    new GhastsCommands(plugin).register(registrar);
    return java.util.Objects.requireNonNull(result.get());
  }
}
