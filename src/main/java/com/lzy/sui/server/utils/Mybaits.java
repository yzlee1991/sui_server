package com.lzy.sui.server.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class Mybaits {

	public final static SqlSessionFactory sessionFactory;

	static {
		// mybatis的配置文件
		String resource = "mybaits.xml";
		// 使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
		InputStream is = Mybaits.class.getClassLoader().getResourceAsStream(resource);
		// 构建sqlSession的工厂
		sessionFactory = new SqlSessionFactoryBuilder().build(is);
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
