taboolib {
    version {
        skipKotlinRelocate = true
    }
    subproject = true
}

repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/releases")}
}

dependencies {
    compileOnly(project(":project:common"))
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("ink.ptms.core:v11802:11802:mapped")
    compileOnly("ink.ptms.core:v11802:11802:universal")
}