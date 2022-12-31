val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib")
}

taboolib {
    install("common")
    install("module-configuration", "module-lang")
    options("skip-taboolib-relocate")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    api(project(":project:common"))
}