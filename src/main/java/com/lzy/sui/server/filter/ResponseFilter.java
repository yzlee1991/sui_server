package com.lzy.sui.server.filter;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.AbstractSocketHandle;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.server.Server;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class ResponseFilter extends Filter{

	private Gson gson = new Gson();
	
	@Override
	public void handle(ProtocolEntity entity) {
		try{
			if(ProtocolEntity.Type.RESPONSE.equals(entity.getType())){
				System.out.println("ResponseFilter  handling "+entity);
				//转发，之后添加权限控制
				Socket targetSocket=Server.socketMap.get(entity.getTargetId());
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(targetSocket.getOutputStream()));
				bw.write(gson.toJson(entity));
				bw.newLine();
				bw.flush();
			}else{
				if(this.filter!=null){
					this.filter.handle(entity);
				}else{
					System.out.println("未知类型："+entity.getType());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}