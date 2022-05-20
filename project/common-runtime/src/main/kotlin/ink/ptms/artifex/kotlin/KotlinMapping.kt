package ink.ptms.artifex.kotlin

import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptSourceCode
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode

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