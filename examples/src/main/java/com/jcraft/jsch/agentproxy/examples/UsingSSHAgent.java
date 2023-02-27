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
import com.jcraft.jsch.agentproxy.Identity;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class UsingSSHAgent {
    public static void main(String[] arg) throws AgentProxyException {
        //USocketFactory udsf = new JUnixDomainSocketFactory();
        //USocketFactory udsf = new NCUSocketFactory();
        USocketFactory udsf = new JNAUSocketFactory();
        AgentProxy ap = new AgentProxy(new SSHAgentConnector(udsf));


        Identity[] identities = ap.getIdentities();

        System.out.println("count: " + identities.length);

        for (Identity identity : identities) {
            System.out.println("  comment: " +
                    new String(identity.getComment()));

            byte[] blob = identity.getBlob();
            System.out.print("  blob: ");
            for (byte b : blob) {
                System.out.print(Integer.toHexString(b & 0xff) + ":");
            }
            System.out.println();

            String data = "foo";
            byte[] signed = ap.sign(blob, data.getBytes());
            System.out.print("  sign: " + data + " -> ");
            for (byte b : signed) {
                System.out.print(Integer.toHexString(b & 0xff) + ":");
            }
            System.out.println();
        }
    }
}
