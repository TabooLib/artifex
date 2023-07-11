package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptEnvironment
 *
 * 脚本环境
 *
 * @author 坏黑
 * @since 2022/5/16 14:10
 */
interface ScriptEnvironment {

    /**
     * 获取全局 import 列表
     */
    fun getGlobalImports(): List<String>

    /**
     * classpath 中缺少依赖文件将会产生 Unresolved reference 错误
     */
    fun getClasspath(input: List<Class<*>>): List<File>

    /**
     * 加载脚本中所需的 import 列表
     */
    fun setupGlobalImports()

    /**
     * 从文件中读取 import 列表
     */
    fun loadImportsFromFile(file: File, classLoader: List<ClassLoader>): List<String>

    /**
     * 从字符串中读取 import 列表
     */
    fun loadImportsFromString(str: List<String>, classLoader: List<ClassLoader>): List<String>

    /**
     * 从插件中加载，可传入插件名称或特定包名
     */
    fun loadImportFromPlugin(name: String): List<String>

    /**
     * Kotlin 中的顶层函数需要单独引入，不能使用 FastClasspathScanner 扫描
     *
     * 同时还需要保留 kotlin_module 文件，
     * 该文件中记录了顶层函数的所有信息
     */
    fun loadFunctionsFromFile(file: File): List<String>
}