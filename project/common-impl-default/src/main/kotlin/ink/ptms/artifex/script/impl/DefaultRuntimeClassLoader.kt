package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.RuntimeClassLoader
import taboolib.common.TabooLib
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.security.CodeSource
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * @author 坏黑
 * @since 2022/5/16 00:56
 */
class DefaultRuntimeClassLoader(val files: List<File>) :
    URLClassLoader(files.map { it.toURI().toURL() }.toTypedArray(), DefaultRuntimeClassLoader::class.java.classLoader), RuntimeClassLoader {

    val jars = files.map { JarFile(it).let { jar -> ClassFile(jar, it.toURI().toURL(), jar.manifest) } }
    val runningClasses = ConcurrentHashMap<String, Class<*>>()

    val kotlinClassLoader = KotlinClassLoader()

    init {
        ClassLoader.registerAsParallelCapable()
    }

    override fun runningClasses(): Map<String, Class<*>> {
        return runningClasses
    }

    override fun findClass(name: String): Class<*> {
        var result = runningClasses[name]
        if (result != null) {
            return result
        }
        // 获取父加载器中可能存在的 Kotlin 环境
        try {
            result = super.findClass(name)
            runningClasses[name] = result
            return result
        } catch (_: ClassNotFoundException) {
        }
        val path = name.replace('.', '/').substringBeforeLast('.')
        val entry = getJarClass(path)
        if (entry != null) {
            val jar = entry.first.jarFile
            var classBytes: ByteArray
            try {
                jar.getInputStream(entry.second).use { classBytes = it.readBytes() }
            } catch (ex: IOException) {
                throw ClassNotFoundException(name, ex)
            }
            val dot = name.lastIndexOf('.')
            if (dot != -1) {
                val pkgName = name.substring(0, dot)
                if (getPackage(pkgName) == null) {
                    try {
                        if (entry.first.manifest != null) {
                            definePackage(pkgName, entry.first.manifest, entry.first.url)
                        } else {
                            definePackage(pkgName, null, null, null, null, null, null, null)
                        }
                    } catch (ex: IllegalArgumentException) {
                        checkNotNull(getPackage(pkgName)) { "Cannot find package $pkgName" }
                    }
                }
            }
            result = defineClass(name, classBytes, 0, classBytes.size, CodeSource(entry.first.url, entry.second.codeSigners))
        }
        if (result == null) {
            result = super.findClass(name)
        }
        runningClasses[name] = result!!
        return result
    }

    override fun close() {
        jars.forEach { it.jarFile.use { super.close() } }
    }

    fun getJarClass(path: String): Pair<ClassFile, JarEntry>? {
        jars.forEach {
            val entry = it.jarFile.getJarEntry(path)
            if (entry != null) {
                return it to entry
            }
        }
        return null
    }
}