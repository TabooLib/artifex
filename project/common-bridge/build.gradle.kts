import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

taboolib {
    install("common", "common-5")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    api(project(":project:common"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("origin")
        dependencies {
            include(dependency("1:1:1"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}