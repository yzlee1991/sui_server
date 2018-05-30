package com.lzy.sui.server.filter;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.List;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.proxy.ResponseSocketHandle;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.server.Server;
import com.lzy.sui.server.rmi.RmiServer;
import com.lzy.sui.server.rmi.service.HostService;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class RmiRequestFilter extends Filter {

	private Gson gson = new Gson();

	private RmiServer rmiServer = RmiServer.newInstance();

	@Override
	public void handle(ProtocolEntity entity) {
		try {
			if (ProtocolEntity.Type.RMIREQUEST.equals(entity.getType())) {
				System.out.println("RmiRequestFilter  handling  " + entity);
				Socket targetSocket = Server.newInstance().socketMap.get(entity.getIdentityId());// 之后这个要在服务端维护
				List<String> base64Params = entity.getParams();
				// 还原参数对象
				Object[] objs = new Object[base64Params.size()];
				for (int i = 0; i < base64Params.size(); i++) {
					byte[] bytes = Base64.decode(base64Params.get(i));
					Object obj = CommonUtils.byteArraytoObject(bytes);
					objs[i] = obj;
				}
				// 获取代理对象
				Object target = rmiServer.getCacheMap().get(entity.getRmiName()).getObj();
				String identityId = entity.getIdentityId();
				ResponseSocketHandle handle = new ResponseSocketHandle(targetSocket, target, identityId);
				handle.setConversationId(entity.getConversationId());
				Object proxy = Proxy.newProxyInstance(target.getClass().getClassLoader(),
						target.getClass().getInterfaces(), handle);

				// 获取对应的方法（注意，哪个对象调用则用哪个对象的class获取）
				List<String> paramsType = entity.getParamsType();
				Method[] methods = proxy.getClass().getDeclaredMethods();
				Method method = null;
				for (Method m : methods) {
					if (!m.getName().equals(entity.getMethodName())) {
						continue;
					}
					Class<?>[] types = m.getParameterTypes();
					if (paramsType.size() != types.length) {
						continue;
					}
					if (types.length == 0) {
						method = m;
					} else {
						for (int i = 0; i < paramsType.size(); i++) {
							if (!paramsType.get(i).equals(types[i].getTypeName())) {
								break;
							}
							method = m;
						}
					}
				}
				// 调用 对应的方法
				method.invoke(proxy, objs);
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
