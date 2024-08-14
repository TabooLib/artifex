package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptEnvironment
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import taboolib.common.io.newFile
import taboolib.common.io.taboolibId
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.lang.sendLang
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
    val globalImports = HashSet<String>()

    override fun getGlobalImports(): List<String> {
        return globalImports.toList()
    }

    override fun getClasspath(input: List<Class<*>>): List<File> {
        val dependencies = ArrayList<File?>()
        // 运行库
        dependencies += KotlinEnvironments.getKotlinFiles()
        dependencies += KotlinEnvironments.getFiles(File(getDataFolder(), "runtime"))
        // taboolib 模块
        dependencies += KotlinEnvironments.getTabooModules()
        // 插件列表
        dependencies += DefaultScriptAPI.getPlatformHelper().plugins().map { file(it.javaClass) }
        // 预设
        dependencies += input.map { file(it) }
        return dependencies.filterNotNull()
    }

    override fun setupGlobalImports() {
        val classLoader = listOf(DefaultScriptEnvironment::class.java.classLoader, Artifex.api().getScriptClassLoader() as ClassLoader)
        pluginImports.clear()
        globalImports.clear()
        globalImports.addAll(loadImportsFromFile(releaseResourceFile("standard.imports", true), classLoader).also {
            newFile(getDataFolder(), ".temp/standard.imports").writeText(it.joinToString("\n"))
        })
        globalImports.addAll(loadFunctionsFromFile(releaseResourceFile("standard.functions", true)))
        console().sendLang("loaded-imports", globalImports.size, pluginImports.size)
        // 过时处理
        checkLegacy()
    }

    override fun loadImportsFromFile(file: File, classLoader: List<ClassLoader>): List<String> {
        return loadImportsFromString(file.readLines(StandardCharsets.UTF_8), classLoader)
    }

    override fun loadImportsFromString(str: List<String>, classLoader: List<ClassLoader>): List<String> {
        val scanner = FastClasspathScanner(*str.filter { it.isNotBlank() }.toTypedArray())
        classLoader.forEach { scanner.addClassLoader(it) }
        val classes = scanner.alwaysScanClasspathElementRoot(false).scan().namesOfAllClasses.filter {
            !it.startsWith("kotlin.")
        }
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

//        val pluginClassLoader = plugin.javaClass.classLoader as URLClassLoader
//        // 排除其它插件的 kotlin 影响
//        val excludedClassLoader = object : URLClassLoader(pluginClassLoader.urLs, plugin.javaClass.classLoader) {
//            override fun loadClass(name: String, resolve: Boolean): Class<*> {
//                // 排除 Kotlin 相关的类
//                if (name.startsWith("kotlin.")) {
//                    throw ClassNotFoundException(name)
//                }
//                return super.loadClass(name, resolve)
//            }
//        }

        val imports = loadImportsFromString(listOf("!!", main, *extra, "-$main.$taboolibId"), listOf(plugin.javaClass.classLoader))
        if (imports.isNotEmpty()) {
            pluginImports[name] = imports
        }
        newFile(getDataFolder(), ".temp/plugin.$name.imports").writeText(imports.joinToString("\n"))
        return imports
    }

    override fun loadFunctionsFromFile(file: File): List<String> {
        return file.readLines(StandardCharsets.UTF_8).filter { it.isNotBlank() }
    }

    fun file(clazz: Class<*>): File? {
        return clazz.protectionDomain.codeSource?.location?.file?.let { File(URLDecoder.decode(it, "UTF-8")) }
    }

    fun classpathWithoutPlugins(input: List<Class<*>>): List<File> {
        val dependencies = ArrayList<File?>()
        // 运行库
        dependencies += KotlinEnvironments.getKotlinFiles()
        dependencies += KotlinEnvironments.getFiles(File(getDataFolder(), "runtime"))
        // taboolib 模块
        dependencies += KotlinEnvironments.getTabooModules()
        // 预设
        dependencies += input.map { file(it) }
        return dependencies.filterNotNull()
    }

    fun checkLegacy() {
        var def = File(getDataFolder(), "default.imports")
        if (def.exists()) {
            def.copyTo(newFile(getDataFolder(), "default.imports.bak"), true)
            def.delete()
        }
        def = File(getDataFolder(), "default.functions")
        if (def.exists()) {
            def.copyTo(newFile(getDataFolder(), "default.functions.bak"), true)
            def.delete()
        }
    }
}