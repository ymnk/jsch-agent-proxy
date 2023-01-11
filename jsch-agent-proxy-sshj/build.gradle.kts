plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api("com.hierynomus:sshj:0.34.0")
    api(project(":jsch.agentproxy.core"))
    api("org.slf4j:slf4j-api:2.0.6")
}
