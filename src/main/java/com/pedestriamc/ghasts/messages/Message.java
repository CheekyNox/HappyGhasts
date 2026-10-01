package com.pedestriamc.ghasts.messages;

public enum Message {
  WORLD_DISABLED("world-disabled"),
  RELOAD_UNSUPPORTED("reload-unsupported"),
  CONSOLE_PLAYER_ONLY("console-player-only"),
  NO_PERMISSION("no-perms"),
  HELP("help"),
  INVALID_ITEM("invalid-item"),
  INVALID_LEVEL("invalid-level"),
  ENCHANT_SUCCESS("enchant-success"),
  VERSION("version"),
  ENCHANTMENT_DISABLED("enchantment-disabled");

  private final String key;

  Message(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
