package com.lzy.sui.server.rmi;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.inf.Rmiable;

public class RmiServer {

	public class Cache {
		private Class inf;
		
		private Object obj;
		
		public Class getInf() {
			return inf;
		}
		public void setInf(Class inf) {
			this.inf = inf;
		}
		public Object getObj() {
			return obj;
		}
		public void setObj(Object obj) {
			this.obj = obj;
		}
		
		
	}

	private static volatile RmiServer rmiServer;

	private RmiServer() {
	}

	public static RmiServer newInstance() {
		if (rmiServer == null) {
			synchronized (RmiServer.class) {
				if (rmiServer == null) {
					rmiServer = new RmiServer();
				}
			}
		}
		return rmiServer;
	}

	private Map<String,Cache> cacheMap=new HashMap<String, Cache>();//key=rmiName
	
	/**
	 * 自动注册RMI服务，服务名称默认为实现的接口名称，不支持多个实现相同接口的服务，若需要则手动注册
	 */
	public void autoRegister() {
		System.out.println("RMI远程服务自动注册...");
		try {
			String scanPath = this.getClass().getResource("").getPath() + "service";
			Filter filter = null;
			String packageName = this.getClass().getPackage().getName() + ".service.";
			File file = new File(scanPath);
			// 注册时要确定是哪个实现的接口，通过标记接口确定
			for (File f : file.listFiles()) {
				String fileName = f.getName();
				String packageClassName = packageName + fileName.substring(0, fileName.indexOf("."));
				Class serviceClass = Class.forName(packageClassName);
				Class infClass = getRmiInterface(serviceClass);
				bind(infClass.getName(), serviceClass.newInstance());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("RMI服务注册异常" + e.getMessage());
		}

	}

	private Class getRmiInterface(Class clazz) {
		for (Class infClass : clazz.getInterfaces()) {
			for (Class rmiClass : infClass.getInterfaces()) {
				if (Rmiable.class.getName().equals(rmiClass.getName())) {
					return infClass;
				}
			}
		}
		throw new RuntimeException(clazz.getName() + " 实现的接口没有继承Rmiable标记接口");
	}

	//应该在这里判断接口的的方法没有问题（看情况）
	public void bind(String rmiName, Object obj) {
		if (cacheMap.containsKey(rmiName)) {
			throw new RuntimeException(rmiName + " 服务已注册");
		}
		System.out.println("注册RMI远程服务：" + rmiName+" - "+obj.getClass().getName());
		Class infClass = getRmiInterface(obj.getClass());
		Cache cache=new Cache();
		cache.setInf(infClass);
		cache.setObj(obj);
		cacheMap.put(rmiName, cache);
	}

	public Map<String, Cache> getCacheMap() {
		return cacheMap;
	}

	public void setCacheMap(Map<String, Cache> cacheMap) {
		this.cacheMap = cacheMap;
	}

	public static void main(String[] args) {
		new RmiServer().autoRegister();
	}

}
