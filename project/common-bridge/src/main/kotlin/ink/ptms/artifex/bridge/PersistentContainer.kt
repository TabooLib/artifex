package ink.ptms.artifex.bridge

import ink.ptms.artifex.script.Script
import taboolib.common.io.newFile
import taboolib.library.configuration.ConfigurationSection
import java.io.File

/**
 * 创建持久化储存容器
 */
fun Script.persistentContainer(type: Any, builder: PersistentContainer.() -> Unit): PersistentContainer {
    return PersistentContainer(this, type, builder).also {
        container().resource("@PersistentContainer:${it.container.path()}") { it.container.close() }
    }
}

/**
 * 创建持久化储存容器
 */
fun Script.persistentContainer(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    builder: PersistentContainer.() -> Unit,
): PersistentContainer {
    return PersistentContainer(this, host, port, user, password, database, builder).also {
        container().resource("@PersistentContainer:${it.container.path()}") { it.container.close() }
    }
}

class PersistentContainer {

    internal val container: Container
    internal val script: Script

    /**
     * 设置源
     * - 传入文件类型则为 SQLite 模式
     * - 传入 ConfigurationSection 则读取 SQL 配置
     */
    constructor(script: Script, type: Any, builder: PersistentContainer.() -> Unit) {
        this.script = script
        this.container = when (type) {
            is File -> {
                ContainerSQLite(type)
            }
            is String -> {
                ContainerSQLite(newFile(script.rootFile(), type))
            }
            is ConfigurationSection -> {
                ContainerSQL(
                    type.getString("host", "localhost")!!,
                    type.getInt("port"),
                    type.getString("user", "user")!!,
                    type.getString("password", "user")!!,
                    type.getString("database", "minecraft")!!
                )
            }
            else -> error("Unsupported source type: $type")
        }
        builder(this)
        this.container.init()
    }

    /**
     * 设置 SQL 源
     */
    constructor(script: Script, host: String, port: Int, user: String, password: String, database: String, builder: PersistentContainer.() -> Unit) {
        this.script = script
        this.container = ContainerSQL(host, port, user, password, database)
        builder(this)
        this.container.init()
    }

    /**
     * 注册标准容器
     */
    fun container(name: String, server: Boolean = false, builder: ContainerBuilder.() -> Unit) {
        container.addTable(name, player = !server, playerKey = true, data = ContainerBuilder(name).also(builder).dataList)
    }

    /**
     * 注册扁平容器
     */
    fun flatContainer(name: String, builder: ContainerBuilder.Flatten.() -> Unit = {}) {
        container.addTable(name, player = true, playerKey = false, data = ContainerBuilder.Flatten(name).also(builder).fixed().dataList)
    }

    /**
     * 获取控制器
     */
    operator fun get(name: String): ContainerOperator {
        return container.operator(name)
    }
}

