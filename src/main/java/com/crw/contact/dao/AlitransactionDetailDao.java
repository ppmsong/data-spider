package com.crw.contact.dao;

import com.crw.contact.entity.AlitransactionDetail;

public interface AlitransactionDetailDao {
    int deleteByPrimaryKey(String id);

    int insert(AlitransactionDetail record);

    int insertSelective(AlitransactionDetail record);

    AlitransactionDetail selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AlitransactionDetail record);

    int updateByPrimaryKey(AlitransactionDetail record);
}