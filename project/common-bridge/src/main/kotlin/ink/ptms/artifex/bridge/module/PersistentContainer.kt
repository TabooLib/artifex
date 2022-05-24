package ink.ptms.artifex.bridge.module

import ink.ptms.artifex.script.Script
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
     * 注册统一数据容器
     */
    fun container(name: String, builder: ContainerBuilder.() -> Unit) {
        container.addTable(name, player = false, unique = false, data = ContainerBuilder(name).also(builder).dataList)
    }

    /**
     * 注册玩家数据容器
     */
    fun playerContainer(name: String, unique: Boolean = true, builder: ContainerBuilder.() -> Unit) {
        container.addTable(name, player = true, unique = unique, data = ContainerBuilder(name).also(builder).dataList)
    }
}

