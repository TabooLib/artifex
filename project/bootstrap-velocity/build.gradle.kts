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
    install("common", "platform-velocity")
    install("module-porticus")
    options("skip-taboolib-relocate", "skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolib_version
}

dependencies {
    api(project(":project:common"))
    compileOnly("io.papermc:velocity:3.3.0:376")
}