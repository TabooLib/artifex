package ink.ptms.artifex.kotlin

import java.security.SecureClassLoader
import java.util.concurrent.ConcurrentHashMap

class KotlinScriptClassLoader(val entries: Map<String, ByteArray>, val relation: MutableList<ClassLoader>) :
    SecureClassLoader(KotlinScriptClassLoader::class.java.classLoader) {

    val runningClasses = ConcurrentHashMap<String, Class<*>>()

    init {
        ClassLoader.registerAsParallelCapable()
    }

    override fun findClass(name: String): Class<*> {
        var find = getClass(name)
        if (find != null) {
            return find
        }
        relation.filterIsInstance<KotlinScriptClassLoader>().forEach {
            find = it.getClass(name)
            if (find != null) {
                return find!!
            }
        }
        throw ClassNotFoundException(name)
    }

    fun getClass(name: String): Class<*>? {
        var result = runningClasses[name]
        if (result != null) {
            return result
        }
        val classBytes = entries["$name.class"]
        if (classBytes != null) {
            result = defineClass(name, classBytes, 0, classBytes.size)
        } else {
            return null
        }
        runningClasses[name] = result!!
        return result
    }
}