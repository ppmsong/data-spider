package com.crw.contact.dao;

import com.crw.contact.entity.AliBillingRecord;

public interface AliBillingRecordDao {
    int deleteByPrimaryKey(String id);

    int insert(AliBillingRecord record);

    int insertSelective(AliBillingRecord record);

    AliBillingRecord selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AliBillingRecord record);

    int updateByPrimaryKey(AliBillingRecord record);
}