plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api(project(":jsch.agentproxy.core"))
    api("net.java.dev.jna:jna:4.1.0")
    api("net.java.dev.jna:jna-platform:5.12.1")
}
