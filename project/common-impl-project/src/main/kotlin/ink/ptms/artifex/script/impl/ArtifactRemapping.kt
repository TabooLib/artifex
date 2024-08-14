package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.ArtifactDescription
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtifactRemapping
 *
 * @author 坏黑
 * @since 2022/6/14 14:17
 */
object ArtifactRemapping {

    fun remap(byteArray: ByteArray, main: String, description: ArtifactDescription): ByteArray {
        val classReader = ClassReader(byteArray)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor: ClassVisitor = ClassRemapper(classWriter, remapper(main, description))
        classReader.accept(classVisitor, 0)
        return classWriter.toByteArray()
    }

    private fun remapper(main: String, description: ArtifactDescription) = object : Remapper() {

        override fun mapType(internalName: String): String {
            return super.mapType(translate(internalName))
        }

        override fun map(internalName: String): String {
            return translate(internalName)
        }

        fun translate(key: String): String {
            return when (key) {
                "artifex/ArtifexPlugin" -> main.replace('.', '/')
                "@plugin_id@" -> description.file.getString("id")!!
                else -> key
            }
        }
    }
}