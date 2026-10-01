package com.pedestriamc.ghasts.messages;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

public final class Messenger {
  private final EnumMap<Message, Component> messages = new EnumMap<>(Message.class);
  private final Component prefix;

  public Messenger(FileConfiguration config) {
    prefix = parse(config, "prefix");
    for (Message message : Message.values()) messages.put(message, parse(config, message.key()));
  }

  public void sendMessage(Audience audience, Message message) {
    sendMessage(audience, message, Map.of());
  }

  public void sendMessage(Audience audience, Message message, Map<String, Component> placeholders) {
    Component rendered = Objects.requireNonNull(messages.get(message));
    for (var entry : placeholders.entrySet()) {
      rendered =
          rendered.replaceText(
              builder -> builder.matchLiteral(entry.getKey()).replacement(entry.getValue()));
    }
    audience.sendMessage(prefix.append(rendered));
  }

  private static Component parse(FileConfiguration config, String key) {
    if (!config.isString(key))
      throw new IllegalArgumentException("messages.yml: " + key + " must be a string");
    return MiniMessage.miniMessage().deserialize(config.getString(key));
  }
}
