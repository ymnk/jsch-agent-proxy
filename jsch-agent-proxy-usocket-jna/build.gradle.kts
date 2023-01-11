plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api(project(":jsch.agentproxy.core"))
    api("net.java.dev.jna:jna:5.12.1")
    api("net.java.dev.jna:jna-platform:5.12.1")
}
