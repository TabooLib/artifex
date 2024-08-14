taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
    compileOnly(project(":project:common")) { isTransitive = false }
    compileOnly("net.md_5.bungee:BungeeCord:1")
}