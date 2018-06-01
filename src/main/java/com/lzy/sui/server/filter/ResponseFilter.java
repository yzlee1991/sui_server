package com.lzy.sui.server.filter;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.utils.SocketUtils;
import com.lzy.sui.server.Server;

public class ResponseFilter extends Filter{

	@Override
	public void handle(ProtocolEntity entity) {
		try{
			if(ProtocolEntity.Type.RESPONSE.equals(entity.getType())){
				System.out.println("ResponseFilter  handling "+entity);
				//转发，之后添加权限控制
				Socket targetSocket=Server.newInstance().socketMap.get(entity.getTargetId());
				SocketUtils.sendByNoBlock(targetSocket, entity);
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
