import io.izzel.taboolib.gradle.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException

val taboolib_version: String by project

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.ow2.asm:asm:9.2")
        classpath("org.ow2.asm:asm-commons:9.2")
    }
}

plugins {
    id("org.gradle.java")
    id("org.gradle.maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("io.izzel.taboolib") version "2.0.13"
}

subprojects {
    apply(plugin = "io.izzel.taboolib")
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
//    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.tabooproject.org/repository/releases") }
    }

    taboolib {
        env {
            install(
                CONFIGURATION,
                CHAT,
                LANG,
                KETHER,
                EFFECT,
                DATABASE
            )
            install(
                EXPANSION_COMMAND_HELPER,
                EXPANSION_PLAYER_DATABASE,
                EXPANSION_REDIS, EXPANSION_LETTUCE_REDIS,
                EXPANSION_JAVASCRIPT,
                EXPANSION_GEEK_TOOL,
                EXPANSION_PLAYER_FAKE_OP,
                EXPANSION_PTC, EXPANSION_PTC_OBJECT,
                EXPANSION_SUBMIT_CHAIN
            )
            install(BUKKIT_ALL, UI, NMS, NMS_UTIL)
            install(BUNGEE, PORTICUS)
            install(VELOCITY)
        }
        version {
            taboolib = taboolib_version
            coroutines = null
        }
        if (project.name != "plugin") {
            exclude("plugin.yml")
            exclude("bungee.yml")
            exclude("velocity-plugin.json")
            exclude("taboolib")
        }
        classifier = null
        // asm
        relocate("org.objectweb.asm.", "org.objectweb.asm9.")
        // 第三方库
        relocate("ink.ptms.um", "ink.ptms.artifex.library.um")
        relocate("io.github.lukehutch", "ink.ptms.artifex.library")
    }

    tasks {
        jar {
            if (!project.taboolib.isSubproject) {
                return@jar
            }
            doLast {
                // 配置
                val relocations = taboolib.relocation
                val mapping = relocations.mapKeys { it.key.replace('.', '/') }.mapValues { it.value.replace('.', '/') }
                val remapper = RelocateRemapper(relocations, mapping)

                val inJar = archiveFile.get().asFile
                val tempOut = File(inJar.parentFile, "${inJar.nameWithoutExtension}-relocated.jar")

                // 第一次工作
                JarOutputStream(FileOutputStream(tempOut)).use { out ->
                    val buf = ByteArray(32768)
                    JarFile(inJar).use { jarFile ->
                        // region 重定向
                        jarFile.entries().asSequence().forEach { jarEntry ->
                            val path = jarEntry.name
                            jarFile.getInputStream(jarEntry).use { inputStream ->
                                if (path.endsWith(".class")) {
                                    val reader = ClassReader(inputStream)
                                    val writer = ClassWriter(0)
                                    val visitor = TabooLibClassVisitor(writer, project, taboolib, false)
                                    val rem = ClassRemapper(visitor, remapper)
                                    remapper.remapper = rem
                                    reader.accept(rem, 0)
                                    // 写回文件
                                    // 拦截报错防止文件名称重复导致编译终止
                                    try {
                                        out.putNextEntry(JarEntry(remapper.map(path)))
                                    } catch (zipException: ZipException) {
                                        println(zipException)
                                        return@forEach
                                    }
                                    out.write(writer.toByteArray())
                                } else {
                                    try {
                                        out.putNextEntry(JarEntry(remapper.map(path)))
                                    } catch (zipException: ZipException) {
                                        println(zipException)
                                        return@forEach
                                    }
                                    var n: Int
                                    while (inputStream.read(buf).also { n = it } != -1) {
                                        out.write(buf, 0, n)
                                    }
                                }
                            }
                        }
                    }
                }
                // api mode
//                Files.copy(tempOut.toPath(), inJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    dependencies {
        "compileOnly"(kotlin("stdlib"))
        // Reflex Remapper
        "compileOnly"("org.ow2.asm:asm:9.6")
        "compileOnly"("org.ow2.asm:asm-util:9.6")
        "compileOnly"("org.ow2.asm:asm-commons:9.6")
    }

    tasks.register<Copy>("relocateClasses") {
        val srcDir = file("$buildDir/classes/java/main")
        val destDir = file("$buildDir/classes/java/main-relocated")

        if (taboolib.isSubproject) {
            into(srcDir)
            return@register
        }

        from(srcDir) {
            include("**/*.class")
            eachFile {
                val relativePath = file.relativeTo(srcDir).path
                taboolib.relocation.forEach { t, u ->
                    path = relativePath.replace(t, u)
                    filter { line -> line.replace(t, u) }
                }
            }
        }
        into(destDir)

    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
        }
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}