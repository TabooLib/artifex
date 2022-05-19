package ink.ptms.artifex.controller

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
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
}