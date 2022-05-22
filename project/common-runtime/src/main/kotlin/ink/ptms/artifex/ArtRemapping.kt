package ink.ptms.artifex

import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl
import taboolib.library.asm.ClassReader
import taboolib.library.asm.ClassVisitor
import taboolib.library.asm.ClassWriter
import taboolib.library.asm.commons.ClassRemapper
import taboolib.library.asm.commons.Remapper
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

val remapper = object : Remapper() {

    override fun mapType(internalName: String): String {
        return super.mapType(translate(internalName))
    }

    override fun map(internalName: String): String {
        return translate(internalName)
    }

    fun translate(key: String): String {
        // 替换 Kotlin 的函数类型，以确保正确调用 TabooLib 方法
        return if (key.startsWith("kotlin/jvm/functions/Function")) key.replace("kotlin/", "kotlin1510/") else key
    }
}

fun CompiledScript.remap(): CompiledScript {
    if (this is KJvmCompiledScript) {
        kotlin.runCatching {
            val compilerOutputFiles = (getCompiledModule() as KJvmCompiledModuleInMemoryImpl).compilerOutputFiles
            val mapValues = compilerOutputFiles.mapValues {
                if (it.key.endsWith(".class")) {
                    val classReader = ClassReader(it.value)
                    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                    val classVisitor: ClassVisitor = ClassRemapper(classWriter, remapper)
                    classReader.accept(classVisitor, 0)
                    classWriter.toByteArray()
                } else {
                    it.value
                }
            }
            (compilerOutputFiles as MutableMap).putAll(mapValues)
        }.onFailure {
            it.printStackTrace()
        }
    }
    return this
}