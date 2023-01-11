rootProject.name = "jsch.agentproxy"

include(":jsch.agentproxy.jsch")
include(":jsch.agentproxy.sshj")
include(":jsch.agentproxy.pageant")
include(":jsch.agentproxy.sshagent")
include(":jsch.agentproxy.usocket-jna")
include(":jsch.agentproxy.usocket-nc")
include(":jsch.agentproxy.core")
include(":jsch.agentproxy.connector-factory")
include(":jsch.agentproxy.svnkit-trilead-ssh2")

project(":jsch.agentproxy.jsch").projectDir = file("jsch-agent-proxy-jsch")
project(":jsch.agentproxy.sshj").projectDir = file("jsch-agent-proxy-sshj")
project(":jsch.agentproxy.pageant").projectDir = file("jsch-agent-proxy-pageant")
project(":jsch.agentproxy.sshagent").projectDir = file("jsch-agent-proxy-sshagent")
project(":jsch.agentproxy.usocket-jna").projectDir = file("jsch-agent-proxy-usocket-jna")
project(":jsch.agentproxy.usocket-nc").projectDir = file("jsch-agent-proxy-usocket-nc")
project(":jsch.agentproxy.core").projectDir = file("jsch-agent-proxy-core")
project(":jsch.agentproxy.connector-factory").projectDir = file("jsch-agent-proxy-connector-factory")
project(":jsch.agentproxy.svnkit-trilead-ssh2").projectDir = file("jsch-agent-proxy-svnkit-trilead-ssh2")

include(":examples")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
