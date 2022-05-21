package ink.ptms.artifex.controller

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

/**
 * Artifex
 * ink.ptms.artifex.controller.GameCommand
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
@CommandHeader(name = "artifex", aliases = ["art"], permission = "artifex.command")
object GameCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    /**
     * 运行脚本，通过命令运行的脚本在参数上仅支持基本类型推断
     * 若不存在编译文件则自动编译，反之则通过文件哈希值判断是否需要重新编译
     *
     * art invoke [文件名] -args:[变量名] [值] -args:[变量名] [值] -props:[变量名] [值]
     */
    @CommandBody
    val invoke = subCommand {
    }

    /**
     * 编译脚本，
     * 将脚本文件重新编译到 .build 目录中
     *
     * art compile [文件名]
     */
    @CommandBody
    val compile = subCommand {
    }

    /**
     * 释放脚本，若该脚本被引用则发送警告并阻止释放
     * （Script A is being referenced, please release [ C, D ] first）
     *
     * art release [文件名]
     */
    @CommandBody
    val release = subCommand {
    }

    /**
     * 重新编译运行脚本，若该脚本被引用则发送警告不会阻止重载
     * （Script A is being referenced, please release [ C, D ] later to update）
     *
     * art reload [文件名]
     */
    @CommandBody
    val reload = subCommand {
    }

    /**
     * 查看当前正在运行的脚本信息
     */
    @CommandBody
    val status = subCommand {
    }

    /**
     * 查看某脚本文件的详细信息
     *
     * art detail [文件名]
     */
    @CommandBody
    val detail = subCommand {
    }
}