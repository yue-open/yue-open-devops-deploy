package ai.yue.open.dwz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.util.ParamUtils;
import ai.yue.library.base.util.UUIDUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.library.data.jdbc.ipo.PageIPO;
import ai.yue.open.dwz.dao.DwzDAO;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月20日
 */
@Service
public class DwzService {
	
	@Autowired
	DwzDAO dwzDAO;
	@Value("${domain.name}")
	String domainName;
	
	/**
	 * 生成短网址并返回
	 * @param paramMap
	 * @return
	 */
	public Result<?> shorten(JSONObject paramJson) {
		// 1. 校验参数
		String[] mustContainKeys = {};
		String[] canContainKeys = {};
		ParamUtils.paramValidate(paramJson, mustContainKeys, canContainKeys);
		
		// 2. 确认短网址是否存在
		String url = paramJson.getString("url");
		List<JSONObject> dwzList = dwzDAO.getDwz(url);
		if (dwzList.size() > 0) {
			String dwz = domainName + dwzList.get(0).get("dwz_code");
			return ResultInfo.success(dwz);
		}
		
		// 3. 获得唯一短网址后缀编码
		String dwz_code = "";
		int index = 0;
		while (true) {
			dwz_code = UUIDUtils.randomCode(6);
			if (!dwzDAO.isDwz(dwz_code)) {
				break;
			}
			index++;
			if (index > 5) {
				return ResultInfo.frequent_access_restriction();
			}
		}
		
		// 4. 获得短网址服务域名地址并插入短网址数据
		JSONObject insert = new JSONObject();
		insert.put("dwz_code", dwz_code);
		dwzDAO.insert(insert);
		
		// 5. 返回短网址
		String dwz = domainName + dwz_code;
		return ResultInfo.success(dwz);
	}
	
	/**
	 * 更新-ById
	 * @param paramJson
	 * @return
	 */
	public Result<?> updateById(JSONObject paramJson) {
		dwzDAO.updateById(paramJson);
		return ResultInfo.success();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public Result<?> delete(Long id) {
		dwzDAO.delete(id);
		return ResultInfo.success();
	}
	
	/**
	 * 获得可编辑信息
	 * @param id
	 * @return
	 */
	public Result<JSONObject> getEdit(Long id) {
		return ResultInfo.success(dwzDAO.getEdit(id));
	}
	
	/**
	 * 分页
	 * @param paramJson
	 * @return
	 */
	public Result<List<JSONObject>> page(JSONObject paramJson) {
		PageIPO pageIPO = PageIPO.parsePageIPO(paramJson);
		return dwzDAO.page(pageIPO).toResult();
	}
	
}
