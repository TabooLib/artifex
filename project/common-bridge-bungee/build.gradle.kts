val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

taboolib {
    install("common", "common-5", "module-configuration")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    compileOnly(project(":project:common")) { isTransitive = false }
    compileOnly("net.md_5.bungee:BungeeCord:1")
}