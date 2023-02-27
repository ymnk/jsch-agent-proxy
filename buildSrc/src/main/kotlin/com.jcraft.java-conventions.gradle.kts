plugins {
    kotlin("jvm")
    `maven-publish`
}

kotlin.jvmToolchain(17)

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.register<MavenPublication>("maven") {
        from(components["java"])
    }
}
