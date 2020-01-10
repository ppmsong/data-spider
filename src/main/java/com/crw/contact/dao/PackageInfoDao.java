package com.crw.contact.dao;

import java.util.List;

import com.crw.contact.entity.PackageInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageInfoDao {

    int deleteByCid(String cid);

    int insert(PackageInfo record);

    int insertBatch(List<PackageInfo> record);

    List<PackageInfo> selectByCid(String cid);

}