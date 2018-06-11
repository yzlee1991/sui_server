package com.lzy.sui.server.filter;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.utils.SocketUtils;
import com.lzy.sui.server.Server;
import com.lzy.sui.server.rmi.RmiServer;

public class RmiFilter extends Filter {


	@Override
	public void handle(ProtocolEntity entity) {
		try {
			if (ProtocolEntity.Type.RMI.equals(entity.getType())) {
				System.out.println("RMI  handling	" + entity);
				Socket targetSocket = Server.newInstance().socketMap.get(entity.getIdentityId());// 之后这个要在服务端维护
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(targetSocket.getOutputStream()));

				ProtocolEntity replyEntity = new ProtocolEntity();
				replyEntity.setType(ProtocolEntity.Type.RESPONSE);
				replyEntity.setConversationId(entity.getConversationId());
				String rmiName = entity.getRmiName();
				RmiServer.Cache rmiCache = RmiServer.newInstance().getCacheMap().get(rmiName);
				if (rmiCache == null) {
					replyEntity.setReplyState(ProtocolEntity.ReplyState.ERROR);
					replyEntity.setReply("该RMI服务不存在，服务名称：" + rmiName);
					SocketUtils.sendByNoBlock(targetSocket, replyEntity);
					return;
				}
				replyEntity.setReplyState(ProtocolEntity.ReplyState.SUCCESE);
				replyEntity.setReply(rmiCache.getInf().getName());
				SocketUtils.sendByNoBlock(targetSocket, replyEntity);
			} else {
				if (this.filter != null) {
					this.filter.handle(entity);
				} else {
					System.out.println("未知类型：" + entity.getType());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
