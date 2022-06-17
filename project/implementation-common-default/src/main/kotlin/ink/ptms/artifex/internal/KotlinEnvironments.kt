package ink.ptms.artifex.internal

import ink.ptms.artifex.script.nonExists
import taboolib.common.TabooLibCommon
import taboolib.common.env.*
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.reflect.Reflex.Companion.getProperty
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

object KotlinEnvironments {

    /**
     * 获取 TabooLib 中的 env.properties 环境配置文件
     */
    val properties = RuntimeEnv::class.java.getProperty<Properties>("ENV_PROPERTIES", fixed = true)!!

    /**
     * 默认下载源
     */
    val repository: String
        get() = "https://maven.aliyun.com/repository/central"

    fun loadDependencies() {
        val kotlinVersion = "1.5.10"
        loadDependencies("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion")
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion")
    }

    fun loadDependencies(source: String) {
        val args = source.split(":")
        val baseDir = newFile(getDataFolder(), "runtime/libraries", folder = true)
        val downloader = DependencyDownloader(baseDir)
        if (properties.contains("repository-$repository")) {
            downloader.addRepository(Repository(properties.getProperty("repository-$repository")))
        } else {
            downloader.addRepository(Repository(repository))
        }
        val pomFile = File(baseDir, String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]))
        val pomShaFile = File(pomFile.path + ".sha1")
        if (pomFile.exists() && pomShaFile.exists() && DependencyDownloader.readFile(pomShaFile).startsWith(DependencyDownloader.readFileHash(pomFile))) {
            downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream())
        } else {
            val pom = String.format("%s/%s/%s/%s/%s-%s.pom", repository, args[0].replace('.', '/'), args[1], args[2], args[1], args[2])
            try {
                TabooLibCommon.print(String.format("Downloading library %s:%s:%s", args[0], args[1], args[2]))
                downloader.loadDependencyFromInputStream(URL(pom).openStream())
            } catch (ex: FileNotFoundException) {
                throw ex
            }
        }
        downloader.loadDependency(downloader.repositories, Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME))
    }

    fun getFiles(file: File): List<File> {
        return when {
            file.isDirectory -> file.listFiles()?.flatMap { getFiles(it) } ?: emptyList()
            file.extension == "jar" -> listOf(file)
            else -> emptyList()
        }
    }

    fun getKotlinFiles(): List<File> {
        val kotlinVersion = "1.5.10"
        val baseFile = File(properties.getProperty("library", "libs"))
        val files = ArrayList<File>()
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib/$kotlinVersion/kotlin-stdlib-$kotlinVersion.jar")
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-common/$kotlinVersion/kotlin-stdlib-common-$kotlinVersion.jar")
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-jdk7/$kotlinVersion/kotlin-stdlib-jdk7-$kotlinVersion.jar")
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-jdk8/$kotlinVersion/kotlin-stdlib-jdk8-$kotlinVersion.jar")
        return files.filter { it.exists() }
    }
}