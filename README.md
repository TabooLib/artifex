# Artifex

Artifex 提供了完善的 Kotlin Script (.kts) 运行环境，且支持 [TabooLib](https://github.com/taboolib/taboolib) 全特性。

```kotlin
val compiledScript = Artifex.api().scriptCompiler().compile {

    // 传入源文件
    it.source(File(getDataFolder(), "test.kts"))

    it.onReport { r ->
        if (r.severity > ScriptResult.Severity.DEBUG) {
            warning(r.message)
        }
    }

    it.onSuccess { r ->
        // 生成 jar 文件
        r.generateScriptJar(newFile(getDataFolder(), "test.jar"))
    }

    it.onFailure {
        warning("编译失败")
    }
}

// 运行脚本
scriptCompiled.invoke(...)
```

## 使用方式

Artifex 可基于 Bukkit & BungeeCord 环境下运行：

1. 构建项目

```
./gradlew build
```

1. 在 `/plugin/build/libs` 目录中获取 **插件** 文件
2. 将 `/project/common-runtime/build/libs` 目录中的 `common-runtime-1.0.0.jar` 文件放入 `plugins/Artifex/runtime` 目录下并更名为 `core.jar`
3. 将 `/project/common-bridge/build/libs` 目录中的 `common-bridge-1.0.0-origin.jar` 文件放入 `plugins/Artifex/runtime` 目录下并更名为 `bridge.jar`

> 也可在 `Github Actions` 中直接获取

## 模块结构

* **common**
    * 核心模块
    * 包含主类 `Artifex` 及所有接口
    * 不含任何业务逻辑

* **common-runtime**
    * 独立运行环境
    * 依赖 `kotlin.script.experimental` 的类文件必须在此模块中编写

* **controller**
    * 游戏内控制器
    * 不实现任何逻辑，仅提供基于 Minecraft 游戏内的脚本控制方法（例如：命令、配置）

* **implementation-bukkit**
    * 实现了 `common` 模块中的平台适配相关接口
    * 提供了 `Bukkit` 专用特性
    * 依赖 `org.bukkit`

* **implementation-bungee**
    * 实现了 `common` 模块中的平台适配相关接口
    * 提供了 `BungeeCord` 专用特性
    * 依赖 `net.md_5.bungee`

* **implementation-common-default**
    * 实现了 `common` 模块中的通用接口
    * 不依赖 `kotlin.script.experimental`

## 感谢

+ 神奇田螺捐赠 `16160` 原石并提供测试环境
+ 部分代码来自 https://github.com/Redempt/RedLib（请给它一个星星️）