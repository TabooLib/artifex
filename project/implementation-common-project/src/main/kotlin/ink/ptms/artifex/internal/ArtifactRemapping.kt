package ink.ptms.artifex.internal

import taboolib.common.platform.function.info
import taboolib.library.asm.ClassReader
import taboolib.library.asm.ClassVisitor
import taboolib.library.asm.ClassWriter
import taboolib.library.asm.commons.ClassRemapper
import taboolib.library.asm.commons.Remapper

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtifactRemapping
 *
 * @author 坏黑
 * @since 2022/6/14 14:17
 */
object ArtifactRemapping {

    fun remap(byteArray: ByteArray, main: String): ByteArray {
        val classReader = ClassReader(byteArray)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor: ClassVisitor = ClassRemapper(classWriter, remapper(main))
        classReader.accept(classVisitor, 0)
        return classWriter.toByteArray()
    }

    private fun remapper(main: String) = object : Remapper() {

        override fun mapType(internalName: String): String {
            return super.mapType(translate(internalName))
        }

        override fun map(internalName: String): String {
            return translate(internalName)
        }

        fun translate(key: String): String {
            return if (key == "artifex/ArtifexPlugin") main.replace('.', '/') else key
        }
    }
}