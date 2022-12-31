val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib")
    id("com.github.johnrengelman.shadow")
}

taboolib {
    install("common", "common-5")
    install("module-configuration", "module-ui", "module-nms", "module-nms-util", "module-kether")
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