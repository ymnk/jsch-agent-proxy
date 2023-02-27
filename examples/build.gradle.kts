plugins {
    id("com.jcraft.java-conventions")
}

dependencies {
    api(project(":jsch.agentproxy.core"))
    api(project(":jsch.agentproxy.jsch"))
    api(project(":jsch.agentproxy.pageant"))
    api(project(":jsch.agentproxy.sshagent"))
    api(project(":jsch.agentproxy.usocket-jna"))
    api(project(":jsch.agentproxy.usocket-nc"))
    api(project(":jsch.agentproxy.connector-factory"))
    api(project(":jsch.agentproxy.sshj"))
    api(project(":jsch.agentproxy.svnkit-trilead-ssh2"))
    api("com.trilead:trilead-ssh2:1.0.0-build222")
}
