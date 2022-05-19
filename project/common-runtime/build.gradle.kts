import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

taboolib {
    install("common")
    install("module-configuration")
    options("skip-taboolib-relocate")
    classifier = null
    version = taboolib_version
}

dependencies {
    val kotlinVersion = "1.5.10"
    api(project(":project:common"))
    implementation("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
    implementation("ink.ptms.artifex:EmptyBukkit:1.0.0")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            val kotlinVersion = "1.5.10"
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion"))
            include(dependency("ink.ptms.artifex:EmptyBukkit:1.0.0"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}