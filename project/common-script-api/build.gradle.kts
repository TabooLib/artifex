import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

plugins {
//    id("io.izzel.taboolib")
//    id("com.github.johnrengelman.shadow")
}

//taboolib {
//    install("common", "common-5", "module-lang", "module-configuration", "module-database")
//    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
//    classifier = null
//    version = taboolib_version
//    exclude("taboolib")
//}

dependencies {
    api("io.izzel.taboolib:common:$taboolib_version")
    api("io.izzel.taboolib:common-5:$taboolib_version")
    api("io.izzel.taboolib:module-lang:$taboolib_version")
    api("io.izzel.taboolib:module-chat:$taboolib_version")
    api("io.izzel.taboolib:module-configuration:$taboolib_version")
    api("io.izzel.taboolib:module-database:$taboolib_version")
    api("io.izzel.taboolib:expansion-command-helper:$taboolib_version")
    api(project(":project:common"))
    compileOnly(project(":project:common-script-api-bukkit"))
    compileOnly(project(":project:common-script-api-bungee"))
    compileOnly(project(":project:common-script-api-velocity"))
}

//tasks {
//    withType<ShadowJar> {
//        archiveClassifier.set("shade")
//        dependencies {
//            include(project(":project:common-script-api-bukkit"))
//            include(project(":project:common-script-api-bungee"))
//        }
//        relocate("kotlin1820", "kotlin")
//    }
//    build {
//        dependsOn(shadowJar)
//    }
//}