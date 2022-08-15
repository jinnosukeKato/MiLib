import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mcVersion = "1.19.1"

plugins {
    kotlin("jvm") version "1.7.10"
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
