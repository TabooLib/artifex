val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
}

taboolib {
    description {
        name(rootProject.name)
    }
    install("common", "platform-bungee")
    install("module-porticus")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    relocate("taboolib.common.OpenAPI", "!ink.ptms.artifex.taboolib.common")
    relocate("taboolib.platform.BungeePlugin", "!ink.ptms.artifex.taboolib.platform")
    relocate("taboolib.platform.type.BungeeProxyEvent", "!ink.ptms.artifex.taboolib.platform.type")
}

dependencies {
    api(project(":project:common"))
}