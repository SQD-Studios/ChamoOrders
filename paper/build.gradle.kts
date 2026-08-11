plugins {
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    compileOnly("net.strokkur.commands:annotations-paper:2.1.4")
    annotationProcessor("net.strokkur.commands:processor-paper:2.1.4")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.12.3")

    compileOnly("com.zaxxer:HikariCP:7.1.0")
    implementation("dev.faststats.metrics:bukkit:0.29.4")

    implementation(project(":api"))
}


tasks {
    runServer {
        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar") // Vault (The economy)
            modrinth("hXiIvTyT", "2.22.0") // EssentialsX (Vault Economy Provider)
            modrinth("lKEzGugV", "2.12.3") // PlaceholderAPI
            modrinth("Vebnzrzj", "v5.5.53-bukkit") // Luckperms
        }
        minecraftVersion("26.2")
    }
    runPaper.folia.registerTask()

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        configurations = project.configurations.runtimeClasspath.map { setOf(it) }
        relocate("dev.faststats", "net.chamosmp.chamoorders.libs.faststats")
    }
}