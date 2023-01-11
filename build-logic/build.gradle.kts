plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
}

kotlin.jvmToolchain(17)

repositories {
    mavenCentral()
    gradlePluginPortal()
}
