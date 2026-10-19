# Measurement

<img src="src/main/resources/assets/measurement/icon.png" width="96" align="right" alt="icon">

**English** | [简体中文](README.zh-CN.md)

A client-side [Fabric](https://fabricmc.net/) mod for Minecraft 1.21 up to the latest release that measures an area.
Hold a wooden shovel, click two blocks, and the area is drawn in the world with its size written on the edges.
`/measurement` prints its length, width, height, volume and volume without air in chat.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) for your
   Minecraft version.
2. Download the Measurement jar for your version from
   [Releases](https://github.com/BillBlueInnit/Measurement/releases) and put it into `mods`.

Only the client needs the mod. It works in singleplayer and on any server, including servers without mods.

## Usage

Hold a **wooden shovel**:

- **Left click** a block to set corner 1 (blue marker).
- **Right click** a block to set corner 2 (pink marker).

While the mod is on, the shovel only selects: it doesn't break blocks or make paths, and nothing is sent to the
server. The area is drawn as an orange box with its length, height and width on the edges closest to you. Parts of the
box behind blocks are drawn faintly, so you can always see the whole box and still tell what is in front.

| Command | What it does |
| --- | --- |
| `/measurement` | Prints the corners, length (X), width (Z), height (Y), volume and volume without air |
| `/measurement on` | Turns the shovel selection and the box on (default) |
| `/measurement off` | Turns them off; the wooden shovel works normally again |
| `/measurement clear` | Removes the selection |

On/off is remembered in `config/measurement.properties`. Volume without air counts every block that isn't air
(water and other fluids count as blocks). The client only knows loaded chunks, so blocks in unloaded chunks are not
counted and the message says how many were skipped.

## Shaders

The box and its labels are drawn with the render types Minecraft uses for name tags, so they work with
[Iris](https://modrinth.com/mod/iris) and shader packs. Tested with Iris and Complementary Reimagined.

## Supported versions

Each Minecraft version family lives on its own git branch. `main` tracks the newest release.

| Branch | Minecraft | Fabric API | Java |
| --- | --- | --- | --- |
| `main` / `26.3` | 26.3 | 0.163.0+26.3 | 25 |
| `26.2` | 26.2 | 0.161.0+26.2 | 25 |
| `26.1` | 26.1 - 26.1.2 | 0.155.3+26.1.2 | 25 |
| `1.21.11` | 1.21.11 | 0.141.6+1.21.11 | 21 |
| `1.21.10` | 1.21.9 - 1.21.10 | 0.138.4+1.21.10 | 21 |
| `1.21.8` | 1.21.6 - 1.21.8 | 0.136.1+1.21.8 | 21 |
| `1.21.5` | 1.21.5 | 0.128.2+1.21.5 | 21 |
| `1.21.4` | 1.21.4 | 0.119.4+1.21.4 | 21 |
| `1.21.3` | 1.21.2 - 1.21.3 | 0.114.1+1.21.3 | 21 |
| `1.21.1` | 1.21 - 1.21.1 | 0.116.17+1.21.1 | 21 |

Fixes go to the oldest branch they apply to and are merged forward.

## Building

Requires JDK 21+ (JDK 25 for the 26.x branches).

```
./gradlew build
```

The jar is written to `build/libs/`.

## License

MIT
