package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.SmsInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsInfoDao {

    int deleteByCid(String id);

    int insert(SmsInfo record);

    int insertBatch(List<SmsInfo> record);

    List<SmsInfo> selectByCid(String cid);

}