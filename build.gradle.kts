plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "net.chamosmp"
version = "1.0-ALPHA"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.codemc.io/repository/creatorfromhell/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")

    // Faststats.dev
    maven {
        name = "faststatsReleases"
        url = uri("https://repo.faststats.dev/releases")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:5.0.1")
    implementation("net.kyori:adventure-text-minimessage:5.0.1")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.19")
    compileOnly("me.clip:placeholderapi:2.12.2")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("net.kyori:adventure-text-serializer-ansi:5.0.1")
    implementation("org.spongepowered:configurate-hocon:4.2.0")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    implementation("dev.faststats.metrics:bukkit:0.22.0")

}
tasks.shadowJar {
//    configurations = project.configurations.runtimeClasspath.map { setOf(it) }

    dependencies {
        // Only merge bStats into the final jar, no other dependencies
        include { it.moduleGroup == "org.bstats" }
    }

    // Relocate bStats into the plugin"s package to avoid conflicts with other
    // plugins using bStats
    relocate("org.bstats", project.group.toString())
    relocate("dev.faststats", "your.plugin.libs.faststats")
}
tasks {
    runServer {
        downloadPlugins {
            //url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
            //modrinth("VaultUnlocked", "2.19.0-release")
            url("https://github.com/EssentialsX/Essentials/releases/download/2.21.2/EssentialsX-2.21.2.jar")
            url("https://download.luckperms.net/1631/bukkit/loader/LuckPerms-Bukkit-5.5.42.jar")
        }
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin"s jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.11")
    }
    runPaper.folia.registerTask()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}