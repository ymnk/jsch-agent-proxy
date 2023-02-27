/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */ /*
Copyright (c) 2013 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.jcraft.jsch.agentproxy.examples;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.sshj.AuthAgent;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.method.AuthMethod;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SshjWithAgentProxy {
    public static void main(String[] args) throws Exception {
        String target = getTarget(args);
        String username = target.substring(0, target.indexOf('@'));
        String hostname = target.substring(target.indexOf('@') + 1);

        AgentProxy agentProxy = getAgentProxy();
        if (agentProxy == null) {
            fail("Could not find or connect to an agent");
        }

        SSHClient client = new SSHClient();
        client.loadKnownHosts();
        client.connect(hostname);
        client.auth(username, getAuthMethods(agentProxy));

        Session session = client.startSession();

        Session.Command command = session.exec("ls -l");
        BufferedReader reader = new BufferedReader(new InputStreamReader(command.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        session.join();
        client.close();
    }

    private static String getTarget(String[] args) {
        if (args.length > 0) {
            return args[0];
        } else {
            return JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name") + "@localhost");
        }
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static AgentProxy getAgentProxy() {
        Connector connector = getAgentConnector();
        if (connector != null)
            return new AgentProxy(connector);
        return null;
    }

    private static Connector getAgentConnector() {
        try {
            return ConnectorFactory.getDefault().createConnector();
        } catch (AgentProxyException e) {
            System.err.println(e);
        }
        return null;
    }

    private static List<AuthMethod> getAuthMethods(AgentProxy agent) {
        Identity[] identities = agent.getIdentities();
        List<AuthMethod> result = new ArrayList<AuthMethod>();
        for (Identity identity : identities) {
            result.add(new AuthAgent(agent, identity));
        }
        return result;
    }
}
