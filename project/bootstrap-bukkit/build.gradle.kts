taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
    api(project(":project:common"))
    api(project(":project:common-impl-default"))
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}