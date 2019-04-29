package ai.yue.open.dwz.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.util.MapUtils;
import ai.yue.library.data.jdbc.client.DB;
import ai.yue.library.data.jdbc.constant.DBExpectedValueModeEnum;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.library.data.jdbc.vo.PageVO;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月21日
 */
@Repository
public class AdminDAO {
	
	@Autowired
	DB db;
	
	/**
	 * 插入数据
	 * @param paramJson
	 * @return 
	 */
	public Long insert(JSONObject paramJson) {
		return db.insert("admin", paramJson);
	}
	
	/**
	 * 更新-ById
	 * @param paramJson
	 */
	public void updateById(JSONObject paramJson) {
		db.updateById("admin", paramJson);
	}
	
	/**
	 * 更新-ByUsername
	 * @param paramJson
	 * @return 
	 */
	public Long updateByUsername(JSONObject paramJson) {
		String[] conditions = {"username"};
		return db.update("admin", paramJson, conditions);
	}
	
	/**
	 * 更新-密码
	 * @param paramJson
	 */
	public void updatePassword(JSONObject paramJson) {
		String sql = "UPDATE admin SET password = :password WHERE id = :id AND password = :oldPassword";
		int expectedValue = 1;
		db.update(sql, paramJson, expectedValue, DBExpectedValueModeEnum.等于);
	}
	
	/**
	 * 删除
	 * @param id
	 */
	public void delete(Long id) {
		db.delete("admin", id);
	}
	
	/**
	 * 确认admin是否存在
	 * @param username
	 * @return
	 */
	public boolean isAdminExist(String username) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("username", username);
		String sql = "SELECT id FROM admin WHERE username = :username";
		int size = db.queryForList(sql, paramJson).size();
		if (size <= 0) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 确认admin启用状态
	 * @param username
	 * @return
	 */
	public boolean isAdminStatus(String username) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("username", username);
		String sql = "SELECT admin_status FROM admin WHERE username = :username";
		return db.queryForJson(sql, paramJson).getBoolean("admin_status");
	}
	
	/**
	 * 获得可编辑信息
	 * @param id
	 * @return
	 */
	public JSONObject getEdit(Long id) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("id", id);
		String sql = "SELECT username, role_name FROM admin WHERE id = :id";
		return db.queryForJson(sql, paramJson);
	}
	
	/**
	 * 单个
	 * @param username
	 * @param password
	 * @return
	 */
	public JSONObject get(String username, String password) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("username", username);
		paramJson.put("password", password);
		String sql = "SELECT id, role_name FROM admin WHERE username = :username AND password = :password";
		return db.queryForJson(sql, paramJson);
	}
	
	/**
	 * 分页
	 * @param pageIPO
	 * @return
	 */
	public PageVO page(PageIPO pageIPO) {
		JSONObject conditions = pageIPO.getConditions();
		StringBuffer whereSql  = new StringBuffer();
		String[] keys = {"start_date", "end_date"};
		if (conditions.containsKey("start_date") || conditions.containsKey("end_date")) {
			if (MapUtils.isKeys(conditions, keys)) {
				whereSql .append("WHERE create_time BETWEEN :start_date AND :end_date");
			}else if (conditions.containsKey("start_date")) {
				whereSql .append("WHERE create_time LIKE CONCAT(:start_date, '%')");
			}else {
				whereSql .append("WHERE create_time LIKE CONCAT(:end_date, '%')");
			}
		}
		
		return db.pageWhere("he_campus_dwz", whereSql .toString(), pageIPO);
	}
	
}
