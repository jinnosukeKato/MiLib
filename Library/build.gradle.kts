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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            groupId = rootProject.group as String?
            artifactId = rootProject.name
            version = rootProject.version as String?
        }
    }
}
