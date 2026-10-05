# Unlimited XP (Fabric, MC 26.1.2)

Keeps every online player's XP level topped up automatically.

## Commands (op only)
- `/unlimitedxp toggle` — turn the effect on/off
- `/unlimitedxp status` — show current state and target level
- `/unlimitedxp level <value>` — change the target XP level (default 999)

## Building
This uses Gradle 9 + Fabric Loom, targeting the unobfuscated 26.1.2 game
jar directly (Mojang's own names — no Yarn mappings needed anymore).

The Gradle wrapper jar binary isn't included here. Easiest path:

1. Open this folder as a project in **IntelliJ IDEA** (2025.3+, for full
   Java 25 support). It will detect the Gradle build, download the
   wrapper automatically, and let you run `build` from the Gradle panel.

   OR

2. If you have Gradle 9.x installed locally:
   ```
   gradle wrapper --gradle-version 9.4.1
   ./gradlew build
   ```

The built mod jar will be in `build/libs/`.

## Installing
Requires **Fabric Loader** and **Fabric API** for Minecraft 26.1.2.
Drop the built jar into your `.minecraft/mods` (or server `mods`) folder.

## Notes
Minecraft 26.1.2 and its toolchain are very recent (post my knowledge
cutoff), so if `fabric_version` / `loader_version` in `gradle.properties`
have moved on since this was written, grab the current recommended
numbers from https://fabricmc.net/develop and bump them there — nothing
else needs to change.
