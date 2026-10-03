# Changelog

## 1.4.0

- Target Paper 26.3.build.140-beta and Java 25.
- Forbid mounting Happy Ghasts with the speed enchantment through the configurable disabled-worlds list; ordinary Happy Ghasts remain usable.
- Keep riding speed while another player remains aboard; respect cancelled riding events.
- Validate speed, enchantment keys, levels, world names and XP costs before registration.
- Migrate legacy messages into messages.yml with config.yml.v1.bak backup.
- Preserve existing messages.yml and user-defined enchantment levels.
- Register commands through Paper Brigadier with permission-aware completion.
- Support stored enchantments on books in /ghasts enchant.
- Remove obsolete commands, plugin.yml and command/config updater dependencies.
- Add regression tests, Spotless, Error Prone, NullAway and CI vulnerability checks.
- Update Dependency-Check to 13.0.0, use official NVD bulk feeds and add an exact-version OSV check.
- Pin patched compile-only Commons Lang, Log4j API and Plexus Utils dependencies; document separate server-library findings.
