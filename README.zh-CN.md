# Measurement 测量

<img src="src/main/resources/assets/measurement/icon.png" width="96" align="right" alt="icon">

[English](README.md) | **简体中文**

一个只需装在客户端的 [Fabric](https://fabricmc.net/) 模组，用来测量区域，支持 Minecraft 1.21 到最新正式版。
手持木铲点击两个方块，选区会画在世界里，边上标出尺寸；`/measurement` 会在聊天栏输出长、宽、高、体积和除去空气的体积。

## 安装

1. 为你的 Minecraft 版本安装 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)。
2. 在 [Releases](https://github.com/BillBlueInnit/Measurement/releases) 下载对应版本的 Measurement jar，放进 `mods` 文件夹。

只有客户端需要安装。单人游戏和任何服务器（包括没有模组的服务器）都能用。

## 使用

手持**木铲**：

- **左键**方块设置角点 1（蓝色标记）。
- **右键**方块设置角点 2（粉色标记）。

模组开启时，木铲只用来选区：不会挖掉方块，也不会铲出土径，也不会向服务器发送任何操作。
选区画成一个橙色的框，长、高、宽标在离你最近的边上。被方块挡住的部分会淡淡地显示出来，
所以整个框始终可见，也能分清前后。

| 命令 | 作用 |
| --- | --- |
| `/measurement` | 输出两个角点、长 (X)、宽 (Z)、高 (Y)、体积和除去空气的体积 |
| `/measurement on` | 开启木铲选区和选框（默认开启） |
| `/measurement off` | 关闭它们，木铲恢复正常使用 |
| `/measurement clear` | 清除选区 |

开关状态保存在 `config/measurement.properties`。除去空气的体积会统计所有不是空气的方块（水等流体也算）。
客户端只知道已加载的区块，未加载区块中的方块不会被统计，消息里会写出跳过了多少个。

## 光影

选框和标签使用的是 Minecraft 绘制名牌时的透视渲染类型，所以和 [Iris](https://modrinth.com/mod/iris) 及光影包兼容。已用 Iris 和 Complementary Reimagined 测试。

## 支持的版本

每个 Minecraft 版本系列都有自己的 git 分支，`main` 跟随最新正式版。

| 分支 | Minecraft | Fabric API | Java |
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

修复提交到适用的最旧分支，再向新分支合并。

## 构建

需要 JDK 21+（26.x 分支需要 JDK 25）。

```
./gradlew build
```

jar 输出在 `build/libs/`。

## 许可证

MIT
