package com.lzy.sui.server.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.lzy.sui.server.db.entity.Auth;

public interface AuthMapper {

	@Select("select * from auth where name=#{name} and password=#{password}")
	Auth get(@Param("name") String name, @Param("password") String password);

}
