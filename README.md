# LevelZ Inventory Weight

**LevelZ Inventory Weight** is an add-on for **MT Inventory Weight** and **LevelZ**.

It connects LevelZ progression with inventory weight capacity, allowing a player's **overall LevelZ character level** and/or a configured **LevelZ skill** to increase maximum inventory weight.

This mod is **not standalone**.

## Required Dependencies

This add-on requires:

- [LevelZ](https://modrinth.com/mod/levelz)
- [MT Inventory Weight](https://modrinth.com/mod/inventory-weight)
- [Fabric API](https://modrinth.com/mod/fabric-api)

Both **LevelZ** and **MT Inventory Weight** must be installed for this add-on to work.

Recommended versions:

- **Minecraft:** `1.21.1`
- **LevelZ:** `2.0.10+1.21.1` or newer
- **MT Inventory Weight:** `2.0.1-1.21` or newer

## Features

- Adds integration between **LevelZ** and **MT Inventory Weight**
- Uses the player's **overall LevelZ level** to modify max inventory weight
- Uses a configurable **LevelZ skill** to modify max inventory weight
- Includes support for a custom `carrying` skill
- Server-side synced config
- Supports both flat and multiplier-based capacity scaling
- Can optionally hide the configured skill from the LevelZ skill screen
- Can dynamically create the configured LevelZ skill if it does not already exist
- Supports two capacity modes:
  - `EVENT_MODIFIER`
  - `LEVELZ_ATTRIBUTE`

## Documentation

Full documentation for this add-on is available here:
https://megatrex4.github.io/inventory-weight/guide/addons/levelz
