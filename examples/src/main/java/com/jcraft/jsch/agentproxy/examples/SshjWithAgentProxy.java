/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jsch.agentproxy.examples;

import java.io.*;
import java.util.*;
import javax.swing.*;

import com.jcraft.jsch.agentproxy.*;
import com.jcraft.jsch.agentproxy.usocket.*;
import com.jcraft.jsch.agentproxy.connector.*;
import com.jcraft.jsch.agentproxy.sshj.*;
import net.schmizz.sshj.*;
import net.schmizz.sshj.connection.channel.direct.*;
import net.schmizz.sshj.userauth.method.AuthMethod;

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

    private static List<AuthMethod> getAuthMethods(AgentProxy agent) throws Exception {
        Identity[] identities = agent.getIdentities();
        List<AuthMethod> result = new ArrayList<AuthMethod>();
        for (Identity identity : identities) {
            result.add(new AuthAgent(agent, identity));
        }
        return result;
    }
}