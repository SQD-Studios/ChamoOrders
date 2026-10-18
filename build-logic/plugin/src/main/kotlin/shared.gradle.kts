plugins {
    id("java")
}

group = "com.sqdstudios.chamoorders"
version = "1.0.0"


repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.faststats.dev/releases")
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://maven.chamosmp.net/releases")
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}