package com.jcraft.jsch.agentproxy.examples

import com.jcraft.jsch.agentproxy.*
import com.jcraft.jsch.agentproxy.connector.*

fun main() {
    val ap = AgentProxy(PageantConnector())
    val identities = ap.identities
    println("count: " + identities.size)
    for (i in identities.indices) {
        println("  comment: " + String(identities[i].comment))
        val blob = identities[i].blob
        print("  blob: ")
        for (j in blob.indices) {
            print(Integer.toHexString(blob[j].toInt() and 0xff) + ":")
        }
        println("")
        val data = "foo"
        val signed = ap.sign(blob, data.toByteArray())
        print("  sign: $data -> ")
        for (j in signed.indices) {
            print(Integer.toHexString(signed[j].toInt() and 0xff) + ":")
        }
        println("")
    }
}
