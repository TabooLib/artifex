package ink.ptms.artifex.kotlin

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = KotlinCompilationConfiguration::class,
    evaluationConfiguration = KotlinEvaluationConfiguration::class
)
abstract class KotlinScript(val args: Array<String>)