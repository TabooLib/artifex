repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/releases")}
}

dependencies {
    compileOnly(project(":project:common"))
    compileOnly(project(":project:controller"))
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("net.md_5.bungee:BungeeCord:1")
}