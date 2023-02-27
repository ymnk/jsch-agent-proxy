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

import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.TrileadAgentProxy;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrileadWithAgentProxy {
    public static void main(String[] arg) throws IOException {
        if (arg.length != 2) {
            System.err.println("Usage: test-ssh user@host command");
            System.exit(1);
        }

        int splitPoint = arg[0].indexOf("@");
        if (splitPoint <= 0) {
            System.err.println("Usage: test-ssh user@host command");
            System.exit(1);
        }

        String user = arg[0].substring(0, splitPoint);
        String host = arg[0].substring(splitPoint + 1);

        com.trilead.ssh2.auth.AgentProxy agentProxy = getAgentProxy();

        if (agentProxy == null) {
            System.err.println("ERROR: Unable to connect to SSH agent");
            System.exit(1);
        }

        Connection conn = new Connection(host);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithAgent(user, agentProxy);
        if (!isAuthenticated) {
            System.err.println("ERROR: Agent authentication not accepted");
            System.exit(1);
        }

        Session sess = conn.openSession();
        sess.execCommand(arg[1]);
        BufferedReader br = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
        Integer exitStatus = sess.getExitStatus();
        sess.close();
        conn.close();

        System.exit(exitStatus == null ? 1 : exitStatus);
    }

    static TrileadAgentProxy getAgentProxy() {
        try {
            Connector con = ConnectorFactory.getDefault().createConnector();
            return new TrileadAgentProxy(con);
        } catch (AgentProxyException e) {
            return null;
        }
    }
}
