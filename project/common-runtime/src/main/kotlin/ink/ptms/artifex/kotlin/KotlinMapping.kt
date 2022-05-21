package ink.ptms.artifex.kotlin

import ink.ptms.artifex.ImportScript
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptSourceCode
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl
import taboolib.common.platform.function.getDataFolder
import java.io.File
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

val scriptsFile = File(getDataFolder(), "scripts")

fun File.isKts(include: String): Boolean {
    return (name == include && extension == "kts") || name == "$include.kts"
}

fun File.isJar(include: String): Boolean {
    return (name == include && extension == "jar") || name == "$include.jar"
}

fun File.searchFile(match: File.() -> Boolean): Set<File> {
    return when {
        isDirectory -> listFiles()?.flatMap { it.searchFile(match) }?.toSet() ?: emptySet()
        match(this) -> setOf(this)
        else -> emptySet()
    }
}

fun position(position: SourceCode.Position): ScriptSourceCode.Position {
    return ScriptSourceCode.Position(position.line, position.col, position.absolutePos)
}

fun position(position: ScriptSourceCode.Position): SourceCode.Position {
    return SourceCode.Position(position.line, position.col, position.absolutePos)
}

fun diagnostic(diagnostic: ScriptDiagnostic): ScriptResult.Diagnostic {
    val loc = diagnostic.location
    val location = if (loc != null) {
        ScriptSourceCode.Location(position(loc.start), if (loc.end != null) position(loc.end!!) else null)
    } else {
        null
    }
    return ScriptResult.Diagnostic(
        diagnostic.code,
        diagnostic.message,
        ScriptResult.Severity.valueOf(diagnostic.severity.name),
        ScriptResult.Source(diagnostic.sourcePath, location),
        diagnostic.exception
    )
}

fun diagnostic(diagnostic: ScriptResult.Diagnostic): ScriptDiagnostic {
    val start = diagnostic.source.location?.start?.let { position(it) }
    val end = diagnostic.source.location?.end?.let { position(it) }
    return ScriptDiagnostic(
        diagnostic.code,
        diagnostic.message,
        ScriptDiagnostic.Severity.valueOf(diagnostic.severity.name),
        diagnostic.source.path,
        if (start != null) SourceCode.Location(start, end) else null,
        diagnostic.exception
    )
}

fun CompiledScript.scriptClassFQName(): String {
    return when (this) {
        is KJvmCompiledScript -> scriptClassFQName
        is ImportScript -> scriptClassFQName
        else -> error("Unsupported $this")
    }
}

fun CompiledScript.compilerOutputFiles(): Map<String, ByteArray> {
    return when (this) {
        is KJvmCompiledScript -> (getCompiledModule() as? KJvmCompiledModuleInMemoryImpl)?.compilerOutputFiles ?: HashMap()
        is ImportScript -> compilerOutputFiles
        else -> error("Unsupported $this")
    }
}

fun File.nonExists(): Boolean {
    return !exists()
}

fun Char.isValidIdentifier(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'
}

fun String.toClassIdentifier(): String {
    val charArray = toCharArray()
    charArray.map { if (it.isValidIdentifier()) this else "_" }
    return if (charArray[0].isDigit()) {
        charArrayOf('_', *charArray).concatToString()
    } else {
        charArray[0] = charArray[0].uppercaseChar()
        charArray.concatToString()
    }
}

fun checkImportScript(file: File?, script: CompiledScript, compilerOutputFiles: MutableMap<String, ByteArray>, imports: List<Pair<File, String>>): CompiledScript {
    val otherScripts = script.otherScripts.map { checkImportScript(null, it, compilerOutputFiles, imports) }
    return if (imports.any { i -> i.second == script.scriptClassFQName() }) {
        ImportScript(file, script.scriptClassFQName(), compilerOutputFiles, otherScripts)
    } else {
        script
    }
}