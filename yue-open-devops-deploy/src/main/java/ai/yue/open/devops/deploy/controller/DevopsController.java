package ai.yue.open.devops.deploy.controller;

import ai.yue.library.base.view.Result;
import ai.yue.open.devops.deploy.constant.EnvEnum;
import ai.yue.open.devops.deploy.service.DevopsService;
import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rancher CD
 *
 * @author ylyue
 * @since 2018年11月12日
 */
@RestController
@RequestMapping("/devops")
public class DevopsController {

	@Autowired
	private DevopsService devopsService;
	
	/**
	 * 重新部署
	 * @param workloadApiUrl 工作负载API地址
	 * @param envEnum 环境
	 * @return
	 */
	@PutMapping("/redeploy")
	public Result<?> redeploy(@RequestParam("workloadApiUrl") String workloadApiUrl,
								@RequestParam("envEnum") EnvEnum envEnum,
								@RequestParam(name = "tag", required = false) String tag) {
		// 1. 打印日志
		JSONObject logInfo = new JSONObject();
		logInfo.put("workloadApiUrl", workloadApiUrl);
		logInfo.put("envEnum", envEnum);
		logInfo.put("tag", tag);
		Console.log("【工作负载-重新部署】请求参数：{}", logInfo);
		
		// 2. 重新部署
		return devopsService.redeploy(workloadApiUrl, envEnum, tag);
	}
	
}
