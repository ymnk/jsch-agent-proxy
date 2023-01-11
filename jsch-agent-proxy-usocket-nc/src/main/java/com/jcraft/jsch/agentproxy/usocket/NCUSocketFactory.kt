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
package com.jcraft.jsch.agentproxy.usocket

import com.jcraft.jsch.agentproxy.*
import java.io.*

class NCUSocketFactory : USocketFactory {
    init {
        var p: Process? = null
        val sb = StringBuilder()
        try {
            p = Runtime.getRuntime().exec("nc -h")
            val `is` = p.errorStream
            val buf = ByteArray(1024)
            var i: Int
            while (`is`.read(buf, 0, buf.size).also { i = it } > 0) {
                sb.append(String(buf, 0, i))
            }
        } catch (_: IOException) {
        } finally {
            try {
                if (p != null) {
                    p.errorStream.close()
                    p.outputStream.close()
                    p.inputStream.close()
                    p.destroy()
                }
            } catch (_: IOException) {
            }
        }
        val result = sb.toString()
        if (result.indexOf("-U") == -1) {
            throw AgentProxyException("netcat does not support -U option.")
        }
    }

    inner class MySocket internal constructor(private val p: Process) : USocketFactory.Socket() {
        private val `is`: InputStream = p.inputStream
        private val os: OutputStream = p.outputStream

        @Throws(IOException::class)
        override fun readFull(buf: ByteArray, s: Int, len: Int): Int {
            var s = s
            var len = len
            val _len = len
            while (len > 0) {
                val j = `is`.read(buf, s, len)
                if (j <= 0) {
                    return -1
                }
                s += j
                len -= j
            }
            return _len
        }

        @Throws(IOException::class)
        override fun write(buf: ByteArray, s: Int, len: Int) {
            os.write(buf, s, len)
            os.flush()
        }

        @Throws(IOException::class)
        override fun close() {
            p.errorStream.close()
            p.inputStream.close()
            p.destroy()
            os.close()
        }
    }

    @Throws(IOException::class)
    override fun open(path: String): USocketFactory.Socket {
        val p: Process = try {
            Runtime.getRuntime().exec("nc -U $path")
        } catch (e: SecurityException) {
            throw IOException(e.toString())
        }
        return MySocket(p)
    }
}
