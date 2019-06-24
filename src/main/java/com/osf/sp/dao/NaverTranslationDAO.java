package com.osf.sp.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class NaverTranslationDAO {
	
	@Resource
	private SqlSession ss;
	
	public List<Map<String,Object>> selectTranslationHisList(){
		return ss.selectList("com.osf.sp.mapper.NaverTranslationMapper.selectList");
	}
	
	public Map<String,Object> selectTranslationHisOne(Map<String,String> param){
		return ss.selectOne("com.osf.sp.mapper.NaverTranslationMapper.selectOne",param);
	}
	public Integer updateTranslationHis(Map<String,Object> param) {
		return ss.update("com.osf.sp.mapper.NaverTranslationMapper.updateCount",param);
	}
	public Integer insertTranslationHis(Map<String,String> param) {
		return ss.update("com.osf.sp.mapper.NaverTranslationMapper.insert",param);
	}
	
}
