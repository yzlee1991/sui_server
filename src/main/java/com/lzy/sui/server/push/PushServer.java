package com.lzy.sui.server.push;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.model.push.PushEvent;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.common.utils.SocketUtils;
import com.lzy.sui.server.Server;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class PushServer {

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

	public void push(PushEvent event,String... filterIdentityIds) throws IOException {
		Set<String> filterSet=new HashSet<String>(Arrays.asList(filterIdentityIds));
		ProtocolEntity entity=new ProtocolEntity();
		entity.setType(ProtocolEntity.Type.PUSH);
		byte[] bytes=CommonUtils.ObjectToByteArray(event);
		String reply=Base64.encode(bytes);
		entity.setReply(reply);
		
		for(String key:Server.newInstance().socketMap.keySet()){
			if(filterSet.contains(key)){
				continue;
			}
			Socket socket=Server.newInstance().socketMap.get(key);
			SocketUtils.send(socket, entity);
		}
	}

}
