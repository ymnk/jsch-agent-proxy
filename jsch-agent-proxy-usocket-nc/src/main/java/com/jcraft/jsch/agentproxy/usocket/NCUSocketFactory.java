/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
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

package com.jcraft.jsch.agentproxy.usocket;

import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.USocketFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class NCUSocketFactory implements USocketFactory {

  public NCUSocketFactory() throws AgentProxyException {
    Process p = null;
    StringBuilder sb = new StringBuilder();
    try {
      p = Runtime.getRuntime().exec("nc -h");
      InputStream is = p.getErrorStream();
      byte[] buf = new byte[1024];
      int i = 0;
      while((i = is.read(buf, 0, buf.length))>0){
        sb.append(new String(buf, 0, i));
      }
    }
    catch(IOException e){
    }
    finally {
      try {
        if(p != null) {
          p.getErrorStream().close();
          p.getOutputStream().close();
          p.getInputStream().close();
          p.destroy();
        }
      }
      catch(IOException e){
      }
    }

    String result = sb.toString();
    if(result.indexOf("-U") == -1){
      throw new AgentProxyException("netcat does not support -U option.");
    }
  }

  public class MySocket extends Socket {
    private Process p;
    private InputStream is;
    private OutputStream os;

    public int readFull(byte[] buf, int s, int len) throws IOException {
      int _len = len; 
      while(len>0){
        int j = is.read(buf, s, len);
        if(j<=0)
          return -1;
        if(j>0){
          s+=j;
          len-=j;
        }
      }
      return _len;
    }

    public void write(byte[] buf, int s, int len) throws IOException {
      os.write(buf, s, len);
      os.flush();
    }

    MySocket(Process p) throws IOException {
      this.p = p;
      this.os = p.getOutputStream();
      this.is = p.getInputStream();
    }

    public void close() throws IOException {
      p.getErrorStream().close();
      p.getInputStream().close();
      p.destroy();
      os.close();
    }
  }

  public Socket open(String path) throws IOException {
    Process p = null;
    try {
      p = Runtime.getRuntime().exec("nc -U "+path);
    }
    catch (SecurityException e){
      throw new IOException(e.toString());
    }
    return new MySocket(p);
  }
}
