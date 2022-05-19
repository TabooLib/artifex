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
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            val kotlinVersion = "1.5.10"
            include(dependency("org.jetbrains.intellij.deps:trove4j:1.0.20181211"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-daemon-embeddable:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion"))
            include(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}