taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
    api(project(":project:common"))
    compileOnly("net.md_5.bungee:BungeeCord:1")
}