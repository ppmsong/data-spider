package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.BillInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillInfoDao {

    int deleteByCid(String cid);

    int insert(BillInfo record);

    int insertBatch(List<BillInfo> record);

    List<BillInfo> selectByCid(String cid);

}