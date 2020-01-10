package com.crw.contact.dao;

import com.crw.contact.entity.AliTotalssets;

public interface AliTotalssetsDao {
    int deleteByPrimaryKey(String id);

    int insert(AliTotalssets record);

    int insertSelective(AliTotalssets record);

    AliTotalssets selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AliTotalssets record);

    int updateByPrimaryKey(AliTotalssets record);
}