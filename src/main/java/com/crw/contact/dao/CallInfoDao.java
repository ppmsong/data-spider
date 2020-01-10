package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.CallInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CallInfoDao {

    int deleteByCid(String cid);

    int insert(CallInfo record);

    int insertBatch(List<CallInfo> record);

    List<CallInfo> selectByCid(String cid);

}