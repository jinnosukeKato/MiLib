plugins {
    kotlin("jvm") apply true
    `maven-publish`
}

val mcVersion = "1.19.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
}
