rootProject.name = "Artifex"

include("plugin")
include("project:common")
// 实现
include("project:common-impl-default")
include("project:common-impl-project")
// 脚本运行环境（核心）
include("project:common-core")
// 脚本标准库
include("project:common-script-api")
include("project:common-script-api-bukkit")
include("project:common-script-api-bungee")
// 运行平台
include("project:bootstrap-bukkit")
include("project:bootstrap-bungee")
// jar 代理
include("project:jar-proxy-bukkit")
include("project:jar-proxy-bungee")