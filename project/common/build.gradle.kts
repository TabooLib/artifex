val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib")
}

taboolib {
    install("common", "common-5")
    install(
        "module-configuration",
        "module-chat",
        "module-lang",
        "module-kether",
        "module-effect",
        "module-database",
    )
    install(
        "expansion-command-helper",
        "expansion-player-database",
        "expansion-persistent-container-object"
    )
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
//    relocate("io.github.lukehutch", "taboolib.library")
//    relocate("taboolib.common.OpenAPI", "!ink.ptms.artifex.taboolib.common")
}

dependencies {
    taboo("io.github:fast-classpath-scanner:3.1.13")
}