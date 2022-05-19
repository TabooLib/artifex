package ink.ptms.artifex.script

/**
 * 脚本运行选项
 * @param property 运行参数
 * @param compileAsync 是否在异步编译脚本
 * @param useCache 是否在缓存文件存在时使用缓存，同时关闭后将不会产生缓存文件
 * @param logging 是否报告运行信息
 * @param report 是否报告运行结果
 */
open class EvaluationOption(val property: ScriptRuntimeProperty, val compileAsync: Boolean, val useCache: Boolean, val logging: Boolean, val report: Boolean) {

    class Builder {

        private var property = ScriptRuntimeProperty()
        private var compileAsync = true
        private var useCache = true
        private var logging = true
        private var report = true

        fun property(property: ScriptRuntimeProperty): Builder {
            this.property = property
            return this
        }

        fun compileAsync(compileAsync: Boolean): Builder {
            this.compileAsync = compileAsync
            return this
        }

        fun useCache(useCache: Boolean): Builder {
            this.useCache = useCache
            return this
        }

        fun logging(logging: Boolean): Builder {
            this.logging = logging
            return this
        }

        fun report(report: Boolean): Builder {
            this.report = report
            return this
        }

        fun create(): EvaluationOption {
            return EvaluationOption(property, compileAsync, useCache, logging, report)
        }
    }

    companion object {

        fun new(): Builder {
            return Builder()
        }
    }
}

/**
 * 脚本编译选项
 * @param report 是否报告运行结果
 */
open class CompilationOption(val report: Boolean) {

    class Builder {

        private var report = true

        fun report(report: Boolean): Builder {
            this.report = report
            return this
        }

        fun create(): CompilationOption {
            return CompilationOption(report)
        }
    }

    companion object {

        fun new(): Builder {
            return Builder()
        }
    }
}