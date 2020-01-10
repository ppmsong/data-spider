package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.RechargeInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RechargeInfoDao {

    int deleteByCid(String cid);

    int insert(RechargeInfo record);

    int insertBatch(List<RechargeInfo> record);

    List<RechargeInfo> selectByCid(String cid);

}