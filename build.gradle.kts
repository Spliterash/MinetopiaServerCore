import io.papermc.paperweight.util.Git

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.papermc.paperweight.patcher") version "1.5.5"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    }
}
val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://jitpack.io")
    }
}

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(configurations.paperclip.name)
        }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:2.0.627.2")
    paperclip("io.papermc:paperclip:3.0.3")
}

val paperDir = layout.projectDirectory.dir("work/Purpur")
val initSubmodules by tasks.registering {
    outputs.upToDateWhen { false }
    doLast {
        Git(layout.projectDirectory)("submodule", "update", "--init").executeOut()
    }
}

paperweight {
    serverProject.set(project(":minetopia-server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    upstreams {
        register("paper") {
            upstreamDataTask {
                dependsOn(initSubmodules)
                projectDir.set(paperDir)
            }

            patchTasks {
                register("api") {
                    upstreamDir.set(paperDir.dir("Purpur-API"))
                    patchDir.set(layout.projectDirectory.dir("patches/api"))
                    outputDir.set(layout.projectDirectory.dir("minetopia-api"))
                }
                register("server") {
                    upstreamDir.set(paperDir.dir("Purpur-Server"))
                    patchDir.set(layout.projectDirectory.dir("patches/server"))
                    outputDir.set(layout.projectDirectory.dir("minetopia-server"))
                    importMcDev.set(true)
                }
            }
        }
    }
}
tasks.generateDevelopmentBundle {
    apiCoordinates.set("ru.minetopia.server:minetopia-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://repo.maven.apache.org/maven2/",
            paperMavenPublicUrl,
            "https://repo.purpurmc.org/snapshots",
        )
    )
}
publishing {
    publications.create<MavenPublication>("devBundle") {
        artifact(tasks.generateDevelopmentBundle) {
            artifactId = "dev-bundle"
        }
    }
}

allprojects {
    publishing {
        repositories {
            maven {
                name = "nexus"
                url = uri("https://repo.spliterash.ru/minetopia-server")
                credentials {
                    username = findProperty("SPLITERASH_NEXUS_USR")?.toString()
                    password = findProperty("SPLITERASH_NEXUS_PSW")?.toString()
                }
            }
        }
    }
}