plugins {
    kotlin("jvm") version "1.7.10" apply false
}

allprojects {
    version = "0.3"
    group = "com.github.jinnosukeKato"
}

subprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }
}
