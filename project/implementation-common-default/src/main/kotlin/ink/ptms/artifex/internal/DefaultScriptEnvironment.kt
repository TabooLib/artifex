package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptEnvironment
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import taboolib.common.io.newFile
import taboolib.common.io.taboolibId
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultEnvironment
 *
 * @author 坏黑
 * @since 2022/5/16 14:13
 */
class DefaultScriptEnvironment : ScriptEnvironment {

    val pluginImports = ConcurrentHashMap<String, List<String>>()
    val globalImports = ArrayList<String>()

    override fun getGlobalImports(): List<String> {
        return globalImports
    }

    override fun setupClasspath() {
        val classpath = ArrayList<File>()
        classpath += File(getDataFolder(), "runtime").listFiles()!!.filter { it.extension == "jar" }
        classpath += File("plugins").listFiles()!!.filter { it.extension == "jar" }
        val separator = if (System.getProperty("os.name").lowercase().contains("windows")) ";" else ":"
        System.setProperty("kotlin.script.classpath", classpath.joinToString(separator) { it.path })
    }

    override fun setupGlobalImports() {
        pluginImports.clear()
        globalImports.clear()
        globalImports.addAll(loadImportsFromFile(releaseResourceFile("default.imports")))
        globalImports.addAll(loadFunctionsFromFile(releaseResourceFile("default.functions")))
    }

    override fun loadImportsFromFile(file: File, classLoader: ClassLoader?): List<String> {
        return loadImportsFromString(file.readLines(StandardCharsets.UTF_8), classLoader)
    }

    override fun loadImportsFromString(str: List<String>, classLoader: ClassLoader?): List<String> {
        val scanner = FastClasspathScanner(*str.filter { it.isNotBlank() }.toTypedArray())
        if (classLoader != null) {
            scanner.addClassLoader(classLoader)
        }
        val classes = scanner.alwaysScanClasspathElementRoot(false).scan().namesOfAllClasses
        return classes.map { it.substringBeforeLast(".") }.filter { it.isNotEmpty() }.toSet().map { "$it.*" }
    }

    override fun loadImportFromPlugin(name: String): List<String> {
        if (pluginImports.contains(name)) {
            return pluginImports[name]!!
        }
        val args = name.split(":")
        val plugin = Artifex.api().platformHelper().plugin(args[0]) ?: return emptyList()
        // 默认添加插件主类所在的包
        // 如果是 TabooLib 项目则进行特殊兼容
        val javaName = plugin.javaClass.name
        val main = if (javaName.contains(".$taboolibId")) {
            javaName.substringBefore(".$taboolibId")
        } else {
            javaName.substringBeforeLast('.')
        }
        // 插入用户片段
        val extra = if (args.size > 1) args[1].split(",").toTypedArray() else emptyArray()
        val imports = loadImportsFromString(listOf("!!", main, *extra, "-$main.$taboolibId"), plugin.javaClass.classLoader)
        if (imports.isNotEmpty()) {
            pluginImports[name] = imports
        }
        return imports
    }

    override fun loadFunctionsFromFile(file: File): List<String> {
        return file.readLines(StandardCharsets.UTF_8).filter { it.isNotBlank() }
    }
}