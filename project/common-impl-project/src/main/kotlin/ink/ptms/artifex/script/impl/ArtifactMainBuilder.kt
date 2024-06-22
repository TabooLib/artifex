package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.ArtifactDescription
import ink.ptms.artifex.script.toFileSet
import taboolib.common.platform.function.info
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtifactMainBuilder
 *
 * @author 坏黑
 * @since 2022/6/14 14:33
 */
abstract class ArtifactMainBuilder {

    abstract val name: String

    open fun build(description: ArtifactDescription): ArtifactMain {
        var main = description.file.getString("main")!!
        if (main.indexOf('.') == -1 && main.indexOf('/') == -1) {
            error("main requires at least 1 separator, such as \".\" or \"/\"")
        }
        main = main.replace('.', '/')
        main = main.substringBeforeLast('/') + "/$name/" + main.substringAfterLast('/')
        description.main(main.replace('/', '.'))
        val proxyJar = javaClass.classLoader.getResourceAsStream("proxy/$name.jar")?.readBytes() ?: error("proxy/$name.jar not found")
        val mainClass = getMainClass(proxyJar)
        return ArtifactMain(main, ArtifactRemapping.remap(mainClass, main, description))
    }

    protected fun getMainClass(byteArray: ByteArray): ByteArray {
        val fileSet = ZipInputStream(ByteArrayInputStream(byteArray)).toFileSet()
        return fileSet["artifex/ArtifexPlugin.class"]!!
    }
}