plugins {
    kotlin("jvm") apply true
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("dev.s7a.gradle.minecraft.server") version "1.2.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

val mcVersion = "1.19.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
    implementation(project(":Library"))
}

bukkit {
    main = "$group.milibExamplePlugin.MiLibPlugin"
    name = "MiLib-Example-Plugin"
    version = rootProject.version.toString()
    apiVersion = "1.19"
    author = "InI"
}

task<dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask>("buildAndLaunchServer") {
    dependsOn("shadowJar") // ビルドタスク
    doFirst {
        copy {
            from(buildDir.resolve("libs/MiLibExamplePlugin-$version-all.jar")) // build/libs/example.jar
            into(buildDir.resolve("MinecraftPaperServer/plugins")) // build/MinecraftPaperServer/plugins
        }
    }

    jarUrl.set(dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl.Paper(mcVersion))
    jarName.set("server.jar")
    serverDirectory.set(buildDir.resolve("MinecraftPaperServer")) // build/MinecraftPaperServer
    nogui.set(true)
    agreeEula.set(true)
}
