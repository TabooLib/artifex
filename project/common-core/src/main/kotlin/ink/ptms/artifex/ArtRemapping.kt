package ink.ptms.artifex

import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl
import taboolib.common.platform.function.warning
import taboolib.library.asm.ClassReader
import taboolib.library.asm.ClassVisitor
import taboolib.library.asm.ClassWriter
import taboolib.library.asm.commons.ClassRemapper
import taboolib.library.asm.commons.Remapper
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

fun CompiledScript.remap(): CompiledScript {
    if (this is KJvmCompiledScript) {
        runCatching {
            val compilerOutputFiles = (getCompiledModule() as KJvmCompiledModuleInMemoryImpl).compilerOutputFiles
            val mapValues = compilerOutputFiles.mapValues {
                if (it.key.endsWith(".class")) {
                    val classReader = ClassReader(it.value)
                    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                    val classVisitor: ClassVisitor = ClassRemapper(classWriter, Artifex.api().getScriptCompiler().getRemapper())
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
    } else {
        warning("Unsupported script type: ${this::class.simpleName}")
    }
    return this
}