/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */ /*
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
package com.jcraft.jsch.agentproxy.sshj

import com.jcraft.jsch.agentproxy.*
import net.schmizz.sshj.common.*
import net.schmizz.sshj.common.Buffer.*
import net.schmizz.sshj.transport.*
import net.schmizz.sshj.userauth.*
import net.schmizz.sshj.userauth.method.*
import org.slf4j.LoggerFactory

/**
 * An AuthMethod for sshj authentication with an agent.
 */
class AuthAgent(
    /** The AgentProxy instance that is used for signing  */
    private val agentProxy: AgentProxy,
    /** The identity from Agent  */
    private val identity: Identity
) : AbstractAuthMethod("publickey") {
    val log = LoggerFactory.getLogger(javaClass)

    /** The identity's key algorithm  */
    private val algorithm: String = PlainBuffer(identity.blob).readString()
    private val comment: String = String(identity.comment)

    /** Internal use.  */
    @Throws(UserAuthException::class, TransportException::class)
    override fun handle(cmd: Message, buf: SSHPacket) {
        if (cmd == Message.USERAUTH_60) sendSignedReq() else super.handle(cmd, buf)
    }

    @Throws(UserAuthException::class)
    fun putPubKey(reqBuf: SSHPacket): SSHPacket {
        reqBuf
            .putString(algorithm)
            .putBytes(identity.blob).compactData
        return reqBuf
    }

    @Throws(UserAuthException::class)
    private fun putSig(reqBuf: SSHPacket): SSHPacket {
        val dataToSign = PlainBuffer()
            .putString(params.transport.sessionID)
            .putBuffer(reqBuf) // & rest of the data for sig
            .compactData
        reqBuf.putBytes(agentProxy.sign(identity.blob, dataToSign))
        return reqBuf
    }

    /**
     * Send SSH_MSG_USERAUTH_REQUEST containing the signature.
     *
     * @throws UserAuthException
     * @throws TransportException
     */
    @Throws(UserAuthException::class, TransportException::class)
    private fun sendSignedReq() {
        params.transport.write(putSig(buildReq(true)))
    }

    /**
     * Builds SSH_MSG_USERAUTH_REQUEST packet.
     *
     * @param signed whether the request packet will contain signature
     *
     * @return the [SSHPacket] containing the request packet
     *
     * @throws UserAuthException
     */
    @Throws(UserAuthException::class)
    private fun buildReq(signed: Boolean): SSHPacket {
        log.debug("Attempting authentication using agent identity {}", comment)
        return putPubKey(super.buildReq().putBoolean(signed))
    }

    /** Builds a feeler request (sans signature).  */
    @Throws(UserAuthException::class)
    override fun buildReq(): SSHPacket {
        return buildReq(false)
    }
}
