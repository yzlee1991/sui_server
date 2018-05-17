package com.lzy.sui.server.push;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;

import com.google.gson.Gson;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.model.push.PushEvent;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.server.Server;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class PushServer {

	Gson gson=new Gson();
	
	private static volatile PushServer pushServer;

	private PushServer() {
	}

	public static PushServer newInstance() {
		if (pushServer == null) {
			synchronized (PushServer.class) {
				if (pushServer == null) {
					pushServer = new PushServer();
				}
			}
		}
		return pushServer;
	}

	public void push(PushEvent event) throws IOException {
		ProtocolEntity entity=new ProtocolEntity();
		entity.setType(ProtocolEntity.Type.PUSH);
		byte[] bytes=CommonUtils.ObjectToByteArray(event);
		String reply=Base64.encode(bytes);
		entity.setReply(reply);
		
		String json=gson.toJson(entity);
		for(String key:Server.socketMap.keySet()){
			Socket socket=Server.socketMap.get(key);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bw.write(json);
			bw.newLine();
			bw.flush();
			
		}
	}

}
