package ink.ptms.artifex.script.impl

import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.env.*
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

object KotlinEnvironments {

    /**
     * 获取 TabooLib 中的 env.properties 环境配置文件
     */
    val properties = PrimitiveSettings.RUNTIME_PROPERTIES

    /**
     * 默认下载源
     */
    val repository: String
        get() = "https://maven.aliyun.com/repository/central"

    /**
     * TabooLib 下载源
     */
    val repositoryTabooLib: String
        get() = "https://repo.tabooproject.org/repository/releases"


    private val baseDir = newFile(getDataFolder(), "runtime/libraries", folder = true)

    private val kotlinVersion = "1.8.20"

    private val relocation = listOf(JarRelocation("kotlin", "kotlin${kotlinVersion.replace(".", "")}"))

    fun loadDependencies() {
        loadDependencies("org.jetbrains.kotlin:kotlin-main-kts:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:$kotlinVersion", repository)
        loadDependencies("org.jetbrains.intellij.deps:trove4j:1.0.20181211", repository)
        loadDependencies("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2", repository)
        // 需要补 common-reflex
        loadDependencies(
            "io.izzel.taboolib:common-reflex:${PrimitiveSettings.TABOOLIB_VERSION}",
            repositoryTabooLib,
            dir =  File(PrimitiveSettings.FILE_LIBS),
            impl = false
        )
    }

    fun loadDependencies(source: String, repository: String, dir: File = baseDir, impl: Boolean = true) {
        val args = source.split(":")
        val downloader = DependencyDownloader(dir)
        if (properties.contains("repository-$repository")) {
            downloader.addRepository(Repository(properties.getProperty("repository-$repository")))
        } else {
            downloader.addRepository(Repository(repository))
        }
        val pomFile = File(dir, String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]))
        val pomShaFile = File(pomFile.path + ".sha1")
        if (pomFile.exists() && pomShaFile.exists() && PrimitiveIO.readFile(pomShaFile).startsWith(PrimitiveIO.getHash(pomFile))) {
            downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream())
        } else {
            val pom = String.format("%s/%s/%s/%s/%s-%s.pom", repository, args[0].replace('.', '/'), args[1], args[2], args[1], args[2])
            try {
                PrimitiveIO.println(String.format("Downloading library %s:%s:%s", args[0], args[1], args[2]))
                downloader.loadDependencyFromInputStream(URL(pom).openStream())
            } catch (ex: FileNotFoundException) {
                throw ex
            }
        }
        downloader.loadDependency(
            downloader.repositories,
            Dependency(args[0], args[1], args[2], if (impl) DependencyScope.RUNTIME else DependencyScope.PROVIDED)
        )
    }

    fun getFiles(file: File): List<File> {
        return when {
            file.isDirectory -> file.listFiles()?.flatMap { getFiles(it) } ?: emptyList()
            file.extension == "jar" -> listOf(file)
            else -> emptyList()
        }
    }

    fun getTabooModules(): List<File> {
        val files = mutableListOf<File>()
        // 必须补全缺失的 common 模块
        val modules = listOf(
            "common-env",
            "common-util",
            "common-reflex",
            "common-legacy-api",
            "common-platform-api",
            *PrimitiveSettings.INSTALL_MODULES
        )
        files += modules.map {
            File(
                PrimitiveSettings.FILE_LIBS,
                String.format(
                    "%s/%s/%s/%s-%s.jar",
//                    PrimitiveLoader.TABOOLIB_GROUP.replace(".", "/"),
                    "io/izzel/taboolib",
                    it,
                    PrimitiveSettings.TABOOLIB_VERSION,
                    it,
                    PrimitiveSettings.TABOOLIB_VERSION
                )
            )
        }
        files += File("cache/taboolib/ink.ptms.artifex").listFiles() ?: arrayOf()
        return files
    }

    fun getKotlinFiles(): List<File> {
        val baseFile = File(PrimitiveSettings.FILE_LIBS)
        val files = ArrayList<File>()
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib/$kotlinVersion/kotlin-stdlib-$kotlinVersion.jar")
        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-common/$kotlinVersion/kotlin-stdlib-common-$kotlinVersion.jar")
//        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-jdk7/$kotlinVersion/kotlin-stdlib-jdk7-$kotlinVersion.jar")
//        files += File(baseFile, "org/jetbrains/kotlin/kotlin-stdlib-jdk8/$kotlinVersion/kotlin-stdlib-jdk8-$kotlinVersion.jar")
        return files.filter { it.exists() }
    }
}