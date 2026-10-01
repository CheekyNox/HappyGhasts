# Local validation

Checked on 2026-10-01 against Paper API 26.3.build.140-beta, Java 25.

- Maven verify: passed; 16 regression tests, Error Prone, NullAway and Spotless passed.
- JAR inventory: obsolete plugin.yml and command/config-updater classes absent; bStats relocated.
- OSV: checked 52 exact compile/provided Maven versions; no advisories returned after dependency fixes.
- Dependency-Check 13.0.0: complete NVD bulk-feed import and final scan passed; no vulnerable dependencies reported.
- Reports: target/osv-report-fixed.json and target/dependency-check-report.html/json.
- Sonatype OSS Index did not run because it requires authentication; NVD and OSV did run.
- Paper 26.3 build 140: official JAR checksum verified; --initSettings recognized EnchantedHarnesses 1.4.0.

The corrected build pins Commons Lang 3.20.0, Log4j API 2.26.1 and Plexus Utils 3.6.1.
They remain provided dependencies, not plugin-shaded libraries. The inspected Paper server itself
contains Log4j API 2.26.0 and Plexus Utils 3.5.1 with published advisories. Changing this plugin's
build does not update them. The plugin does not call the affected MapMessage JSON serialization
or Plexus archive extraction; exploitation through the plugin has not been established.

Full server startup, onEnable and gameplay have not been tested. The isolated server's eula.txt
is false; no EULA acceptance was inferred or written. No existing production server was modified.
The server scenarios listed in README still require an authorized test server with accepted EULA.
