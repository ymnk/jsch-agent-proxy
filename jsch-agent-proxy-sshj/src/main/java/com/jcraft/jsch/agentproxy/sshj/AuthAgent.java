/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2013 Olli Helenius All rights reserved.

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
package com.jcraft.jsch.agentproxy.sshj;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.Identity;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AbstractAuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AuthMethod for sshj authentication with an agent.
 */
public class AuthAgent extends AbstractAuthMethod {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** The AgentProxy instance that is used for signing */
    private final AgentProxy agentProxy;
    /** The identity from Agent */
    private final Identity identity;
    /** The identity's key algorithm */
    private final String algorithm;
    private final String comment;

    public AuthAgent(AgentProxy agentProxy, Identity identity) throws Buffer.BufferException {
        super("publickey");
        this.agentProxy = agentProxy;
        this.identity = identity;
        this.comment = new String(identity.getComment());
        this.algorithm = (new Buffer.PlainBuffer(identity.getBlob())).readString();
    }

    /** Internal use. */
    @Override
    public void handle(Message cmd, SSHPacket buf)
            throws UserAuthException, TransportException {
        if (cmd == Message.USERAUTH_60)
            sendSignedReq();
        else
            super.handle(cmd, buf);
    }

    protected SSHPacket putPubKey(SSHPacket reqBuf)
            throws UserAuthException {
        reqBuf
            .putString(algorithm)
            .putBytes(identity.getBlob()).getCompactData();
        return reqBuf;
    }

    private SSHPacket putSig(SSHPacket reqBuf)
            throws UserAuthException {
        final byte[] dataToSign = new Buffer.PlainBuffer()
                .putString(params.getTransport().getSessionID())
                .putBuffer(reqBuf) // & rest of the data for sig
                .getCompactData();

        reqBuf.putBytes(agentProxy.sign(identity.getBlob(), dataToSign));

        return reqBuf;
    }

    /**
     * Send SSH_MSG_USERAUTH_REQUEST containing the signature.
     *
     * @throws UserAuthException
     * @throws TransportException
     */
    private void sendSignedReq()
            throws UserAuthException, TransportException {
        params.getTransport().write(putSig(buildReq(true)));
    }

    /**
     * Builds SSH_MSG_USERAUTH_REQUEST packet.
     *
     * @param signed whether the request packet will contain signature
     *
     * @return the {@link SSHPacket} containing the request packet
     *
     * @throws UserAuthException
     */
    private SSHPacket buildReq(boolean signed)
            throws UserAuthException {
        log.debug("Attempting authentication using agent identity {}", comment);
        return putPubKey(super.buildReq().putBoolean(signed));
    }

    /** Builds a feeler request (sans signature). */
    @Override
    protected SSHPacket buildReq()
            throws UserAuthException {
        return buildReq(false);
    }
}
