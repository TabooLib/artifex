val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib")
    // id("com.github.johnrengelman.shadow")
}

taboolib {
//    install("common")
//    install("module-lang")
//    install("module-configuration")
//    options("skip-taboolib-relocate", "skip-kotlin-relocate", "skip-minimize", "keep-kotlin-module")
    options("skip-kotlin-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
    exclude("taboolib")
}

dependencies {
    val kotlinVersion = "1.8.20"
    api("io.izzel.taboolib:common:$taboolib_version")
    api("io.izzel.taboolib:module-lang:$taboolib_version")
    api("io.izzel.taboolib:module-configuration:$taboolib_version")
    api(project(":project:common"))
    api("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
}

//tasks {
//    withType<ShadowJar> {
//        archiveClassifier.set("")
//        relocate("taboolib", "ink.ptms.artifex.taboolib")
////        dependencies {
////            val kotlinVersion = "1.8.20"
////            include(dependency("org.jetbrains.intellij.deps:trove4j:1.0.20181211"))
////            include(dependency("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-daemon-embeddable:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion"))
////            include(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8"))
////        }
//    }
//    build {
//        dependsOn(shadowJar)
//    }
//}