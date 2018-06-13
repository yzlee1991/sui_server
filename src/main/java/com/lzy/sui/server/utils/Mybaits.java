package com.lzy.sui.server.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

//还是那个问题，jar包的配置文件读取
public class Mybaits {

	public static SqlSessionFactory sessionFactory=null;

	static {
		String resource = null;
		try {
			String str = Mybaits.class.getResource("").toURI().toString();
			if (str.startsWith("file")) {
				// mybatis的配置文件
				resource = "mybaits.xml";
			} else if (str.startsWith("jar")) {
				resource = "resources/mybaits.xml";
			}
			// 使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
			InputStream is = Mybaits.class.getClassLoader().getResourceAsStream(resource);
			System.out.println(is==null);
			// 构建sqlSession的工厂
			sessionFactory = new SqlSessionFactoryBuilder().build(is);
			
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
