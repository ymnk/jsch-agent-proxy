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
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
