import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-bridge"))
    implementation(project(":project:controller"))
    implementation(project(":project:implementation-bukkit"))
    implementation(project(":project:implementation-bungee"))
    implementation(project(":project:implementation-common-default"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
    }
    build {
        dependsOn(shadowJar)
    }
}