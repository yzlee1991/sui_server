package com.lzy.sui.server.rmi.service;

import com.lzy.sui.common.inf.HostInf;

public class HostService implements HostInf{

	@Override
	public String a(int a) {
		System.out.println("=========***********=========");
		return "调用成功";
	}

}
