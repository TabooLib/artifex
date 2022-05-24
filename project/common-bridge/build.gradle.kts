import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

taboolib {
    install("common", "common-5", "module-lang", "module-configuration", "module-database")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    api(project(":project:common"))
    implementation(project(":project:common-bridge-bukkit"))
    implementation(project(":project:common-bridge-bungee"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("origin")
        dependencies {
            include(project(":project:common-bridge-bukkit"))
            include(project(":project:common-bridge-bungee"))
        }
        relocate("kotlin1510", "kotlin")
    }
    build {
        dependsOn(shadowJar)
    }
}