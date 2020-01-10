package com.crw.contact.dao;

import com.crw.contact.entity.AliUserInfo;

public interface AliUserInfoDao {
    int deleteByPrimaryKey(String id);

    int insert(AliUserInfo record);

    int insertSelective(AliUserInfo record);

    AliUserInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AliUserInfo record);

    int updateByPrimaryKey(AliUserInfo record);
}