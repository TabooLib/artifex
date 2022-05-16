package ink.ptms.artifex.kotlin

import ink.ptms.artifex.ArtScript
import ink.ptms.artifex.Artifex
import ink.ptms.artifex.Import
import ink.ptms.artifex.Include
import ink.ptms.artifex.script.ScriptRuntimeProperty
import org.jetbrains.kotlin.mainKts.CompilerOptions
import java.util.*
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

/**
 * 编译配置
 */
@Suppress("SimplifiableCallChain")
class KotlinCompilationConfiguration(val props: ScriptRuntimeProperty) : ScriptCompilationConfiguration(
    {
        baseClass(ArtScript::class)
        defaultImports(Include::class, Import::class, CompilerOptions::class)
        defaultImports.append(Artifex.api().scriptEnvironment().getGlobalImports())
        jvm {
            dependenciesFromClassContext(KotlinCompilationConfiguration::class, wholeClasspath = true)
            compilerOptions("-jvm-target", "1.8")
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
)