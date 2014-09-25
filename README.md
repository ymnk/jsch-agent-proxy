# jsch-agent-proxy
a proxy to ssh-agent and Pageant in Java.

## Description
**jsch-agent-proxy** is a proxy program to [OpenSSH](http://www.openssh.com/)'s [ssh-agent](http://en.wikipedia.org/wiki/Ssh-agent) and [Pageant](http://en.wikipedia.org/wiki/PuTTY#Applications)
included [Putty](http://www.chiark.greenend.org.uk/~sgtatham/putty/).  It will be easily integrated into [JSch](http://www.jcraft.com/jsch/), and users
will be allowed to use those programs in authentications.
This software has been developed for JSch, but it will be easily
applicable to other ssh2 implementations in Java.
This software is licensed under [BSD style license](https://github.com/ymnk/jsch-agent-proxy/blob/master/LICENSE.txt).


## Build from Source
    $ git clone git://github.com/ymnk/jsch-agent-proxy.git
    $ cd jsch-agent-proxy
    $ mvn package
    $ mvn install

## Examples
+ [UsingPageant.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/jsch/agentproxy/examples/UsingPageant.java)  
    This sample demonstrates how to get accesses to Pageant. 
 
		$ cd examples
		$ cd compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.UsingPageant"

+ [UsingSSHAgent.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/jsch/agentproxy/examples/UsingSSHAgent.java)  
    This sample demonstrates how to get accesses to ssh-agent.  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.UsingSSHAgent"

+ [JSchWithAgentProxy.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/jsch/agentproxy/examples/JSchWithAgentProxy.java)  
    This sample demonstrates how to integrate jsch-agent-proxy into JSch.  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.JSchWithAgentProxy" \
		  -Dexec.args="foo@bar.com"

+ [SshjWithAgentProxy.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/jsch/agentproxy/examples/SshjWithAgentProxy.java)  
    This sample demonstrates how to integrate jsch-agent-proxy into sshj.  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.SshjWithAgentProxy" \
		  -Dexec.args="foo@bar.com"

+ [TrileadWithAgentProxy.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/jsch/agentproxy/examples/TrileadWithAgentProxy.java)  
    This sample demonstrates how to integrate jsch-agent-proxy into Trilead SSH2 (SVNKit fork).  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.TrileadWithAgentProxy" \
		  -Dexec.args="foo@bar.com date"

## Dependencies
To work as a proxy to ssh-agent and Pageant,
the current implementation depends on the following software,
 
+ JNA: https://github.com/twall/jna licensed under the [GNU LGPL](https://github.com/twall/jna/blob/master/LICENSE) and the [Apache License 2.0](http://code.google.com/p/junixsocket/source/browse/trunk/junixsocket/LICENSE.txt)
+ junixsocket: http://code.google.com/p/junixsocket/ licensed under the [Apache License 2.0](http://code.google.com/p/junixsocket/source/browse/trunk/junixsocket/LICENSE.txt)
+ OpenBSD's netcat: http://www.openbsd.org/cgi-bin/cvsweb/src/usr.bin/nc/

As for connections to ssh-agent, unix domain sockets must be
handled, and the current implementation has been using JNA or junixsocket for that purpose.  Refer to following classes,
 
+ [com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-usocket-jna/src/main/java/com/jcraft/jsch/agentproxy/usocket/JNAUSocketFactory.java)
+ [com.jcraft.jsch.agentproxy.usocket.JUnixDomainSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-usocket-junixsocket/src/main/java/com/jcraft/jsch/agentproxy/usocket/JUnixDomainSocketFactory.java)
+ [com.jcraft.jsch.agentproxy.usocket.NCUSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-usocket-nc/src/main/java/com/jcraft/jsch/agentproxy/usocket/NCUSocketFactory.java)

NCUSocketFactory expects the external command nc(OpenBSD's netcat), but
you don't have to install other third party software. 

As for connections to Pageant, win32 APIs must be
handled, and JNA has been used in the current implementation for that purpose.  Refer to the following class,

+ [com.jcraft.jsch.agentproxy.connector.PageantConnector](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-pageant/src/main/java/com/jcraft/jsch/agentproxy/connector/PageantConnector.java)


If you want to be free from JNA and junixsocket,
implement following interfaces without them,

+ [com.jcraft.jsch.agentproxy.Connector](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-core/src/main/java/com/jcraft/jsch/agentproxy/Connector.java)
+ [com.jcraft.jsch.agentproxy.USocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-core/src/main/java/com/jcraft/jsch/agentproxy/USocketFactory.java)
