taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
    compileOnly(project(":project:common")) { isTransitive = false }
    compileOnly("io.papermc:velocity:3.3.0:376")
}