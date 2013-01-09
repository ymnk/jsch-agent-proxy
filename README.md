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
+ [UsingPageant.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/agentproxy/examples/UsingPageant.java)  
    This sample demonstrates how to get accesses to Pageant. 
 
		$ cd examples
		$ cd compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.UsingPageant"

+ [UsingSSHAent.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/agentproxy/examples/UsingSSHAgent.java)  
    This sample demonstrates how to get accesses to ssh-agent.  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.UsingSSHAgent"

+ [JSchWithAgentProxy.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/src/main/java/com/jcraft/agentproxy/examples/JSchWithAgentProxy.java)  
    This sample demonstrates how to integrate jsch-agent-proxy into JSch.  

		$ cd examples
		$ mvn compile
		$ mvn exec:java \
		  -Dexec.mainClass="com.jcraft.jsch.agentproxy.examples.UsingJSchWithAgentProxy" \
		  -Dexec.args="foo@bar.com"

## Dependencies
To work as a proxy to ssh-agent and Pageant,
the current implementation depends on the following software
 
+ JNA: https://github.com/twall/jna
+ junixsocket: http://code.google.com/p/junixsocket/

As for connections to ssh-agent, unix domain sockets must be
handled, and the current implementation has been using JNA or junixsocket.  Refer to following classes,
 
+ [com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-usocket-jna/src/main/java/com/jcraft/jsch/agentproxy/usocket/JNAUSocketFactory.java)
+ [com.jcraft.jsch.agentproxy.usocket.JUnixDomainSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-usocket-junixsocket/src/main/java/com/jcraft/jsch/agentproxy/usocket/JUnixDomainSocketFactory.java)

If you can use JNA, the later is not needed.

As for connections to Pageant, win32 APIs on Windows must be
handled, and JNA has been used in the current implementation.  Refer to the following class,

+ [com.jcraft.jsch.agentproxy.connector.PageantConnector](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-pageant/src/main/java/com/jcraft/jsch/agentproxy/connector/PageantConnector.java)


If you want to be free from JNA and junixsocket,
implement following interfaces without them,

+ [com.jcraft.jsch.agentproxy.Connector](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-core/src/main/java/com/jcraft/jsch/agentproxy/Connector.java)
+ [com.jcraft.jsch.agentproxy.USocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/jsch-agent-proxy-core/src/main/java/com/jcraft/jsch/agentproxy/USocketFactory.java)
