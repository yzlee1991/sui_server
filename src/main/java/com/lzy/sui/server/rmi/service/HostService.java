package com.lzy.sui.server.rmi.service;

import java.util.ArrayList;
import java.util.List;

import com.lzy.sui.common.inf.HostInf;
import com.lzy.sui.common.model.push.HostEntity;
import com.lzy.sui.server.Server;

public class HostService implements HostInf{

	@Override
	public String a(int a) {
		System.out.println("=========***********=========");
//		try {
//			System.out.println("睡眠开始");
//			Thread.sleep(11000);
//			System.out.println("睡眠结束");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return "调用成功";
	}

	@Override
	public List<HostEntity> getOnlineHostEntity() {
		HostEntity hostEntity=Server.newInstance().getOwnHostEntity();
		String identityId=hostEntity.getIdentityId();
		List<HostEntity> list=new ArrayList<HostEntity>();
		for(Thread key:Server.newInstance().hostMap.keySet()){
			HostEntity he=Server.newInstance().hostMap.get(key);
			if(he.getIdentityId().equals(identityId)){
				continue;
			}
			list.add(he);
		}
		return list;
	}

}
