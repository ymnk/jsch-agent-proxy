package com.jcraft.jsch.agentproxy.examples;

import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.TrileadAgentProxy;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import com.trilead.ssh2.auth.AgentProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrileadWithAgentProxy {
    public static void main(String[] arg) throws IOException, AgentProxyException {
        if(arg.length != 2) {
            System.err.println("Usage: test-ssh user@host command");
            System.exit(1);
        }

        int splitPoint = arg[0].indexOf("@");
        if(splitPoint <= 0) {
            System.err.println("Usage: test-ssh user@host command");
            System.exit(1);
        }

        String user = arg[0].substring(0, splitPoint);
        String host = arg[0].substring(splitPoint + 1);

        Connector con;
        if(SSHAgentConnector.isConnectorAvailable()) {
            USocketFactory usf = new JNAUSocketFactory();
            con = new SSHAgentConnector(usf);
        } else if(PageantConnector.isConnectorAvailable()) {
            con = new PageantConnector();
        } else {
            System.err.println("ERROR: Unable to connect to SSH agent");
            System.exit(1);
            return; /* compiler does not recognize System.exit as terminating */
        }
        AgentProxy agentProxy = new TrileadAgentProxy(con);

        Connection conn = new Connection(host);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithAgent(user, agentProxy);
        if(isAuthenticated == false) {
            System.err.println("ERROR: Agent authentication not accepted");
            System.exit(1);
        }

        Session sess = conn.openSession();
        sess.execCommand(arg[1]);
        BufferedReader br = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
        while(true) {
            String line = br.readLine();
            if(line == null) {
                break;
            }
            System.out.println(line);
        }
        Integer exitStatus = sess.getExitStatus();
        sess.close();
        conn.close();

        System.exit(exitStatus == null ? 1 : exitStatus.intValue());
    }
}
