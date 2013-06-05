package com.jcraft.jsch.agentproxy.simple;

import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class ConnectorFactory {
    /** Try the currently recommended methods to retrieve an agent connector.
     *
     * Use the most widely-supported / recommended / maintained mechanisms to
     * attempt to connect to a local SSH agent. This exists for ease-of-use;
     * developers seeking more fine-grained control (to reduce platform
     * dependencies, to use less-common connectors, to modify the search
     * order, etc) should leverage the desired connectors directly.
     *
     * @return A usable SSH agent connector, or null if none is available.
     */
    public static Connector getConnector() {
        try {
            Connector con = null;
            if(SSHAgentConnector.isConnectorAvailable()) {
                USocketFactory usf = new JNAUSocketFactory();
                con = new SSHAgentConnector(usf);
            } else if(PageantConnector.isConnectorAvailable()) {
                con = new PageantConnector();
            }
            return con;
        } catch(AgentProxyException e) {
            return null;
        }
    }
}
