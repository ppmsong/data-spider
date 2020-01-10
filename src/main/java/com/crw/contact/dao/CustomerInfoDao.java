package com.crw.contact.dao;

import com.crw.contact.entity.CustomerInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerInfoDao {

    int deleteById(String id);

    int insert(CustomerInfo record);

    CustomerInfo selectById(String id);

}