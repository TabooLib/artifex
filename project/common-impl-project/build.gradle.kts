val taboolib_version: String by project

plugins {
//    id("io.izzel.taboolib")
}

//taboolib {
//    install("common")
//    install("module-configuration", "module-lang")
//    options("skip-taboolib-relocate")
//    classifier = null
//    version = taboolib_version
//    exclude("taboolib")
//}

dependencies {
    api("io.izzel.taboolib:common:$taboolib_version")
    api("io.izzel.taboolib:module-lang:$taboolib_version")
    api("io.izzel.taboolib:module-configuration:$taboolib_version")
    api(project(":project:common"))
}