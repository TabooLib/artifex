import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.zip.ZipFile

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-script-api"))
    implementation(project(":project:common-script-api-bukkit"))
    implementation(project(":project:common-script-api-bungee"))
    // 运行平台
    implementation(project(":project:bootstrap-bukkit"))
    implementation(project(":project:bootstrap-bungee"))
    // 逻辑实现
    implementation(project(":project:common-impl-default"))
    implementation(project(":project:common-impl-project"))
    // 第三方库
    implementation("ink.ptms:um:1.0.0-beta-29")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        // 移除不必要的文件
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        // 重定向 kotlin
        relocate("kotlin.", "kotlin1820.") {
            exclude("kotlin.Metadata")
        }
        // 重定向 TabooLib
        relocate("taboolib", "ink.ptms.artifex.taboolib")
        // 第三方库
        relocate("ink.ptms.um", "ink.ptms.artifex.library.um")
        relocate("io.github.lukehutch", "ink.ptms.artifex.library")
    }
    create("collect") {
        doFirst {
            val version = project.version
            val file = projectDir.resolve("build/libs/plugin-$version.jar")
            val newFile = projectDir.resolve("build/libs/${rootProject.name}-$version.jar")
            ZipFile(file).use { old ->
                ZipOutputStream(FileOutputStream(newFile)).use { new ->
                    for (entry in old.entries()) {
                        new.putNextEntry(entry)
                        if (!entry.isDirectory) {
                            new.write(old.getInputStream(entry).readBytes())
                        }
                        new.closeEntry()
                    }

                    // 因为 TabooLib 运行在 relocated 后的 Kotlin 环境中 (kotlin1820)
                    // 因此需要给脚本提供未经重定向的 jar 文件来进行编译

                    // 运行环境及标准库
                    applyToZip(new, version, "runtime/core", "common-core")
                    applyToZip(new, version, "runtime/script-api", "common-script-api")
                    applyToZip(new, version, "runtime/script-api-bukkit", "common-script-api-bukkit")
                    applyToZip(new, version, "runtime/script-api-bungee", "common-script-api-bungee")
                    // jar 代理
                    applyToZip(new, version, "proxy/bukkit", "jar-proxy-bukkit")
                    applyToZip(new, version, "proxy/bungee", "jar-proxy-bungee")
                }
            }
            file.delete()
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

fun applyToZip(new: ZipOutputStream, version: Any, name: String, module: String) {
    new.putNextEntry(JarEntry("$name.jar"))
    new.write(rootProject.file("project/$module/build/libs/$module-$version.jar").readBytes())
    new.closeEntry()
}