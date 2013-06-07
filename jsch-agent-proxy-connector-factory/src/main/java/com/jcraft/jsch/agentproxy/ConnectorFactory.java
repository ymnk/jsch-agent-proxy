/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
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

package com.jcraft.jsch.agentproxy;

import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.usocket.NCUSocketFactory;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import java.util.ArrayList;

public abstract class ConnectorFactory {

  protected String connectors = "pageant,ssh-agent";
  protected String usocketFactories = "nc,jna";

  public void setPreferredConnectors(String connectors) {
    this.connectors = connectors;
  }

  public String getPreferredConnectors() {
    return connectors;
  }

  public void setPreferredUSocketFactories(String usocketFactories){
    this.usocketFactories = usocketFactories;
  }

  public String getPreferredUSocketFactories() {
    return usocketFactories;
  }

  public Connector createConnector() throws AgentProxyException {
    ArrayList<String> trials = new ArrayList<String>();

    String[] _connectors = connectors.split(",");
    for(int i = 0; i < _connectors.length; i++) {
      if(_connectors[i].trim().equals("pageant")) {
        if(PageantConnector.isConnectorAvailable()) {
          try {
            return new PageantConnector();
          }
          catch(AgentProxyException e){
            trials.add("pageant");
          }
        }
      }
      else if(_connectors[i].trim().equals("ssh-agent")) {
        if(!SSHAgentConnector.isConnectorAvailable())
          continue;

        String[] _usocketFactories = usocketFactories.split(",");
        for(int j = 0; j < _usocketFactories.length; j++) {
          if(_usocketFactories[j].trim().equals("nc")) {
            try {
              USocketFactory usf = new NCUSocketFactory();
              return new SSHAgentConnector(usf);
            }
            catch(AgentProxyException e){
              trials.add("ssh-agent:nc");
            }
          }
          else if(_usocketFactories[j].trim().equals("jna")) {
            try {
              USocketFactory usf = new JNAUSocketFactory();
              return new SSHAgentConnector(usf);
            }
            catch(AgentProxyException e){
              trials.add("ssh-agent:jna");
            }
          }
        }
      }
    }

    String message = "connector is not available: ";
    String foo = "";
    for(int i = 0; i < trials.size(); i++){ 
      message += (foo + trials.get(i));
      foo = ",";
    }
    throw new AgentProxyException(message);
  }

  public static ConnectorFactory getDefault() {
    return new Default();
  }

  static class Default extends ConnectorFactory {
    Default() {
      String osName = System.getProperty("os.name");
      if(osName != null){
        if(!osName.startsWith("Windows")){
          setPreferredConnectors("ssh-agent");
        }
        /*
        // NetBSD's nc must be available since Mac OS X Tiger.
        if(osName.startsWith("Mac")){
          setUSocketFactories("nc");
        }
        */
      }
    }
  }
}
