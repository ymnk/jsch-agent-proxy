package com.jcraft.jsch.agentproxy.connector;

import java.io.IOException;
import java.io.InputStream;

public class SSHAgentProcess{
  private String socketPath;
  private Integer pid;

  public String getSocketPath() {
    return socketPath;
  }

  public void setSocketPath(String socketPath) {
    this.socketPath=socketPath;
  }

  public SSHAgentProcess() throws IOException{
    this("/usr/bin/ssh-agent");
  }

  public SSHAgentProcess(String sshAgentPath) throws IOException{
    ProcessBuilder builder=new ProcessBuilder(sshAgentPath);
    builder.redirectErrorStream(true);

    Process process=builder.start();
    InputStream is=process.getInputStream();

    byte[] buff=new byte[2048];
    is.read(buff);
    is.close();

    String agentOutput=new String(buff);
    String[] splitAgentOutput=agentOutput.split(";");

    String[] splitSocketPath=splitAgentOutput[0].split("=");
    socketPath=splitSocketPath[1];

    String[] splitAgentPid=splitAgentOutput[2].split("=");
    pid=Integer.valueOf(splitAgentPid[1]);
  }

  public void stopAgent() throws IOException {
    Runtime.getRuntime().exec("kill "+this.pid);
  }
}
