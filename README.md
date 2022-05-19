# Artifex

**模块结构如下**

* **common**
    * 核心模块
    * 包含主类 `Artifex` 及所有接口
    * 不含任何业务逻辑

* **common-runtime**
    * 运行环境模块
    * 依赖 `kotlin.script.experimental` 的类文件必须在此模块中运行

* **implementation-bukkit**
    * 实现了 `common` 模块中的平台适配类型接口
    * 提供了 `Bukkit` 专用特性
    * 依赖 `org.bukkit`

* **implementation-bungee**
    * 实现了 `common` 模块中的平台适配类型接口
    * 提供了 `BungeeCord` 专用特性
    * 依赖 `net.md_5.bungee`

* **implementation-common-default**
    * 实现模块
    * 实现了 `common` 模块中的通用类型接口
    * 不依赖 `kotlin.script.experimental`