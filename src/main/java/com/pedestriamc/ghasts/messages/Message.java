package com.pedestriamc.ghasts.messages;

import org.jetbrains.annotations.NotNull;

public enum Message {
    BOOK_GIVEN("messages.book-given", "<white>Gave <gray>{quantity}</gray> books to <gray>{player}</gray></white>"),
    PLUGIN_VERSION("messages.plugin-version", "<white>Running Ghasts version <green>{version}</green></white>"),
    RELOAD_UNSUPPORTED("messages.reload-unsupported", "<white>Reloading is not supported, please restart instead.</white>"),
    CONSOLE_MUST_DEFINE_PLAYER("messages.console-insufficient", "<white>Console must define a player to use this command.</white>"),
    NO_PERMISSION("messages.no-perms", "<red>No permission!</red>"),
    HELP("messages.help", "Help message not found"),
    INVALID_ITEM("messages.invalid-item", "<red>You must be holding an un-enchanted book or a harness.</red>"),
    INVALID_LEVEL("messages.invalid-level", "Invalid enchantment level. Must be from 0 to {max}"),
    TOO_MANY_ARGS("messages.too-many-args", "<red>Too many args!</red>"),
    VERSION("messages.version", "<light-gray>Running EnchantedHarnesses version <green>{version}</green></light-gray>"),
    ENCHANTMENT_DISABLED("messages.enchantment-disabled", "The Happy Ghast speed enchantment is currently disabled.");

    private final String key;
    private final String fallback;

    Message(@NotNull String key, @NotNull String fallback) {
        this.key = key;
        this.fallback = fallback;
    }

    public String key() {
        return key;
    }

    public String fallback() {
        return fallback;
    }
}
