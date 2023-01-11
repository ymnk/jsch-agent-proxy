plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api("com.jcraft:jsch:0.1.55")
    api(project(":jsch.agentproxy.core"))
}
