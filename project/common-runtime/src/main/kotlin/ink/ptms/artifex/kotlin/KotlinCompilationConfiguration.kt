package ink.ptms.artifex.kotlin

import ink.ptms.artifex.*
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptRuntimeProperty
import org.jetbrains.kotlin.mainKts.CompilerOptions
import java.util.*
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

/**
 * 编译配置
 */
@Suppress("SimplifiableCallChain")
class KotlinCompilationConfiguration(val props: ScriptRuntimeProperty) : ScriptCompilationConfiguration(
    {
        val classpath = Artifex.api().getScriptEnvironment().getClasspath(listOf(KotlinCompilationConfiguration::class.java))
        updateClasspath(classpath)
        updateClasspath(props.defaultClasspath)
        baseClass(ArtScript::class)
        defaultImports(Art::class, Include::class, Import::class, CompilerOptions::class)
        defaultImports.append(Artifex.api().getScriptEnvironment().getGlobalImports())
        defaultImports.append(props.defaultImports)
        jvm {
            compilerOptions("-jvm-target", "1.8")
            // 避免版本检查
            // Some JAR files in the classpath have the Kotlin Runtime library bundled into them.
            // This may cause difficult to debug problems if there's a different version of the Kotlin Runtime library in the classpath.
            // Consider removing these libraries from the classpath
            // Library has Kotlin runtime bundled into it
            compilerOptions("-Xskip-runtime-version-check")
            compilerOptions("-Xskip-metadata-version-check")
            compilerOptions("-Xjvm-default", "all")
        }
        refineConfiguration {
            onAnnotations(Art::class, Include::class, Import::class, CompilerOptions::class, handler = KotlinCompilationConfigurationHandler(props))
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
        val map = props.providedProperties.map { it.key.toString() to it.value.javaClass.kotlin }.toMutableList<Pair<String, KClass<*>>>()
        map += "runArgs" to Properties::class
        providedProperties(*map.toTypedArray())
    }
), ScriptCompiler.Configuration