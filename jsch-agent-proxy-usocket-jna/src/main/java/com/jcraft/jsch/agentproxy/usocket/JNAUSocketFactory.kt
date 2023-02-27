/*
Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.

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
package com.jcraft.jsch.agentproxy.usocket

import com.jcraft.jsch.agentproxy.*
import com.sun.jna.*
import java.io.*

class JNAUSocketFactory : USocketFactory {
    interface CLibrary : Library {
        fun socket(domain: Int, type: Int, protocol: Int): Int
        fun fcntl(fd: Int, cmd: Int, vararg args: Any?): Int
        fun connect(sockfd: Int, addr: Pointer?, addrlen: Int): Int
        fun close(fd: Int): Int
        fun read(fd: Int, buf: ByteArray?, count: Int): Int
        fun write(fd: Int, buf: ByteArray?, count: Int): Int

        companion object {
            val INSTANCE: CLibrary = Native.load("c", CLibrary::class.java)
        }
    }

    class SockAddr : Structure() {
        var sun_family: Short = 0
        var sun_path: ByteArray = byteArrayOf()

        override fun getFieldOrder(): List<String> = listOf("sun_family", "sun_path")
    }

    class MySocket internal constructor(private val sock: Int) : USocketFactory.Socket() {
        override fun readFull(buf: ByteArray, s: Int, len: Int): Int {
            var _buf = buf
            var _len = len
            var _s = s
            while (_len > 0) {
                if (_s != 0) {
                    _buf = ByteArray(_len)
                }
                val i = CLibrary.INSTANCE.read(
                    sock, _buf, _len
                )
                if (i <= 0) {
                    return -1
                    // throw new IOException("failed to read usocket");
                }
                if (_s != 0) System.arraycopy(_buf, 0, buf, _s, i)
                _s += i
                _len -= i
            }
            return len
        }

        override fun write(buf: ByteArray, s: Int, len: Int) {
            var _buf = buf
            if (s != 0) {
                _buf = ByteArray(len)
                System.arraycopy(buf, s, _buf, 0, len)
            }
            CLibrary.INSTANCE.write(sock, _buf, len)
        }

        override fun close() {
            CLibrary.INSTANCE.close(sock)
        }
    }

    @Throws(IOException::class)
    override fun open(path: String): USocketFactory.Socket {
        val sock = CLibrary.INSTANCE.socket(
            1,  // AF_UNIX
            1,  // SOCK_STREAM
            0
        )
        if (sock < 0) {
            throw IOException("failed to allocate usocket")
        }
        var foo = CLibrary.INSTANCE.fcntl(sock, 2, 8)
        if (foo < 0) {
            CLibrary.INSTANCE.close(sock)
            throw IOException("failed to fctrl usocket: $foo")
        }
        val sockaddr = SockAddr()
        sockaddr.sun_family = 1
        sockaddr.sun_path = ByteArray(108)
        System.arraycopy(
            path.toByteArray(), 0,
            sockaddr.sun_path, 0,
            path.length
        )
        sockaddr.write()
        foo = CLibrary.INSTANCE.connect(sock, sockaddr.pointer, sockaddr.size())
        if (foo < 0) {
            throw IOException("failed to fctrl usocket: $foo")
        }
        return MySocket(sock)
    }
}
