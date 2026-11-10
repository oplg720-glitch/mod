# Advanced Inventory — Fabric 26.1.2 (Singleplayer only)

A client-only Fabric mod project for Minecraft Java Edition **26.1.2**. Type `/inv` in a Singleplayer world to open an item browser. Select an item, enter a quantity, optionally choose supported utility/defensive enchantments, then drop the generated stacks into the world. Items are not inserted into the player's inventory.

> **Scope:** this project is intentionally Singleplayer-only. It checks for an integrated server before generating anything and does not register a server-side command or server entrypoint. Multiplayer/LAN use is not supported. Combat-focused items and combat enchantments are excluded from the browser.

## Included in this first implementation

- Client command `/inv`.
- Item catalogue populated from the active item registry, including items registered by other installed mods.
- Search by item ID or displayed name.
- Categories: all, blocks, food, tools, redstone, miscellaneous. These category filters use simple ID heuristics and are not the official Creative tab taxonomy.
- Quantity input with a per-request limit of **4,096 items**.
- Stack splitting using each item's maximum stack size.
- Item entities spawned around the player by the integrated server.
- Utility/defensive enchantments configurable from level **1–255**.
- A limit of **128 spawned item entities per request** to reduce lag. Some unstackable items therefore cannot use the full 4,096 quantity in one request.
- GitHub Codespaces configuration and GitHub Actions build workflow.

## Enchantment list

`unbreaking`, `mending`, `efficiency`, `silk_touch`, `fortune`, `aqua_affinity`, `respiration`, `feather_falling`, `depth_strider`, `frost_walker`, `soul_speed`, `swift_sneak`, `luck_of_the_sea`, `lure`, `protection`, `fire_protection`, `blast_protection`, `projectile_protection`.

The selected levels are written into the item's `DataComponents.ENCHANTMENTS` component. The code does not run the normal enchanting-table compatibility checks; the allowlist above is the supported set.

## Target toolchain

- Minecraft: `26.1.2`
- Java: `25`
- Fabric Loader: `0.19.0` or newer
- Fabric API: `0.155.3+26.1.2`
- Fabric Loom: `1.16.1`
- Gradle: `9.4.1`

Minecraft 26.1+ uses the non-remapped Loom plugin ID `net.fabricmc.fabric-loom`; do not add old Yarn or Mojang mapping configuration.

## Build in GitHub Codespaces (no local Java/Gradle install)

1. Create an empty GitHub repository.
2. Upload the contents of this project to the repository root (not the enclosing ZIP folder).
3. Open **Code → Codespaces → Create codespace on main**.
4. Codespaces reads `.devcontainer/devcontainer.json` and installs Java 25 and Gradle 9.4.1.
5. Open the terminal and run:

   ```bash
   java -version
   gradle --version
   gradle --no-daemon clean build
   ```

6. The mod JAR should be generated in `build/libs/`. For the GitHub Actions route, open the repository's **Actions** tab, select the latest **Build Fabric mod** run, and download the `advanced-inventory-fabric-26.1.2` artifact if the workflow succeeds.

## Install in Minecraft

1. Use a Minecraft 26.1.2 Fabric profile.
2. Install a compatible Fabric Loader and Fabric API.
3. Put the built mod JAR in the profile's `mods` folder.
4. Launch the game and open a Singleplayer world.
5. Enter `/inv` in chat.

## Important implementation notes

- This is a **source project**, not a prebuilt JAR. The current authoring container only has Java 21 and cannot run a Minecraft 26.1.2 build requiring Java 25. The code has therefore **not been compile-tested here**. Use Codespaces or GitHub Actions to perform the actual build and inspect any compiler output.
- Minecraft 26.1.2 changed several APIs compared with 1.21.x. The source targets the 26.1.2 naming/API family (for example, `GuiGraphicsExtractor`, `ClientCommands`, and `Identifier`).
- The item browser uses registry enumeration and ID-based category heuristics. Some modded items may be categorized as Miscellaneous.
- Quantity is capped to protect the world from entity overload. The service validates the integrated server and player again before spawning entities.
- The mod intentionally filters out combat-oriented items and excludes attack-focused enchantments.

## Project layout

```text
.
├── .devcontainer/devcontainer.json
├── .github/workflows/build.yml
├── src/main/java/com/sua/advancedinventory/
│   ├── AdvancedInventoryClient.java
│   ├── catalog/ItemCatalog.java
│   ├── client/InventoryScreen.java
│   ├── client/EnchantmentScreen.java
│   └── service/ItemDropService.java
├── src/main/resources/fabric.mod.json
├── build.gradle
├── gradle.properties
└── settings.gradle
```
