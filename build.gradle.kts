plugins {
    java
    id("com.gradleup.shadow") version "8.3.1"
    id("io.papermc.paperweight.userdev") version "1.7.3"
}

group = "com.github.lukesky19"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.11-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    archiveClassifier.set("")
    relocate("org.spongepowered.configurate", "com.github.lukesky19.skytrials.libs.configurate")
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION