# Enchanted Harnesses | Happy Ghast Enhancements
- Customize Happy Ghast default idle speeds
- Customize Happy Ghast default riding speeds
- Fully customizable enchantment that increases Happy Ghast speed

## Configuration
Build target: Paper API `26.3.build.140-beta`, Java 25. Build with `mvn clean verify`.
This runs Error Prone, NullAway, regression tests and Spotless. Use `mvn spotless:apply` to format.
Maven is retained for this existing API-only project; NMS and paperweight are not needed.

Configuration is in `plugins/EnchantedHarnesses/config.yml`; texts are in `messages.yml` in the same directory.
Legacy messages migrate automatically with a `config.yml.v1.bak` backup. Existing `messages.yml` takes precedence.
Invalid YAML, registry keys, levels, speeds or costs stop startup with a configuration error.

## World restrictions

To forbid riding Happy Ghasts with the speed enchantment in specific worlds, add their exact (case-sensitive) names to `disabled-worlds` in `config.yml`. Happy Ghasts without this enchantment can still be ridden:

```yaml
disabled-worlds:
  - world_nether
  - world_the_end
```

An empty list allows riding everywhere. Configure `world-disabled` in `messages.yml` for the denial message.
Restart the server to apply changes; plugin reload is not supported. This restriction applies to mounting,
including additional passengers; it does not remove ghasts or block their spawning or teleportation.

## Getting the Enchantment
The enchantment is available in enchantment tables and the creative inventory.

`/ghasts enchant [level]` enchants the held book or harness and requires `ghasts.enchant` (operators by default).
`/ghasts help` shows commands. `/ghasts version` displays the build identifier.
`ghasts.*` includes administrative permissions. Completion hides branches without permission.
Disable anonymous statistics with `metrics: false`.

## Validation and releases

CI builds on Java 25 and scans dependency vulnerabilities using official NVD bulk feeds, without an API key.
Dependency-Check 13.0.0 supports the current NVD data. CI saves HTML and JSON scan reports, including failed scans.
CI also checks exact compile/provided Maven versions with OSV through `scripts/check-osv.ps1`.
Compile dependency management pins Commons Lang 3.20.0, Log4j API 2.26.1 and Plexus Utils 3.6.1.
These libraries are provided by Paper and are not bundled in the plugin. Updating these build versions
does not replace the server's own libraries. Paper 26.3 build 140 contains Log4j API 2.26.0 and Plexus Utils 3.5.1,
which have published advisories; the reviewed plugin does not call MapMessage JSON logging or archive extraction.
No exploit through this plugin has been established. Server library updates require a separate Paper update.
Run the scan locally with `mvn dependency-check:check`. CI artifacts record the commit in the JAR manifest;
local builds default to `uncommitted`, or accept `-Dbuild.commit=<sha>`.
The CRC32 build identifier identifies the plugin name; version and commit identify the actual revision.
Folia support is not declared.

Before deployment, check on Paper 26.3: clean startup, legacy config upgrade, allowed and denied worlds,
two passengers with one dismounting, the final passenger dismounting, cancelled mount/dismount events,
table enchanting, book/harness enchanting and commands without permission. Local tests do not replace this server check.

## Demo (Click)
[![Watch on YouTube](https://img.youtube.com/vi/Q2lEWF8hUPs/maxresdefault.jpg)](https://www.youtube.com/watch?v=Q2lEWF8hUPs)
