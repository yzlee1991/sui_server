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
import com.lzy.sui.common.proxy.CommonRequestSocketHandle;
import com.lzy.sui.common.proxy.ResponseSocketHandle;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.server.Server;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class RequestFilter extends Filter {

	private Gson gson = new Gson();

	@Override
	public void handle(ProtocolEntity entity) {
		try {
			if (ProtocolEntity.Type.COMMONREQUEST.equals(entity.getType())) {
				System.out.println("RequestFilter  handling  " + entity);
				// 之后添加权限控制和文件缓存等
				if (ProtocolEntity.TARGER_SERVER.equals(entity.getTargetId())) {// 服务器服务请求
					List<String> base64Params = entity.getParams();
					// 还原参数对象
					Object[] objs = new Object[base64Params.size()];
					for (int i = 0; i < base64Params.size(); i++) {
						byte[] bytes = Base64.decode(base64Params.get(i));
						Object obj = CommonUtils.byteArraytoObject(bytes);
						objs[i] = obj;
					}
					// 获取代理对象
					Object target = Class.forName(entity.getClassName()).newInstance();
					String identityId = entity.getIdentityId();
					Socket targetSocket = Server.socketMap.get(identityId);
					ResponseSocketHandle handle = new ResponseSocketHandle(targetSocket, target,
							ProtocolEntity.TARGER_SERVER, identityId, entity.getMode());
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
				} else {// 转发请求
					Socket targetSocket = Server.socketMap.get(entity.getTargetId());
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(targetSocket.getOutputStream()));
					bw.write(gson.toJson(entity));
					bw.newLine();
					bw.flush();
				}
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
