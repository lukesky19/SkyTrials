plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.1.1")
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION