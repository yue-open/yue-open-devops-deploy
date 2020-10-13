package ai.yue.open.dwz.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.crypto.client.SecureSingleton;
import ai.yue.library.base.util.MapUtils;
import ai.yue.library.base.util.ParamUtils;
import ai.yue.library.base.view.R;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultPrompt;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.open.dwz.dao.AdminDAO;
import ai.yue.open.dwz.dataobject.AdminDO;

/**
 * @author	孙金川
 * @since	2018年3月21日
 */
@Service
public class AdminService {

	@Autowired
	AdminDAO adminDAO;
	
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
		String password = paramJson.getString("password");
		password = SecureSingleton.getAES().encryptBase64(password);
		paramJson.replace("password", password);
		if (adminDAO.isAdminExist(username)) {
			return R.errorPrompt(ResultPrompt.USER_EXIST);
		}
		
		// 3. 插入数据
		adminDAO.insert(paramJson);
		
		// 4. 返回结果
		return R.success();
	}
	
	/**
	 * 分页
	 * @param paramJson
	 * @return
	 */
	public Result<List<AdminDO>> page(JSONObject paramJson) {
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
		return R.success();
	}
	
	/**
	 * 更新密码
	 * @param paramJson
	 * @return
	 */
	public Result<?> updatePassword(JSONObject paramJson) {
		adminDAO.updatePassword(paramJson);
		return R.success();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public Result<?> delete(Long id) {
		adminDAO.delete(id);
		return R.success();
	}
	
}
