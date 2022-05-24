package ink.ptms.artifex.bridge.module

import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.database.*
import java.io.File

class ContainerSQLite(val file: File) : Container {

    val host = HostSQLite(file)
    val hostTables = HashMap<String, Table<HostSQLite, SQLite>>()
    val dataSource = this.host.createDataSource(autoRelease = false)

    override fun addTable(name: String, player: Boolean, unique: Boolean, data: List<ContainerBuilder.Data>) {
        hostTables[name] = Table(name, host) {
            // 玩家容器
            if (player) {
                add("username") {
                    type(ColumnTypeSQLite.TEXT, 36) {
                        if (unique) {
                            options(ColumnOptionSQLite.PRIMARY_KEY)
                        }
                    }
                }
            }
            data.forEach {
                add(it.name) {
                    val type = when {
                        it.int || it.long -> ColumnTypeSQLite.INTEGER
                        it.double -> ColumnTypeSQLite.NUMERIC
                        else -> ColumnTypeSQLite.TEXT
                    }
                    type(type, if (type == ColumnTypeSQLite.TEXT) it.length else 0)
                }
            }
        }
    }

    override fun init() {
        hostTables.forEach { it.value.createTable(dataSource) }
    }

    override fun path(): String {
        return host.connectionUrl
    }

    override fun close() {
        dataSource.invokeMethod<Void>("close")
    }
}