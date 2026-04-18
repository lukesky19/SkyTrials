plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.lukesky19"
version = "2.2.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://maven.enginehub.org/repo/") {
        name = "EngineHub"
    }
    mavenLocal()
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    // SkyLib
    compileOnly("com.github.lukesky19:SkyLib:2.0.0.0")

    // Integration
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    // This allows usage of @apiNode in javadocs
    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:", "implNote:a:IMPL Note:")
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveClassifier.set("")
    }

    build {
        dependsOn(publishToMavenLocal)
        dependsOn(javadoc)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}