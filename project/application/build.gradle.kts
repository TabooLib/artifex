val taboolib_version: String by project

plugins {
    application
    id("io.izzel.taboolib")
}

taboolib {
    install("common", "platform-application")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
}

dependencies {
    api(project(":project:common"))
}

application {
    mainClass.set("ink.ptms.artifex.Application")
}