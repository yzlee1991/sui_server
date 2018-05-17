package com.lzy.sui.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.infimpl.Observer;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.model.push.HostEntity;
import com.lzy.sui.common.model.push.HostEvent;
import com.lzy.sui.common.utils.CommonUtils;
import com.lzy.sui.common.utils.MillisecondClock;
import com.lzy.sui.common.utils.RSAUtils;
import com.lzy.sui.server.filter.HeartbeatFilter;
import com.lzy.sui.server.push.PushServer;
import com.lzy.sui.server.filter.CommonRequestFilter;
import com.lzy.sui.server.rmi.RmiServer;
import com.lzy.sui.server.rmi.service.HostService;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Server {

	// 之后添加factory，要能在主线程捕获其他线程中的异常
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	});

	final MillisecondClock clock = new MillisecondClock(cachedThreadPool);

	private Gson gson = new Gson();

	private Filter headFilter = null;

	private Map<Socket, Long> heartBeatMap = new HashMap<Socket, Long>();

	private long timeout = 30000;

	private int delayTime = 100;

	private long lastTime = clock.now();

	private RmiServer rmiServer = RmiServer.newInstance();

	// 缓存连接服务器的所有socket
	public static Map<String, Socket> socketMap = new HashMap<String, Socket>();// key=identityId

	// 缓存socket对应的identityId
//	private static Map<Integer, String> identityMap = new HashMap<Integer, String>();// key=socket.hashCode

	public void start() {
		init();
		System.out.println("启动服务...");
		try {
			ServerSocket serverSocket = new ServerSocket(12345);
			while (true) {
				Socket socket = serverSocket.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				cachedThreadPool.execute(() -> {
					try {
						// 1.登陆
						String identityId = login(socket);
//						if (!flag) {
//							br.close();
//							bw.close();
//							socket.close();
//							return;
//						}
						heartBeatMap.put(socket, lastTime);
						// 2.登陆成功启动监听
						while (true) {
							String json = br.readLine();
							ProtocolEntity entity = gson.fromJson(json, ProtocolEntity.class);
							// observer.notifyListener(entity);
							entity.setIdentityId(identityId);
							headFilter.handle(entity);
							// 刷新心跳时间
							heartBeatMap.put(socket, clock.now());
						}

					} catch (Exception e) {
						e.printStackTrace();
						// 关闭连接
						try {
							br.close();
							bw.close();
							socket.close();
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// 退出线程
					}

				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 登陆，成功则返回身份id
	private String login(Socket socket) throws Exception {

		String identityId = UUID.randomUUID().toString();// 这步的key应该查数据库得到

		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		String json = br.readLine();
		ProtocolEntity entity = gson.fromJson(json, ProtocolEntity.class);
		ProtocolEntity.Identity identity = entity.getIdentity();
		
		//推送
		HostEntity hostEntity=new HostEntity();
		hostEntity.setIdentity(entity.getIdentity());
		hostEntity.setIdentityId(entity.getIdentityId());
		hostEntity.setName(entity.getSysUserName());
		
		// 1.登陆
		if (identity.equals(ProtocolEntity.Identity.USER)) {
			// RSA账号密码登陆 1.生成并发送公钥
			KeyPair keyPair = RSAUtils.genKeyPair();
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
			byte[] bytes = CommonUtils.ObjectToByteArray(publicKey);
			String base64PublicKey = Base64.encode(bytes);
			entity = new ProtocolEntity();
			entity.setReply(base64PublicKey);
			json = gson.toJson(entity);
			bw.write(json);
			bw.newLine();
			bw.flush();
			// 2.校验用户名密码
			json = br.readLine();
			entity = gson.fromJson(json, ProtocolEntity.class);
			// 模拟数据库操作
			String userName = RSAUtils.decrypt(entity.getParams().get(0), privateKey);
			String passWord = RSAUtils.decrypt(entity.getParams().get(1), privateKey);
			if (userName.equals("lzy") && passWord.equals("123456")) {
				socketMap.put(identityId, socket);
//				identityMap.put(socket.hashCode(), identityId);
				entity = new ProtocolEntity();
				entity.setReplyState(ProtocolEntity.ReplyState.SUCCESE);
				entity.setReply("登陆成功");
				json = gson.toJson(entity);
				bw.write(json);
				bw.newLine();
				bw.flush();
				System.out.println("登陆成功");
			} else {
				entity = new ProtocolEntity();
				entity.setReplyState(ProtocolEntity.ReplyState.ERROR);
				entity.setReply("登陆失败，用户名或密码错误");
				json = gson.toJson(entity);
				bw.write(json);
				bw.newLine();
				bw.flush();
				System.out.println("登陆失败");
				throw new RuntimeException("登陆失败，用户名或密码错误");
			}

		} else if (identity.equals(ProtocolEntity.Identity.CORPSE)) {
			// 1.查找数据库，是否被拉黑，被黑则发送指令退出程序

		} else {
			// 未知类型，抛异常
		}

		cachedThreadPool.execute(()->{
			try {
				HostEvent hostEvent=new HostEvent();
				hostEvent.setJson(gson.toJson(hostEntity));
				PushServer.newInstance().push(hostEvent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		return identityId;
	}

	// 初始化
	public void init() {
		System.out.println("初始化配置...");
		// 1.注册filter，之后改成反射可以扫描注册
		register();
		// 2.rmi服务注册
		rmiServer.autoRegister();
		// 3.开启心跳超时检测，之后看情况还quarz第三方类
		heartBeatCheck();
	}

	private void register() {
		System.out.println("处理器自动注册...");
		try {
			String scanPath = this.getClass().getResource("").getPath() + "filter";
			Filter filter = null;
			String packageName = this.getClass().getPackage().getName() + ".filter.";
			File file = new File(scanPath);
			for (File f : file.listFiles()) {
				String fileName = f.getName();
				String packageClassName = packageName + fileName.substring(0, fileName.indexOf("."));
				Filter newFilter = (Filter) Class.forName(packageClassName).newInstance();
				if (headFilter == null) {
					filter = newFilter;
					headFilter = filter;
				} else {
					filter.register(newFilter);
					filter = newFilter;
				}
				System.out.println("注册处理器：" + newFilter.getClass().getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("注册业务异常" + e.getMessage());
		}

	}

	private void heartBeatCheck() {
		System.out.println("开启心跳超时检测");
		cachedThreadPool.execute(() -> {
			try {
				while (true) {
					long currentTime = clock.now();
					if ((currentTime - lastTime) < timeout) {
						Thread.sleep(delayTime);
						continue;
					}
					for (Socket socket : heartBeatMap.keySet()) {
						long lastHeartBeatTime = heartBeatMap.get(socket);
						if ((currentTime - lastHeartBeatTime) > timeout) {
							// 超时socket
							System.out.println("超时Socket:" + socket);
							socket.close();
							heartBeatMap.remove(socket);// 迭代的时候操作容器有问题，之后修复
						}
					}
					lastTime = currentTime;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
	}

}
