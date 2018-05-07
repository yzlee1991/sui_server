package com.lzy.sui.server.listener;

import com.lzy.sui.common.inf.Listener;
import com.lzy.sui.common.model.ProtocolEntity;

public class HeartBeatListener implements Listener{

	@Override
	public void action(ProtocolEntity entity) {
		if(ProtocolEntity.Type.HEARTBEAT.equals(entity.getType())){
			System.out.println("--->>接收到心跳包："+entity.getIdentity());
		}
	}

}
