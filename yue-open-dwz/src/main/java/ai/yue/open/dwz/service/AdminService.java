package ai.yue.open.dwz.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.config.properties.ConstantProperties;
import ai.yue.library.base.util.AESUtils;
import ai.yue.library.base.util.MapUtils;
import ai.yue.library.base.util.ParamUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.open.dwz.dao.AdminDAO;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月21日
 */
@Service
public class AdminService {

	@Autowired
	AdminDAO adminDAO;
	@Autowired
	ConstantProperties constantProperties;
	
	/**
	 * 插入数据
	 * @param paramJson
	 * @return
	 */
	public Result<?> insert(JSONObject paramJson) {
		// 1. 校验参数
		String[] mustContainKeys = {"username", "password"};
		ParamUtils.paramValidate(paramJson, mustContainKeys);
		
		// 2. 处理参数
		MapUtils.trimStringValues(paramJson);
		String username = paramJson.getString("username");
		String password = AESUtils.encrypt(paramJson.getString("password"), constantProperties.getAes_keyt());
		paramJson.replace("password", password);
		if (adminDAO.isAdminExist(username)) {
			return ResultInfo.user_exist();
		}
		paramJson.put("admin_status", 1);
		
		// 3. 插入数据
		adminDAO.insert(paramJson);
		
		// 4. 返回结果
		return ResultInfo.success();
	}
	
	/**
	 * 分页
	 * @param paramJson
	 * @return
	 */
	public Result<List<JSONObject>> page(JSONObject paramJson) {
		PageIPO pageIPO = PageIPO.parsePageIPO(paramJson);
		return adminDAO.page(pageIPO).toResult();
	}
	
	/**
	 * 获得可编辑信息
	 * @param id
	 * @return
	 */
	public Map<String, Object> getEdit(Long id) {
		return adminDAO.getEdit(id);
	}
	
	/**
	 * 更新-ById
	 * @param paramJson
	 * @return
	 */
	public Result<?> updateById(JSONObject paramJson) {
		adminDAO.updateById(paramJson);
		return ResultInfo.success();
	}
	
	/**
	 * 更新密码
	 * @param paramJson
	 * @return
	 */
	public Result<?> updatePassword(JSONObject paramJson) {
		adminDAO.updatePassword(paramJson);
		return ResultInfo.success();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public Result<?> delete(Long id) {
		adminDAO.delete(id);
		return ResultInfo.success();
	}
	
}
