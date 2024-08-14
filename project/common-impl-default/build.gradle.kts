taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

dependencies {
    api(project(":project:common"))
    compileOnly("io.github:fast-classpath-scanner:3.1.13")
    // relocator
    compileOnly("me.lucko:jar-relocator:1.5")
}