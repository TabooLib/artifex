val taboolib_version: String by project

plugins {
//    id("io.izzel.taboolib")
    id("com.github.johnrengelman.shadow")
}

//taboolib {
//    install("common", "common-5", "module-configuration")
//    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
//    classifier = null
//    version = taboolib_version
//    exclude("taboolib")
//}

dependencies {
    api("io.izzel.taboolib:common:$taboolib_version")
    api("io.izzel.taboolib:common-5:$taboolib_version")
    api("io.izzel.taboolib:module-configuration:$taboolib_version")
    api("io.izzel.taboolib:platform-velocity:$taboolib_version")
    compileOnly(project(":project:common")) { isTransitive = false }
    compileOnly("io.papermc:velocity:3.3.0:376")
}