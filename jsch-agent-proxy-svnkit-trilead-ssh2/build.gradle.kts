plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api("com.trilead:trilead-ssh2:1.0.0-build217")
    api(project(":jsch.agentproxy.core"))
}
