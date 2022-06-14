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

https://github.com/taboolib/artifex/wiki

## 感谢

+ 神奇田螺捐赠 `16160` 原石并提供测试环境
+ 部分代码来自 [https://github.com/Redempt/RedLib](https://github.com/Redempt/RedLib)（请给它一个星星️）
