plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "com.github.lukesky19"
    version = "1.1.0"

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    tasks.jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    implementation(project(":Core")) // 1.21.4
    implementation(project(":1_21_3"))
    implementation(project(":1_21_1"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}