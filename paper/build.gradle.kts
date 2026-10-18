plugins {
    id("shared")

    alias(libs.plugins.shadowjar)
    alias(libs.plugins.run.paper)
}

dependencies {
    compileOnly(libs.paper)
    implementation(libs.sqdlib.paper)

    compileOnly(libs.strokk.paper)
    annotationProcessor(libs.strokk.paper.proccesor)

    compileOnly(libs.vault)
    compileOnly(libs.placeholderapi)

    compileOnly(libs.hikaricp)
    implementation(libs.faststats)

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
        minecraftVersion("26.3")
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
        relocate("dev.faststats", "com.sqdstudios.chamoorders.libs.faststats")
        relocate("com.sqdstudios.sqdlib", "com.sqdstudios.chamoorders.libs.sqdlib")
    }
}