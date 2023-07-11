val taboolib_version: String by project

plugins {
//    id("io.izzel.taboolib")
}

//taboolib {
//    install("common", "common-5")
//    install("module-configuration", "module-lang")
//    install("expansion-command-helper")
//    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
//    classifier = null
//    version = taboolib_version
//}

dependencies {
    api("io.izzel.taboolib:common:$taboolib_version")
    api("io.izzel.taboolib:common-5:$taboolib_version")
    api("io.izzel.taboolib:module-lang:$taboolib_version")
    api("io.izzel.taboolib:module-configuration:$taboolib_version")
    api("io.izzel.taboolib:expansion-command-helper:$taboolib_version")
    api(project(":project:common"))
}