# Artifex

Artifex 提供了完善的 Kotlin Script (.kts) 运行环境，且支持 [TabooLib](https://github.com/taboolib/taboolib) 全特性。

```kotlin
val compiledScript = Artifex.api().scriptCompiler().compile {
    // 传入源文件
    it.source(File(getDataFolder(), "test.kts"))
    // 成功时回调
    it.onSuccess { r ->
        // 生成 jar 文件
        r.generateScriptJar(newFile(getDataFolder(), "test.jar"))
    }
}

// 运行脚本
scriptCompiled.invoke(...)
```

## 使用方式

https://github.com/taboolib/artifex/wiki

## 感谢

+ 神奇田螺捐赠 `16160` 原石并提供测试环境
