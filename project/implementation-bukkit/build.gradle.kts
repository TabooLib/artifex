val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
}

taboolib {
    description {
        name(rootProject.name)
    }
    install("common", "platform-bukkit")
    install("module-nms", "module-nms-util", "module-ui", "module-ai")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    relocate("taboolib.common.OpenAPI", "!ink.ptms.artifex.taboolib.common")
    relocate("taboolib.platform.BukkitPlugin", "!ink.ptms.artifex.taboolib.platform")
    relocate("taboolib.platform.type.BukkitProxyEvent", "!ink.ptms.artifex.taboolib.platform.type")
}

dependencies {
    api(project(":project:common"))
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}