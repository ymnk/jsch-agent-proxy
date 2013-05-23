package com.jcraft.jsch.agentproxy;

import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class TrileadAgentFactory {
    public static TrileadAgentProxy getAgentProxy() {
        try {
            Connector con;
            if(SSHAgentConnector.isConnectorAvailable()) {
                USocketFactory usf = new JNAUSocketFactory();
                con = new SSHAgentConnector(usf);
            } else if(PageantConnector.isConnectorAvailable()) {
                con = new PageantConnector();
            } else {
                return null;
            }
            return new TrileadAgentProxy(con);
        } catch(AgentProxyException e) {
            return null;
        }
    }
}
