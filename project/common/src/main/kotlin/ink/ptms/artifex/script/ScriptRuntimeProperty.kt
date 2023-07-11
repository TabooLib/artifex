package ink.ptms.artifex.script

import taboolib.common.io.digest
import java.io.File
import java.util.*

/**
 * 脚本运行时属性
 *
 * @author 坏黑
 * @since 2022/4/12 02:12
 */
class ScriptRuntimeProperty {

    constructor()

    constructor(runArgs: Map<String, Any>, providedProperties: Map<String, Any>) {
        this.runArgs += runArgs
        this.providedProperties += providedProperties
    }

    /**
     * 默认引用
     */
    val defaultImports = ArrayList<String>()

    /**
     * 默认 classpath
     */
    val defaultClasspath = ArrayList<File>()

    /**
     * 默认脚本文件查找器
     */
    var defaultFileFinder: ScriptFileFinder? = null

    /**
     * 通过 runArgs 关键字调用的变量容器
     * 改变这个容器中的变量不会使脚本重新编译
     */
    val runArgs = Properties()

    /**
     * 直接参与脚本编译的变量容器
     * 改变这个容器中的变量将会使脚本重新编译
     */
    val providedProperties = Properties()

    /**
     * 编译版本
     */
    var jvmTarget = "1.8"

    /**
     * 编译参数
     */
    val compileOptions = arrayListOf("-Xskip-metadata-version-check", "-Xjvm-default=all")

    /**
     * 签名
     */
    fun digest(): String {
        return providedProperties.map { it.key.toString() to it.value.javaClass.name }.toMap().toString().digest("sha-1")
    }

    companion object {

        val defaultDigest = "{}".digest("sha-1")

        fun fromRunArgs(runArgs: Map<String, Any>) = ScriptRuntimeProperty().also { it.runArgs += runArgs }

        fun fromProvidedProperties(providedProperties: Map<String, Any>) = ScriptRuntimeProperty().also { it.providedProperties += providedProperties }
    }
}