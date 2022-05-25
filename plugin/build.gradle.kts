import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.FileInputStream
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

plugins {
    java
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
    create("collect") {
        dependsOn(build)
        doFirst {
            val version = project.version
            val file = projectDir.resolve("build/libs/plugin-$version.jar")
            val newFile = projectDir.resolve("build/libs/plugin-$version-all.jar")
            ZipFile(file).use { old ->
                ZipOutputStream(FileOutputStream(newFile)).use { new ->
                    for (entry in old.entries()) {
                        new.putNextEntry(entry)
                        if (!entry.isDirectory) {
                            new.write(old.getInputStream(entry).readBytes())
                        }
                        new.closeEntry()
                    }
                    new.putNextEntry(JarEntry("runtime/bridge.jar"))
                    new.write(rootProject.file("project/common-bridge/build/libs/common-bridge-$version-origin.jar").readBytes())
                    new.closeEntry()
                    new.putNextEntry(JarEntry("runtime/core.jar"))
                    new.write(rootProject.file("project/common-runtime/build/libs/common-runtime-$version.jar").readBytes())
                    new.closeEntry()
                }
            }
        }
    }
    build {
        dependsOn(shadowJar)
    }
}