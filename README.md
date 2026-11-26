# Coal Tracker

Fabric mod for Minecraft 26.1.2 (Hypixel SkyBlock). Needs Fabric API.

```
Coal Tracker
Collection: 1,250,000 (+25,600)
Collection/h: 102,400
Crafting: Sulphuric Coal (40.0)
Money: <coins>
Money/h: <coins>
```

## Get the jar
GitHub: **Actions** tab, open the newest green **Build** run, download `coaltracker-jar`, unzip it,
and put `coaltracker-1.0.0.jar` (not the `-sources` one) in your `mods` folder.

## Use
- `/coaltracker` opens the menu: craft choice, price mode, own prices, total collection, pause, reset, GUI on/off, move GUI.
- `/coaltracker debug` copies the last 5 sack messages to the clipboard.
- Turn on sack notifications in SkyBlock settings. The GUI appears after the first coal is counted.

## Craft choices
| Choice | Raw coal per item |
|---|---|
| Enchanted Coal | 160 |
| Block of Coal | 9 |
| Enchanted Block of Coal | 25,600 |
| Sulphuric Coal | 640, minus 0.25 Enchanted Sulphur (16 E. Coal + 1 E. Sulphur = 4) |

## How it counts
- Coal comes from the `[Sacks]` chat message: Coal, Block of Coal, Enchanted Coal and Enchanted Block of Coal,
  all converted to raw coal. Removed coal in the same message is subtracted.
- Coal that stays in your inventory is not counted.
- Any coal put into the sacks counts, including bought coal. Pause the tracker before moving such coal.
- Collection/h leaves out breaks over 90 seconds and paused time.
- Money is for all coal of the session at the current price, before bazaar tax.
- Prices refresh every 5 minutes. If an item shows `unknown`, type its price in the menu.
- The Chili Pepper recipe for Sulphuric Coal is not included.

The mod has not been tested in game yet.
