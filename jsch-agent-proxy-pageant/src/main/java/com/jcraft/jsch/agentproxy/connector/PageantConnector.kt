/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */ /*
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
package com.jcraft.jsch.agentproxy.connector

import com.jcraft.jsch.agentproxy.*
import com.sun.jna.*
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinBase.*
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinNT.*
import com.sun.jna.win32.*

class PageantConnector : Connector {
    private val libU: User32
    private val libK: Kernel32

    init {
        try {
            libU = User32.INSTANCE
            libK = Kernel32.INSTANCE
        } catch (e: UnsatisfiedLinkError) {
            throw AgentProxyException(e.toString(), e)
        } catch (e: NoClassDefFoundError) {
            throw AgentProxyException(e.toString(), e)
        }
    }

    override val name: String = "pageant"
    override val isAvailable: Boolean = isConnectorAvailable()

    interface User32 : com.sun.jna.platform.win32.User32 {
        fun SendMessage(hWnd: HWND?, msg: Int, num1: WPARAM?, num2: ByteArray?): Long

        companion object {
            val INSTANCE: User32 = Native.load(
                "user32",
                User32::class.java,
                W32APIOptions.DEFAULT_OPTIONS
            )
        }
    }

    class COPYDATASTRUCT32 : Structure() {
        var dwData = 0
        var cbData = 0
        var lpData: Pointer? = null
        override fun getFieldOrder(): List<String> = listOf("dwData", "cbData", "lpData")
    }

    class COPYDATASTRUCT64 : Structure() {
        var dwData = 0
        var cbData: Long = 0
        var lpData: Pointer? = null
        override fun getFieldOrder(): List<String> = listOf("dwData", "cbData", "lpData")
    }

    @Throws(AgentProxyException::class)
    override fun query(buffer: Buffer) {
        val hwnd = libU.FindWindow("Pageant", "Pageant") 
            ?: throw AgentProxyException("Pageant is not runnning.", null)
        val mapname = String.format("PageantRequest%08x", libK.GetCurrentThreadId())

        // TODO
        val psa: SECURITY_ATTRIBUTES? = null
        val sharedFile: HANDLE = libK.CreateFileMapping(
            WinBase.INVALID_HANDLE_VALUE,
            psa,
            PAGE_READWRITE,
            0,
            8192,  // AGENT_MAX_MSGLEN
            mapname
        )
        val sharedMemory: Pointer = Kernel32.INSTANCE.MapViewOfFile(
            sharedFile,
            SECTION_MAP_WRITE,
            0, 0, 0
        )
        try {
            sharedMemory.write(0, buffer.buffer, 0, buffer.length)
            val rcode = if (Platform.is64Bit()) {
                val cds64 = COPYDATASTRUCT64()
                val data = install64(mapname, cds64)
                sendMessage(hwnd, data)
            } else {
                val cds32 = COPYDATASTRUCT32()
                val data = install32(mapname, cds32)
                sendMessage(hwnd, data)
            }
            buffer.rewind()
            if (rcode != 0L) {
                sharedMemory.read(0, buffer.buffer, 0, 4) // length
                val i = buffer.int
                buffer.rewind()
                buffer.checkFreeSize(i)
                sharedMemory.read(4, buffer.buffer, 0, i)
            }
        } finally {
            libK.UnmapViewOfFile(sharedMemory)
            libK.CloseHandle(sharedFile)
        }
    }

    private fun install32(mapname: String, cds: COPYDATASTRUCT32): ByteArray {
        cds.dwData = -0x7fb1af46 // AGENT_COPYDATA_ID
        cds.cbData = mapname.length + 1
        cds.lpData = Memory((mapname.length + 1).toLong())
        val foo = mapname.toByteArray()
        (cds.lpData as Memory).write(0, foo, 0, foo.size)
        (cds.lpData as Memory).setByte(foo.size.toLong(), 0.toByte())
        cds.write()
        val data = ByteArray(12)
        val cdsp = cds.pointer
        cdsp.read(0, data, 0, 12)
        return data
    }

    private fun install64(mapname: String, cds: COPYDATASTRUCT64): ByteArray {
        cds.dwData = -0x7fb1af46 // AGENT_COPYDATA_ID
        cds.cbData = (mapname.length + 1).toLong()
        cds.lpData = Memory((mapname.length + 1).toLong())
        val foo = mapname.toByteArray()
        (cds.lpData as Memory).write(0, foo, 0, foo.size)
        (cds.lpData as Memory).setByte(foo.size.toLong(), 0.toByte())
        cds.write()
        val data = ByteArray(24)
        val cdsp = cds.pointer
        cdsp.read(0, data, 0, 24)
        return data
    }

    fun sendMessage(hwnd: HWND?, data: ByteArray?): Long = libU.SendMessage(
        hwnd,
        0x004A,  //WM_COPYDATA
        null,
        data
    )
    
    companion object {
        @JvmStatic
        public fun isConnectorAvailable() = System.getProperty("os.name").startsWith("Windows")
    }
}
