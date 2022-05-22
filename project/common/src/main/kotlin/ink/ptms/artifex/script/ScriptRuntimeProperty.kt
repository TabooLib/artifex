package ink.ptms.artifex.script

import taboolib.common.io.digest
import java.io.File
import java.util.*

/**
 * @author 坏黑
 * @since 2022/4/12 02:12
 */
@Suppress("SimplifiableCallChain")
class ScriptRuntimeProperty {

    /**
     * 默认引用
     */
    val defaultImports = ArrayList<String>()

    /**
     * 默认 classpath
     */
    val defaultClasspath = ArrayList<File>()

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
     * 签名
     */
    fun digest(): String {
        return providedProperties.map { it.key.toString() to it.value.javaClass.name }.toMap().toString().digest("sha-1")
    }

    companion object {

        val defaultDigest = "{}".digest("sha-1")
    }
}