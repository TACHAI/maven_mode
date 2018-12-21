package com.bdcom.server.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.common.model.Barrage;

import com.alibaba.fastjson.JSONObject;




public interface BarrageService {
	
	/**
	 * 获取所有弹幕
	 * @return
	 */
	public List<Barrage> getAllBar();
	/**
	 * 通过时间模糊获取部分弹幕
	 * @param time
	 * @return
	 */
	public List<Barrage> getListBarByTime(Date time);
	/**
	 * 通过id获取弹幕
	 */
	public Barrage getBarById(String id);
	/**
	 * 模糊查询（待定）
	 */
	public List<Barrage> getListBarByLike(String content);
	/**
	 * 查询当前时间的弹幕数量
	 * @param time
	 * @return
	 */
	public int getBarrageCount(Timestamp time);

	public JSONObject getBarrageByImagesId(Integer imagesId);
}
