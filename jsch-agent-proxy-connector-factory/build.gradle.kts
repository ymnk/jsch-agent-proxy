plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api(project(":jsch.agentproxy.core"))
    api(project(":jsch.agentproxy.usocket-jna"))
    api(project(":jsch.agentproxy.usocket-nc"))
    api(project(":jsch.agentproxy.sshagent"))
    api(project(":jsch.agentproxy.pageant"))
}
