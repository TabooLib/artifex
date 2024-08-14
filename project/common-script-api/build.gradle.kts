taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
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