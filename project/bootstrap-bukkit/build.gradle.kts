val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib")
}

taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("坏黑")
        }
    }
    install("common", "platform-bukkit")
    install("module-nms", "module-nms-util", "module-ui", "module-ai", "module-ai", "module-navigation")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
}

dependencies {
    api(project(":project:common"))
    api(project(":project:common-impl-default"))
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}