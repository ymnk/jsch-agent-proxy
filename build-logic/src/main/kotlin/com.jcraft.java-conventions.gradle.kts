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
    repositories {
        maven(url = "https://maven.pkg.github.com/hfhbd/kobol") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
    }
    publications.configureEach {
        this as MavenPublication
        pom {
            name.set("app.softwork jsch agent proxy")
            url.set("https://github.com/hfhbd/kobol")
            developers {
                developer {
                    id.set("hfhbd")
                    name.set("Philip Wedemann")
                    email.set("mybztg+mavencentral@icloud.com")
                }
            }
            scm {
                connection.set("scm:git://github.com/hfhbd/kobol.git")
                developerConnection.set("scm:git://github.com/hfhbd/kobol.git")
                url.set("https://github.com/hfhbd/kobol")
            }
        }
    }
}
