taboolib {
    version {
        skipKotlinRelocate = true
    }
    env {
        install("platform-velocity-impl")
    }
    subproject = true
}

dependencies {
    api(project(":project:common"))
    compileOnly("io.papermc:velocity:3.3.0:376")
}