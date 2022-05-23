val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

taboolib {
    install("common", "common-5", "module-configuration", "module-ui")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    compileOnly(project(":project:common")) { isTransitive = false }
    compileOnly("public:MythicMobs:1.0.1")
    compileOnly("public:MythicMobs5:5.0.4")
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}