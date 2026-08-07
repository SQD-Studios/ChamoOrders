plugins {
    id("java")

}

allprojects {
    group = "net.chamosmp.ChamoOrders"
    version = "1.0.0"

    apply(plugin = "java")

    repositories {
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        } // PaperAPI
        maven("https://jitpack.io") // VaultAPI
        maven("https://repo.extendedclip.com/releases/") // PlaceholderAPI
        mavenCentral() // HikariCP
        maven("https://repo.faststats.dev/releases") // Faststats
        maven {
            name = "eldonexus"
            url = uri("https://eldonexus.de/repository/maven-public/")
        } // StrokkCommands
    }
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }
}