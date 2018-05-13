package com.lzy.sui.server.rmi.service;

import com.lzy.sui.common.inf.HostInf;

public class HostService implements HostInf{

	@Override
	public String a(int a) {
		System.out.println("=========***********=========");
		try {
			System.out.println("睡眠开始");
			Thread.sleep(11000);
			System.out.println("睡眠结束");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "调用成功";
	}

}
