package com.lzy.sui.server.filter;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.server.Server;

public class ExitFilter extends Filter {

	private Gson gson = new Gson();

	@Override
	public void handle(ProtocolEntity entity) {
		try {
			if (ProtocolEntity.Type.EXIT.equals(entity.getType())) {
				System.out.println("ExitFilter  handling  " + entity);
				Thread.currentThread().interrupt();
				Server.newInstance().outLine(entity.getIdentityId());
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
