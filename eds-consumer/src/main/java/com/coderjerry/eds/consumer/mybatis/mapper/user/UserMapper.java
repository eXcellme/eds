package com.coderjerry.eds.consumer.mybatis.mapper.user;

import com.coderjerry.eds.consumer.model.User;

public interface UserMapper {
    User selectByPrimaryKey(long id);
}
