package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptEnvironment
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import taboolib.common.io.newFile
import taboolib.common.io.taboolibId
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File
import java.net.URLDecoder
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

    override fun getClasspath(input: List<Class<*>>): List<File> {
        val dependencies = ArrayList<File?>()
        // 运行库
        dependencies += File(getDataFolder(), "runtime").listFiles()!!.filter { it.extension == "jar" }
        // 插件列表
        dependencies += Artifex.api().getPlatformHelper().plugins().map { file(it.javaClass) }
        // 预设
        dependencies += input.map { file(it) }
        return dependencies.filterNotNull()
    }

    override fun setupGlobalImports() {
        val classLoader = listOf(DefaultScriptEnvironment::class.java.classLoader, Artifex.api().getScriptClassLoader() as ClassLoader)
        pluginImports.clear()
        globalImports.clear()
        globalImports.addAll(loadImportsFromFile(releaseResourceFile("default.imports"), classLoader).also {
            newFile(getDataFolder(), ".out/default.imports").writeText(it.joinToString("\n"))
        })
        globalImports.addAll(loadFunctionsFromFile(releaseResourceFile("default.functions")))
    }

    override fun loadImportsFromFile(file: File, classLoader: List<ClassLoader>): List<String> {
        return loadImportsFromString(file.readLines(StandardCharsets.UTF_8), classLoader)
    }

    override fun loadImportsFromString(str: List<String>, classLoader: List<ClassLoader>): List<String> {
        val scanner = FastClasspathScanner(*str.filter { it.isNotBlank() }.toTypedArray())
        classLoader.forEach { scanner.addClassLoader(it) }
        val classes = scanner.alwaysScanClasspathElementRoot(false).scan().namesOfAllClasses
        return classes.map { it.substringBeforeLast(".") }.filter { it.isNotEmpty() }.toSet().map { "$it.*" }
    }

    override fun loadImportFromPlugin(name: String): List<String> {
        if (pluginImports.contains(name)) {
            return pluginImports[name]!!
        }
        val args = name.split(":")
        val plugin = Artifex.api().getPlatformHelper().plugin(args[0]) ?: return emptyList()
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
        val imports = loadImportsFromString(listOf("!!", main, *extra, "-$main.$taboolibId"), listOf(plugin.javaClass.classLoader))
        if (imports.isNotEmpty()) {
            pluginImports[name] = imports
        }
        newFile(getDataFolder(), ".out/plugin.$name.imports").writeText(imports.joinToString("\n"))
        return imports
    }

    override fun loadFunctionsFromFile(file: File): List<String> {
        return file.readLines(StandardCharsets.UTF_8).filter { it.isNotBlank() }
    }

    fun file(clazz: Class<*>): File? {
        return clazz.protectionDomain.codeSource?.location?.file?.let { File(URLDecoder.decode(it, "UTF-8")) }
    }
}