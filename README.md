# Create: Powertrain

独立的动力与物理机械模组，包含物理软管、流体铰链、应力连接器、绳索设备、摇杆和伪装门。消防设备及普通渲染水带由 Create: Firefighting Additions 提供；两个模组无需互相安装。

## 依赖

以下以 `src/main/templates/META-INF/neoforge.mods.toml` 的声明为准：

- Minecraft 1.21.1、NeoForge 21.1.228 或更高版本。
- Create：`[6.0.10,6.1.0)`，即 6.0.x。
- `sable` 2.0.0 或更高版本。
- `synaxis` 1.4.0 或更高版本，必须提供完整焊接和角度限位接口。
- `sable_schematic_api` 0.4.0 或更高版本。
- `aeronautics` 1.3.0 或更高版本：物理软管和绳索直接使用其 Simulated 实现，不能仅安装 Sable 和 Synaxis 代替。
- 可选：`jade` `[15,16)`（仅客户端）、`tracks` 1.0.0 或更高版本。

还须满足各前置自身的依赖。例如 Synaxis 1.5.0 要求 Sable Schematic API 0.4.1；Synaxis 要求 LDLib2，客户端要求 Flywheel。Sable 发行包包含自身的原生物理和渲染库。以所安装前置的加载声明为准。

不按缺失依赖隐藏物品、关闭单项功能或生成无功能占位实体。缺少硬依赖由加载器拒绝启动；不兼容的必要接口在初始化时明确报错。

## 共存操作

普通水带支架使用 Create 支架右键；物理软管支架使用潜行加右键，选点和放置均遵循这个区别。换槽、换手和丢弃取消选点。

两个模组通过可选的水带兼容路径传递原泵距离、压力、方向和来源类型。没有安装另一方时，不加载另一方的 Java 类型。

## 开发状态

开发状态：beta。自动检查涵盖编译和纯逻辑回归；游戏运行、多人服务器及不同依赖组合需要单独验证。

`gradlew check jar` 执行编译和纯逻辑检查，不启动游戏。可使用 `gradlew checkSynaxisApi -PsynaxisApiJar=<实际 Synaxis jar 路径>` 核对实际焊接 API 的链接；这不代替物理运行测试。

## 旧整合版同装

允许与旧消防整合版同时安装，不以版本号拦截启动。Powertrain 使用独立的铰链 SavedData、约束标识和 Mixin 接口，并为旧整合版增加绳索归属与应力网络兼容路径。已核对 0.2.7/0.2.8/0.2.9 的目标方法，静态与纯逻辑检查通过；实际同装运行仍需游戏测试。详见 [兼容说明](docs/legacy-coexistence.md)。

## 许可证

代码：MIT；美术资源：All Rights Reserved。
