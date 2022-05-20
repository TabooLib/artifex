package ink.ptms.artifex.kotlin

import ink.ptms.artifex.ArtScript
import ink.ptms.artifex.Artifex
import ink.ptms.artifex.Import
import ink.ptms.artifex.Include
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptRuntimeProperty
import org.jetbrains.kotlin.mainKts.CompilerOptions
import taboolib.common.platform.function.getDataFolder
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

/**
 * 编译配置
 */
@Suppress("SimplifiableCallChain")
class KotlinCompilationConfiguration(val props: ScriptRuntimeProperty) : ScriptCompilationConfiguration(
    {
        val classpath = Artifex.api().scriptEnvironment().getClasspath(listOf(KotlinCompilationConfiguration::class.java))
        updateClasspath(classpath)
        baseClass(ArtScript::class)
        defaultImports(Include::class, Import::class, CompilerOptions::class)
        defaultImports.append(Artifex.api().scriptEnvironment().getGlobalImports())
        jvm {
            compilerOptions("-jvm-target", "1.8")
            // 避免版本检查
            // Some JAR files in the classpath have the Kotlin Runtime library bundled into them.
            // This may cause difficult to debug problems if there's a different version of the Kotlin Runtime library in the classpath.
            // Consider removing these libraries from the classpath
            // Library has Kotlin runtime bundled into it
            compilerOptions("-Xskip-runtime-version-check")
        }
        refineConfiguration {
            onAnnotations(Include::class, Import::class, CompilerOptions::class, handler = KotlinCompilationConfigurationHandler())
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
        val map = props.providedProperties.map { it.key.toString() to it.value.javaClass.kotlin }.toMutableList<Pair<String, KClass<*>>>()
        map += "runArgs" to Properties::class
        providedProperties(*map.toTypedArray())
    }
), ScriptCompiler.Configuration