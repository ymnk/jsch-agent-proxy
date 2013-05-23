package com.jcraft.jsch.agentproxy;

public class TrileadAgentIdentity implements com.trilead.ssh2.auth.AgentIdentity {

    AgentProxy proxy;
    Identity wrappedIdentity;
    String algName;

    public TrileadAgentIdentity(AgentProxy proxy, Identity wrappedIdentity) {
        byte[] blob = wrappedIdentity.getBlob();
        this.proxy = proxy;
        this.wrappedIdentity = wrappedIdentity;
        this.algName = new String((new Buffer(blob)).getString());
    }

    public String getAlgName() {
        return algName;
    }

    public byte[] getPublicKeyBlob() {
        return wrappedIdentity.getBlob();
    }

    public byte[] sign(byte[] bytes) {
        return proxy.sign(getPublicKeyBlob(), bytes);
    }


}
