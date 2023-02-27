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
package com.jcraft.jsch.agentproxy

import com.jcraft.jsch.agentproxy.connector.*
import com.jcraft.jsch.agentproxy.usocket.*

abstract class ConnectorFactory {
    var preferredConnectors = "pageant,ssh-agent"
    var preferredUSocketFactories = "nc,jna"
    var uSocketPath: String? = null

    @Throws(AgentProxyException::class)
    fun createConnector(): Connector {
        val trials = ArrayList<Exception>()
        val _connectors = preferredConnectors.split(",".toRegex()).dropLastWhile { it.isEmpty() }
        for (i in _connectors.indices) {
            if (_connectors[i].trim { it <= ' ' } == "pageant") {
                if (PageantConnector.isConnectorAvailable()) {
                    try {
                        return PageantConnector()
                    } catch (e: AgentProxyException) {
                        trials.add(e)
                    }
                }
            } else if (_connectors[i].trim { it <= ' ' } == "ssh-agent") {
                if (!SSHAgentConnector.isConnectorAvailable(uSocketPath)) continue
                val _usocketFactories =
                    preferredUSocketFactories.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                for (j in _usocketFactories.indices) {
                    if (_usocketFactories[j].trim { it <= ' ' } == "nc") {
                        try {
                            val usf: USocketFactory = NCUSocketFactory()
                            return SSHAgentConnector(usf, uSocketPath)
                        } catch (e: AgentProxyException) {
                            trials.add(e)
                        }
                    } else if (_usocketFactories[j].trim { it <= ' ' } == "jna") {
                        try {
                            val usf: USocketFactory = JNAUSocketFactory()
                            return SSHAgentConnector(usf, uSocketPath)
                        } catch (e: AgentProxyException) {
                            trials.add(e)
                        }
                    }
                }
            }
        }
        var message = "connector is not available: "
        var foo = ""
        for (i in trials.indices) {
            message += foo + trials[i]
            foo = ","
        }
        throw AgentProxyException(message, null)
    }

    internal class Default : ConnectorFactory() {
        init {
            val osName = System.getProperty("os.name")
            if (osName != null) {
                if (!osName.startsWith("Windows")) {
                    preferredConnectors = "ssh-agent"
                }/*
        // NetBSD's nc must be available since Mac OS X Tiger.
        if(osName.startsWith("Mac")){
          setUSocketFactories("nc");
        }
        */
            }
        }
    }

    companion object {
        @JvmStatic
        fun getDefault(): ConnectorFactory = Default()
    }
}
