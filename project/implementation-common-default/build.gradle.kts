val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
}

taboolib {
    install("common")
    install("module-configuration")
    options("skip-taboolib-relocate")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    api(project(":project:common"))
}