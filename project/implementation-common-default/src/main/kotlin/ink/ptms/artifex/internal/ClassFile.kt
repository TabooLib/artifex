package ink.ptms.artifex.internal

import java.net.URL
import java.util.jar.JarFile
import java.util.jar.Manifest

/**
 * Artifex
 * ink.ptms.artifex.internal.ClassFile
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class ClassFile(val jarFile: JarFile, val url: URL, val manifest: Manifest?)