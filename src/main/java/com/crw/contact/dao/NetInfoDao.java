package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.NetInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NetInfoDao {

    int deleteByCid(String cid);

    int insert(NetInfo record);

    int insertBatch(List<NetInfo> record);

    List<NetInfo> selectByCid(String cid);

}