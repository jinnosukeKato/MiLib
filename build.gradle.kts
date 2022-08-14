import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask

val mcVersion = "1.19.1"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("dev.s7a.gradle.minecraft.server") version "1.2.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "com.github.jinnosukeKato"
version = "0.3"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
}

bukkit {
    main = "com.github.jinnosukeKato.milib.example.MiLibPlugin"
    name = "MiLib-Example-Plugin"
    version = getVersion().toString()
    apiVersion = "1.19"
    author = "InI"
}

task<LaunchMinecraftServerTask>("buildAndLaunchServer") {
    dependsOn("shadowJar") // ビルドタスク
    doFirst {
        copy {
            from(buildDir.resolve("libs/MiLib-$version-all.jar")) // build/libs/example.jar
            into(buildDir.resolve("MinecraftPaperServer/plugins")) // build/MinecraftPaperServer/plugins
        }
    }

    jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper(mcVersion))
    jarName.set("server.jar")
    serverDirectory.set(buildDir.resolve("MinecraftPaperServer")) // build/MinecraftPaperServer
    nogui.set(true)
    agreeEula.set(true)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
