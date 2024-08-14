taboolib {
    version {
        skipKotlinRelocate = true
    }
//    exclude("taboolib")
    subproject = true
}

dependencies {
    compileOnly(project(":project:common")) { isTransitive = false }
//    compileOnly("public:MythicMobs:1.0.1")
//    compileOnly("public:MythicMobs5:5.0.4")
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}