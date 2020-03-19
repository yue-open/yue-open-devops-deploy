package ai.yue.open.dwz.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.util.MapUtils;
import ai.yue.library.data.jdbc.dao.AbstractDAO;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.library.data.jdbc.vo.PageVO;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月20日
 */
@Repository
public class DwzDAO extends AbstractDAO {

	@Override
	protected String tableName() {
		return "dwz";
	}
	
	/**
	 * 确认短网址是否存在
	 * @param dwz_code
	 * @return
	 */
	public boolean isDwz(String dwz_code) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("dwz_code", dwz_code);
		String sql = "SELECT id FROM dwz WHERE dwz_code = :dwz_code";
		List<JSONObject> list = db.queryForList(sql, paramJson);
		if (list.size() <= 0) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 获得短网址
	 * @param url
	 * @return
	 */
	public List<JSONObject> getDwz(String url) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("url", url);
		String sql = "SELECT dwz_code FROM dwz WHERE url = :url";
		return db.queryForList(sql, paramJson);
	}
	
	/**
	 * 获得url
	 * @param String dwz_code
	 * @return
	 */
	public String getUrl(String dwz_code) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("dwz_code", dwz_code);
		String sql = "SELECT url FROM dwz WHERE dwz_code = :dwz_code";
		List<JSONObject> list = db.queryForList(sql, paramJson);
		if (list.size() <= 0) {
			return "";
		}
		
		return list.get(0).getString("url");
	}
	
	/**
	 * 获得可编辑信息
	 * @param id
	 * @return
	 */
	public JSONObject getEdit(Long id) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("id", id);
		String sql = "SELECT id, dwz_name, elaborate_on FROM dwz WHERE id = :id";
		return db.queryForJson(sql, paramJson);
	}
	
	/**
	 * 分页
	 * @param pageIPO
	 * @return
	 */
	public PageVO page(PageIPO pageIPO) {
		JSONObject conditions = pageIPO.getConditions();
		StringBuffer whereSql = new StringBuffer();
		String[] keys = {"start_date", "end_date"};
		if (conditions.containsKey("start_date") || conditions.containsKey("end_date")) {
			if (MapUtils.isKeys(conditions, keys)) {
				whereSql.append("WHERE create_time BETWEEN :start_date AND :end_date");
			}else if (conditions.containsKey("start_date")) {
				whereSql.append("WHERE create_time LIKE CONCAT(:start_date, '%')");
			}else {
				whereSql.append("WHERE create_time LIKE CONCAT(:end_date, '%')");
			}
		}
		
		return db.pageWhere(tableName, whereSql.toString(), pageIPO);
	}
	
}
