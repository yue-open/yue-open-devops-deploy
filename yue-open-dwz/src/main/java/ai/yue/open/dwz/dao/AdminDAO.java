package ai.yue.open.dwz.dao;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.util.MapUtils;
import ai.yue.library.data.jdbc.constant.DbExpectedValueModeEnum;
import ai.yue.library.data.jdbc.dao.AbstractRepository;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.library.data.jdbc.vo.PageTVO;
import ai.yue.open.dwz.dataobject.AdminDO;

/**
 * @author	ylyue
 * @since	2018年3月21日
 */
@Repository
public class AdminDAO extends AbstractRepository<AdminDO> {
	
	@Override
	protected String tableName() {
		return "dwz_admin";
	}
	
	/**
	 * 更新-ByUsername
	 * @param paramJson
	 * @return 
	 */
	public Long updateByUsername(JSONObject paramJson) {
		String[] conditions = {"username"};
		return db.update(tableName, paramJson, conditions);
	}
	
	/**
	 * 更新-密码
	 * @param paramJson
	 */
	public void updatePassword(JSONObject paramJson) {
		String sql = "UPDATE dwz_admin SET password = :password WHERE id = :id AND password = :oldPassword";
		int expectedValue = 1;
		db.update(sql, paramJson, expectedValue, DbExpectedValueModeEnum.EQUAL);
	}
	
	/**
	 * 确认admin是否存在
	 * @param username
	 * @return
	 */
	public boolean isAdminExist(String username) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("username", username);
		String sql = "SELECT id FROM dwz_admin WHERE username = :username";
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
		String sql = "SELECT admin_status FROM dwz_admin WHERE username = :username";
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
		String sql = "SELECT username, role_name FROM dwz_admin WHERE id = :id";
		return db.queryForJson(sql, paramJson);
	}
	
	/**
	 * 单个
	 * @param username
	 * @param password
	 * @return
	 */
	public AdminDO get(String username, String password) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("username", username);
		paramJson.put("password", password);
		String sql = "SELECT id, role_name FROM dwz_admin WHERE username = :username AND password = :password";
		return db.queryForObject(sql, paramJson, mappedClass);
	}
	
	/**
	 * 分页
	 * @param pageIPO
	 * @return
	 */
	public PageTVO<AdminDO> page(PageIPO pageIPO) {
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
		
		return db.pageWhere(tableName(), whereSql .toString(), pageIPO, mappedClass);
	}
	
}
